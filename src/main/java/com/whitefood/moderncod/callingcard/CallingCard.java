package com.whitefood.moderncod.callingcard;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared helpers describing a calling card.
 *
 * <p>A card is either a <b>built-in</b> resource ({@code textures/gui/imageN.png} shipped inside the
 * mod jar) or a <b>custom</b> image addressed by the SHA-256 of its bytes. This class must stay
 * free of client-only classes so it can be used from common code.</p>
 */
public final class CallingCard {
   public static final String NAMESPACE = "moderncod";
   public static final String NO_HASH = "";
   public static final int MAX_DIMENSION = 2048;
   public static final ResourceLocation DEFAULT_IMAGE =
      ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/default_image.png");

   private static final byte[] PNG_SIGNATURE = {
      (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
   };

   private CallingCard() {
   }

   public static boolean isCustom(String hash) {
      return hash != null && !hash.isEmpty();
   }

   /** Texture location of a built-in calling card, e.g. {@code moderncod:textures/gui/image4.png}. */
   public static ResourceLocation builtinTexture(String path) {
      return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
   }

   /** Synthetic texture location a custom card is registered under at runtime. */
   public static ResourceLocation customTexture(String hash) {
      return ResourceLocation.fromNamespaceAndPath(NAMESPACE, "dynamic/callingcard/" + hash);
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
      int width = readInt(data, 16);
      int height = readInt(data, 20);
      return width > 0 && height > 0 ? new int[]{width, height} : null;
   }

   private static int readInt(byte[] data, int offset) {
      return (data[offset] & 0xFF) << 24
         | (data[offset + 1] & 0xFF) << 16
         | (data[offset + 2] & 0xFF) << 8
         | (data[offset + 3] & 0xFF);
   }

   /**
    * Validates a candidate custom image.
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
      if (!isPng(data)) {
         return "不是有效的 PNG 图片";
      }
      int[] size = pngSize(data);
      if (size == null) {
         return "不是有效的 PNG 图片";
      }
      if (size[0] > MAX_DIMENSION || size[1] > MAX_DIMENSION) {
         return "图片尺寸过大（最大 " + MAX_DIMENSION + "×" + MAX_DIMENSION + "）";
      }
      return null;
   }
}
