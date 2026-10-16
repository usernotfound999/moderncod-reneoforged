package com.whitefood.moderncod.callingcard;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.ngengine.webp.decoder.DecodedWebP;
import org.ngengine.webp.decoder.WebPDecoder;

/**
 * Minimal animated-WebP reader, built on top of the pure-Java {@code org.ngengine.webp.decoder}
 * library.
 *
 * <p>That library refuses animated files outright, but the container is easy to walk: an animated
 * WebP is {@code RIFF/WEBP -> VP8X(ANIM) -> ANIM -> ANMF*}, and each {@code ANMF} frame payload is a
 * self-contained VP8/VP8L bitstream. So we re-wrap every frame payload into a tiny still-WebP
 * container, decode it with the library, and composite the frames onto the canvas exactly like the
 * WebP specification (blend / dispose flags) prescribes.</p>
 *
 * <p>Plain Java only (no Minecraft classes) so it can be unit-tested headless and used on either
 * side.</p>
 */
public final class WebpAnimation {
   public static final int DEFAULT_FRAME_MS = 100;
   /** Hard cap so a hostile/huge animation cannot exhaust memory. */
   public static final int ABSOLUTE_MAX_FRAMES = 512;

   public static final class Frame {
      public final int durationMs;
      /** Canvas-sized, tightly packed RGBA8888. */
      public final byte[] rgba;

      private Frame(int durationMs, byte[] rgba) {
         this.durationMs = durationMs;
         this.rgba = rgba;
      }
   }

   public static final class Animation {
      public final int width;
      public final int height;
      public final List<Frame> frames;

      private Animation(int width, int height, List<Frame> frames) {
         this.width = width;
         this.height = height;
         this.frames = frames;
      }
   }

   private static final class FrameInfo {
      int x;
      int y;
      int width;
      int height;
      int durationMs;
      boolean disposeToBackground;
      boolean noBlend;
      int payloadOffset;
      int payloadLength;
   }

   private WebpAnimation() {
   }

   /**
    * @param maxFrames maximum number of frames to keep (clamped to {@link #ABSOLUTE_MAX_FRAMES})
    * @return the decoded animation, or {@code null} when {@code data} is not an animated WebP
    */
   public static Animation decode(byte[] data, int maxFrames) throws Exception {
      if (!ImageBytes.isAnimatedWebp(data)) {
         return null;
      }
      int limit = Math.max(1, Math.min(maxFrames, ABSOLUTE_MAX_FRAMES));

      int canvasWidth = 0;
      int canvasHeight = 0;
      int background = 0;
      List<FrameInfo> infos = new ArrayList<>();

      int pos = 12;
      while (pos + 8 <= data.length) {
         String fourCc = ImageBytes.fourCc(data, pos);
         int size = ImageBytes.readIntLE(data, pos + 4);
         if (size < 0) {
            break;
         }
         int start = pos + 8;
         if (start + size > data.length) {
            break;
         }
         if ("VP8X".equals(fourCc) && size >= 10) {
            canvasWidth = ImageBytes.read3LE(data, start + 4) + 1;
            canvasHeight = ImageBytes.read3LE(data, start + 7) + 1;
         } else if ("ANIM".equals(fourCc) && size >= 4) {
            // Background colour is stored BGRA.
            background = (data[start + 3] & 0xFF) << 24
               | (data[start + 2] & 0xFF) << 16
               | (data[start + 1] & 0xFF) << 8
               | (data[start] & 0xFF);
         } else if ("ANMF".equals(fourCc) && size >= 16) {
            FrameInfo info = new FrameInfo();
            info.x = ImageBytes.read3LE(data, start) * 2;
            info.y = ImageBytes.read3LE(data, start + 3) * 2;
            info.width = ImageBytes.read3LE(data, start + 6) + 1;
            info.height = ImageBytes.read3LE(data, start + 9) + 1;
            info.durationMs = ImageBytes.read3LE(data, start + 12);
            int flags = data[start + 15] & 0xFF;
            info.disposeToBackground = (flags & 0b0000_0001) != 0;
            info.noBlend = (flags & 0b0000_0010) != 0;
            info.payloadOffset = start + 16;
            info.payloadLength = size - 16;
            if (info.width > 0 && info.height > 0 && info.payloadLength > 0) {
               infos.add(info);
            }
         }
         pos = start + size + (size & 1);
      }

      if (canvasWidth <= 0 || canvasHeight <= 0 || infos.isEmpty()) {
         return null;
      }
      if (infos.size() > limit) {
         infos = new ArrayList<>(infos.subList(0, limit));
      }

      int[] canvas = new int[canvasWidth * canvasHeight];
      if (background != 0) {
         java.util.Arrays.fill(canvas, background);
      }

      List<Frame> frames = new ArrayList<>(infos.size());
      FrameInfo previous = null;
      for (FrameInfo info : infos) {
         if (previous != null && previous.disposeToBackground) {
            clearRect(canvas, canvasWidth, canvasHeight, background, previous);
         }
         int[] pixels = decodeFrame(data, info);
         if (pixels == null) {
            continue;
         }
         drawFrame(canvas, canvasWidth, canvasHeight, info, pixels);
         frames.add(new Frame(info.durationMs > 0 ? info.durationMs : DEFAULT_FRAME_MS, toRgba(canvas)));
         previous = info;
      }

      return frames.size() > 1 ? new Animation(canvasWidth, canvasHeight, frames) : null;
   }

   private static int[] decodeFrame(byte[] data, FrameInfo info) throws Exception {
      byte[] still = wrapAsStillWebp(data, info);
      DecodedWebP decoded = WebPDecoder.decode(still);
      int[] pixels = new int[decoded.width * decoded.height];
      ByteBuffer rgba = decoded.rgba;
      for (int i = 0; i < pixels.length; i++) {
         int r = rgba.get(i * 4) & 0xFF;
         int g = rgba.get(i * 4 + 1) & 0xFF;
         int b = rgba.get(i * 4 + 2) & 0xFF;
         int a = rgba.get(i * 4 + 3) & 0xFF;
         pixels[i] = a << 24 | r << 16 | g << 8 | b;
      }
      return pixels;
   }

   /**
    * Builds a self-contained still-WebP container around one ANMF frame payload. A {@code VP8X}
    * chunk is only needed when the payload carries a separate {@code ALPH} chunk.
    */
   private static byte[] wrapAsStillWebp(byte[] data, FrameInfo info) {
      boolean hasAlpha = info.payloadLength >= 4 && "ALPH".equals(ImageBytes.fourCc(data, info.payloadOffset));
      int vp8xLength = hasAlpha ? 18 : 0;
      int total = 12 + vp8xLength + info.payloadLength;
      byte[] out = new byte[total];
      out[0] = 'R';
      out[1] = 'I';
      out[2] = 'F';
      out[3] = 'F';
      writeIntLE(out, 4, total - 8);
      out[8] = 'W';
      out[9] = 'E';
      out[10] = 'B';
      out[11] = 'P';
      int offset = 12;
      if (hasAlpha) {
         out[offset] = 'V';
         out[offset + 1] = 'P';
         out[offset + 2] = '8';
         out[offset + 3] = 'X';
         writeIntLE(out, offset + 4, 10);
         out[offset + 8] = 0x10; // alpha flag
         write3LE(out, offset + 12, info.width - 1);
         write3LE(out, offset + 15, info.height - 1);
         offset += 18;
      }
      System.arraycopy(data, info.payloadOffset, out, offset, info.payloadLength);
      return out;
   }

   private static void drawFrame(int[] canvas, int canvasWidth, int canvasHeight, FrameInfo info, int[] pixels) {
      if (info.x == 0 && info.y == 0 && info.width == canvasWidth && info.height == canvasHeight) {
         for (int i = 0; i < canvas.length; i++) {
            canvas[i] = info.noBlend ? pixels[i] : blendOver(pixels[i], canvas[i]);
         }
         return;
      }
      for (int fy = 0; fy < info.height; fy++) {
         int cy = info.y + fy;
         if (cy < 0 || cy >= canvasHeight) {
            continue;
         }
         for (int fx = 0; fx < info.width; fx++) {
            int cx = info.x + fx;
            if (cx < 0 || cx >= canvasWidth) {
               continue;
            }
            int src = pixels[fy * info.width + fx];
            int index = cy * canvasWidth + cx;
            canvas[index] = info.noBlend ? src : blendOver(src, canvas[index]);
         }
      }
   }

   private static void clearRect(int[] canvas, int canvasWidth, int canvasHeight, int background, FrameInfo info) {
      int fill = info.x == 0 && info.y == 0 && info.width == canvasWidth && info.height == canvasHeight
         ? background : 0;
      for (int fy = 0; fy < info.height; fy++) {
         int cy = info.y + fy;
         if (cy < 0 || cy >= canvasHeight) {
            continue;
         }
         for (int fx = 0; fx < info.width; fx++) {
            int cx = info.x + fx;
            if (cx < 0 || cx >= canvasWidth) {
               continue;
            }
            canvas[cy * canvasWidth + cx] = background == 0 ? fill : background;
         }
      }
   }

   /** Straight-alpha source-over compositing. */
   private static int blendOver(int src, int dst) {
      int sa = src >>> 24;
      if (sa == 255) {
         return src;
      }
      if (sa == 0) {
         return dst;
      }
      int da = dst >>> 24;
      int sr = (src >> 16) & 0xFF;
      int sg = (src >> 8) & 0xFF;
      int sb = src & 0xFF;
      int dr = (dst >> 16) & 0xFF;
      int dg = (dst >> 8) & 0xFF;
      int db = dst & 0xFF;
      double s = sa / 255.0;
      double d = (da / 255.0) * (1.0 - s);
      double outA = s + d;
      if (outA <= 0.0) {
         return 0;
      }
      int r = (int) Math.round((sr * s + dr * d) / outA);
      int g = (int) Math.round((sg * s + dg * d) / outA);
      int b = (int) Math.round((sb * s + db * d) / outA);
      int a = (int) Math.round(outA * 255.0);
      return clamp255(a) << 24 | clamp255(r) << 16 | clamp255(g) << 8 | clamp255(b);
   }

   private static int clamp255(int value) {
      return value < 0 ? 0 : Math.min(value, 255);
   }

   private static byte[] toRgba(int[] canvas) {
      byte[] out = new byte[canvas.length * 4];
      for (int i = 0; i < canvas.length; i++) {
         int pixel = canvas[i];
         out[i * 4] = (byte)(pixel >> 16);
         out[i * 4 + 1] = (byte)(pixel >> 8);
         out[i * 4 + 2] = (byte) pixel;
         out[i * 4 + 3] = (byte)(pixel >>> 24);
      }
      return out;
   }

   private static void writeIntLE(byte[] out, int offset, int value) {
      out[offset] = (byte) value;
      out[offset + 1] = (byte)(value >> 8);
      out[offset + 2] = (byte)(value >> 16);
      out[offset + 3] = (byte)(value >> 24);
   }

   private static void write3LE(byte[] out, int offset, int value) {
      out[offset] = (byte) value;
      out[offset + 1] = (byte)(value >> 8);
      out[offset + 2] = (byte)(value >> 16);
   }
}
