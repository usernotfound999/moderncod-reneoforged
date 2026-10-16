package com.whitefood.moderncod.callingcard;

import net.minecraft.resources.ResourceLocation;

/**
 * Shared helpers describing a calling card.
 *
 * <p>A card is either a <b>built-in</b> resource ({@code textures/gui/imageN.png} shipped inside the
 * mod jar) or a <b>custom</b> image addressed by the SHA-256 of its bytes. Custom images may be PNG
 * or WebP (WebP may even be animated). Byte-level parsing lives in {@link ImageBytes}.</p>
 */
public final class CallingCard {
   public static final String NAMESPACE = "moderncod";
   public static final String NO_HASH = "";
   public static final int MAX_DIMENSION = ImageBytes.MAX_DIMENSION;
   public static final ResourceLocation DEFAULT_IMAGE =
      ResourceLocation.fromNamespaceAndPath(NAMESPACE, "textures/gui/default_image.png");

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

   public static String hash(byte[] data) {
      return ImageBytes.hash(data);
   }

   public static boolean isPng(byte[] data) {
      return ImageBytes.isPng(data);
   }

   public static boolean isWebp(byte[] data) {
      return ImageBytes.isWebp(data);
   }

   public static ImageBytes.Format format(byte[] data) {
      return ImageBytes.format(data);
   }

   public static boolean isAnimatedWebp(byte[] data) {
      return ImageBytes.isAnimatedWebp(data);
   }

   public static int[] imageSize(byte[] data) {
      return ImageBytes.imageSize(data);
   }

   public static String validate(byte[] data, int maxBytes) {
      return ImageBytes.validate(data, maxBytes);
   }
}
