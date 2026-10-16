package com.whitefood.moderncod.callingcard;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Byte-level PNG / WebP helpers.
 *
 * <p>Deliberately free of Minecraft (and other client) classes so the container parsing can be
 * exercised headless in a plain JVM, and used from common code.</p>
 */
public final class ImageBytes {
   /** Supported custom-image encodings. */
   public enum Format {
      PNG,
      WEBP,
      UNKNOWN
   }

   public static final int MAX_DIMENSION = 2048;

   private static final byte[] PNG_SIGNATURE = {
      (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
   };

   private ImageBytes() {
   }

   /** Lower-case hex SHA-256 of the supplied bytes. */
   public static String hash(byte[] data) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] sum = digest.digest(data);
         StringBuilder builder = new StringBuilder(sum.length * 2);
         for (byte b : sum) {
            builder.append(Character.forDigit((b >> 4) & 0xF, 16));
            builder.append(Character.forDigit(b & 0xF, 16));
         }
         return builder.toString();
      } catch (NoSuchAlgorithmException e) {
         // SHA-256 is guaranteed by the JLS; fall back to a stable id if it somehow is missing.
         return Integer.toHexString(java.util.Arrays.hashCode(data));
      }
   }

   public static boolean isPng(byte[] data) {
      if (data == null || data.length < PNG_SIGNATURE.length) {
         return false;
      }
      for (int i = 0; i < PNG_SIGNATURE.length; i++) {
         if (data[i] != PNG_SIGNATURE[i]) {
            return false;
         }
      }
      return true;
   }

   /** {@code true} for a RIFF/WEBP container (still or animated). */
   public static boolean isWebp(byte[] data) {
      return data != null
         && data.length >= 16
         && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
         && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
   }

   public static Format format(byte[] data) {
      if (isPng(data)) {
         return Format.PNG;
      }
      return isWebp(data) ? Format.WEBP : Format.UNKNOWN;
   }

   /**
    * Reads the width/height from the PNG IHDR chunk.
    *
    * @return {@code {width, height}}, or {@code null} when the header is not a usable PNG IHDR
    */
   public static int[] pngSize(byte[] data) {
      if (!isPng(data) || data.length < 24) {
         return null;
      }
      if (data[12] != 'I' || data[13] != 'H' || data[14] != 'D' || data[15] != 'R') {
         return null;
      }
      int width = readIntBE(data, 16);
      int height = readIntBE(data, 20);
      return width > 0 && height > 0 ? new int[]{width, height} : null;
   }

   /**
    * Reads the canvas size out of a WebP container without decoding it (VP8X / VP8 / VP8L header).
    *
    * @return {@code {width, height}}, or {@code null} when it cannot be read
    */
   public static int[] webpSize(byte[] data) {
      if (!isWebp(data)) {
         return null;
      }
      int pos = 12;
      while (pos + 8 <= data.length) {
         String fourCc = fourCc(data, pos);
         int size = readIntLE(data, pos + 4);
         if (size < 0) {
            return null;
         }
         int start = pos + 8;
         if (start + size > data.length) {
            size = data.length - start;
         }
         if ("VP8X".equals(fourCc)) {
            if (size < 10) {
               return null;
            }
            return new int[]{read3LE(data, start + 4) + 1, read3LE(data, start + 7) + 1};
         }
         if ("VP8 ".equals(fourCc)) {
            if (size < 10) {
               return null;
            }
            int width = (data[start + 6] & 0xFF | (data[start + 7] & 0xFF) << 8) & 0x3FFF;
            int height = (data[start + 8] & 0xFF | (data[start + 9] & 0xFF) << 8) & 0x3FFF;
            return width > 0 && height > 0 ? new int[]{width, height} : null;
         }
         if ("VP8L".equals(fourCc)) {
            if (size < 5 || (data[start] & 0xFF) != 0x2F) {
               return null;
            }
            int header = readIntLE(data, start + 1);
            return new int[]{(1 + header) & 0x3FFF, (1 + (header >>> 14)) & 0x3FFF};
         }
         pos = start + size + (size & 1);
      }
      return null;
   }

   /** {@code true} when the WebP container declares the animation flag. */
   public static boolean isAnimatedWebp(byte[] data) {
      if (!isWebp(data) || data.length < 30) {
         return false;
      }
      if (!"VP8X".equals(fourCc(data, 12))) {
         return false;
      }
      return (data[20] & 0b0000_0010) != 0;
   }

   /** @return the canvas size of any supported custom image, or {@code null}. */
   public static int[] imageSize(byte[] data) {
      return isPng(data) ? pngSize(data) : webpSize(data);
   }

   /**
    * Validates a candidate custom image (PNG or WebP) without fully decoding it.
    *
    * @param data     raw file bytes
    * @param maxBytes maximum accepted size in bytes
    * @return {@code null} when acceptable, otherwise a human readable Chinese reason
    */
   public static String validate(byte[] data, int maxBytes) {
      if (data == null || data.length == 0) {
         return "图片为空";
      }
      if (data.length > maxBytes) {
         return "文件过大（最大 " + (maxBytes / 1024) + " KB）";
      }
      int[] size = imageSize(data);
      if (size == null) {
         return "不是有效的 PNG/WebP 图片";
      }
      if (size[0] > MAX_DIMENSION || size[1] > MAX_DIMENSION) {
         return "图片尺寸过大（最大 " + MAX_DIMENSION + "×" + MAX_DIMENSION + "）";
      }
      return null;
   }

   static String fourCc(byte[] data, int offset) {
      if (offset + 4 > data.length || offset < 0) {
         return "";
      }
      return new String(new char[]{
         (char)(data[offset] & 0xFF), (char)(data[offset + 1] & 0xFF),
         (char)(data[offset + 2] & 0xFF), (char)(data[offset + 3] & 0xFF)
      });
   }

   static int readIntLE(byte[] data, int offset) {
      if (offset < 0 || offset + 4 > data.length) {
         return -1;
      }
      return (data[offset] & 0xFF)
         | (data[offset + 1] & 0xFF) << 8
         | (data[offset + 2] & 0xFF) << 16
         | (data[offset + 3] & 0xFF) << 24;
   }

   static int read3LE(byte[] data, int offset) {
      if (offset < 0 || offset + 3 > data.length) {
         return 0;
      }
      return (data[offset] & 0xFF) | (data[offset + 1] & 0xFF) << 8 | (data[offset + 2] & 0xFF) << 16;
   }

   private static int readIntBE(byte[] data, int offset) {
      return (data[offset] & 0xFF) << 24
         | (data[offset + 1] & 0xFF) << 16
         | (data[offset + 2] & 0xFF) << 8
         | (data[offset + 3] & 0xFF);
   }
}
