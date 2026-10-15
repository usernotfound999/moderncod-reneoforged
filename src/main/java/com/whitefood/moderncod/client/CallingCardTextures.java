package com.whitefood.moderncod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.whitefood.moderncod.ModernCodMod;
import com.whitefood.moderncod.callingcard.CallingCard;
import com.whitefood.moderncod.config.ModernCodConfig;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side registry of dynamically loaded custom calling cards.
 *
 * <p>Every custom image is validated (size, PNG signature, decodable) before being uploaded to the
 * GPU as a {@link DynamicTexture}. Anything that fails validation is refused, which is what keeps
 * the game from ever blitting the purple/black missing texture for a custom card.</p>
 *
 * <p>Must only be touched from the client render thread.</p>
 */
public final class CallingCardTextures {
   private static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();
   private static final Map<String, int[]> SIZES = new HashMap<>();

   private CallingCardTextures() {
   }

   public static synchronized boolean isLoaded(String hash) {
      return TEXTURES.containsKey(hash);
   }

   public static synchronized ResourceLocation location(String hash) {
      return TEXTURES.get(hash);
   }

   /** @return {@code {width, height}}, or {@code null} when the hash is unknown. */
   public static synchronized int[] size(String hash) {
      return SIZES.get(hash);
   }

   /**
    * Validates, decodes and registers {@code data} under {@code hash}.
    *
    * @return {@code true} on success (or when already loaded)
    */
   public static synchronized boolean register(String hash, byte[] data) {
      if (!CallingCard.isCustom(hash)) {
         return false;
      }
      if (TEXTURES.containsKey(hash)) {
         return true;
      }
      String error = CallingCard.validate(data, ModernCodConfig.maxUploadBytes());
      if (error != null) {
         ModernCodMod.LOGGER.warn("[moderncod] Refusing calling card {}: {}", hash, error);
         return false;
      }
      NativeImage image = null;
      try {
         // NOTE: NativeImage.read(byte[]) copies through LWJGL's fixed-size MemoryStack and throws
         // OutOfMemoryError("Out of stack space.") for data larger than ~64 KB. The InputStream
         // overload allocates on the native heap instead, so it is safe for full-size cards.
         image = NativeImage.read(new ByteArrayInputStream(data));
         ResourceLocation location = CallingCard.customTexture(hash);
         DynamicTexture texture = new DynamicTexture(image);
         // Ownership of the image has been transferred to the texture.
         image = null;
         Minecraft.getInstance().getTextureManager().register(location, texture);
         TEXTURES.put(hash, location);
         SIZES.put(hash, new int[]{texture.getPixels().getWidth(), texture.getPixels().getHeight()});
         return true;
      } catch (Throwable t) {
         // A bad image must never take the client down; refuse it and let the caller fall back.
         ModernCodMod.LOGGER.warn("[moderncod] Failed to decode calling card {}: {}", hash, t.toString());
         return false;
      } finally {
         if (image != null) {
            image.close();
         }
      }
   }
}
