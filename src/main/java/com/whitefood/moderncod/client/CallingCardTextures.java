package com.whitefood.moderncod.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.whitefood.moderncod.ModernCodMod;
import com.whitefood.moderncod.callingcard.CallingCard;
import com.whitefood.moderncod.callingcard.ImageBytes;
import com.whitefood.moderncod.callingcard.WebpAnimation;
import com.whitefood.moderncod.config.ModernCodConfig;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.ngengine.webp.decoder.DecodedWebP;
import org.ngengine.webp.decoder.WebPDecoder;

/**
 * Client-side registry of dynamically loaded custom calling cards.
 *
 * <p>Accepts PNG and WebP. Animated WebP is turned into an {@link AnimatedCardTexture}; still images
 * become plain {@link DynamicTexture}s. Everything is validated (size, container header, readable
 * dimensions) before upload, which is what keeps the game from ever blitting the purple/black
 * missing texture for a custom card.</p>
 *
 * <p>Must only be touched from the client render thread.</p>
 */
@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public final class CallingCardTextures {
   private static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();
   private static final Map<String, int[]> SIZES = new HashMap<>();
   private static final Map<String, AnimatedCardTexture> ANIMATED = new HashMap<>();

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
      try {
         ResourceLocation location = CallingCard.customTexture(hash);
         Minecraft minecraft = Minecraft.getInstance();

         if (ImageBytes.isWebp(data) && ImageBytes.isAnimatedWebp(data)) {
            return registerAnimated(hash, location, data, minecraft);
         }

         NativeImage image = readStill(data);
         if (image == null) {
            return false;
         }
         int width = image.getWidth();
         int height = image.getHeight();
         DynamicTexture texture = new DynamicTexture(image);
         minecraft.getTextureManager().register(location, texture);
         TEXTURES.put(hash, location);
         SIZES.put(hash, new int[]{width, height});
         return true;
      } catch (Throwable t) {
         // A bad image must never take the client down; refuse it and let the caller fall back.
         ModernCodMod.LOGGER.warn("[moderncod] Failed to decode calling card {}: {}", hash, t.toString());
         return false;
      }
   }

   private static boolean registerAnimated(String hash, ResourceLocation location, byte[] data, Minecraft minecraft)
      throws Exception {
      WebpAnimation.Animation animation = WebpAnimation.decode(data, ModernCodConfig.maxWebpFrames());
      if (animation == null || animation.frames.isEmpty()) {
         ModernCodMod.LOGGER.warn("[moderncod] Failed to decode animated calling card {}", hash);
         return false;
      }
      if (animation.frames.size() > 1) {
         NativeImage[] frames = new NativeImage[animation.frames.size()];
         int[] durations = new int[animation.frames.size()];
         for (int i = 0; i < frames.length; i++) {
            WebpAnimation.Frame frame = animation.frames.get(i);
            frames[i] = toNativeImage(animation.width, animation.height, frame.rgba);
            durations[i] = frame.durationMs;
         }
         AnimatedCardTexture texture = new AnimatedCardTexture(frames, durations);
         minecraft.getTextureManager().register(location, texture);
         TEXTURES.put(hash, location);
         SIZES.put(hash, new int[]{animation.width, animation.height});
         ANIMATED.put(hash, texture);
         return true;
      }
      // Animated container that only yielded a single frame: render it as a still card.
      NativeImage image = toNativeImage(animation.width, animation.height, animation.frames.get(0).rgba);
      DynamicTexture texture = new DynamicTexture(image);
      minecraft.getTextureManager().register(location, texture);
      TEXTURES.put(hash, location);
      SIZES.put(hash, new int[]{animation.width, animation.height});
      return true;
   }

   private static NativeImage readStill(byte[] data) throws Exception {
      if (ImageBytes.isWebp(data)) {
         DecodedWebP decoded = WebPDecoder.decode(data);
         int length = decoded.width * decoded.height * 4;
         byte[] rgba = new byte[length];
         ByteBuffer buffer = decoded.rgba;
         for (int i = 0; i < length; i++) {
            rgba[i] = buffer.get(i);
         }
         return toNativeImage(decoded.width, decoded.height, rgba);
      }
      // NOTE: NativeImage.read(byte[]) copies through LWJGL's fixed-size MemoryStack and throws
      // OutOfMemoryError("Out of stack space.") for data larger than ~64 KB. The InputStream
      // overload allocates on the native heap instead, so it is safe for full-size cards.
      return NativeImage.read(new ByteArrayInputStream(data));
   }

   private static NativeImage toNativeImage(int width, int height, byte[] rgba) {
      NativeImage image = new NativeImage(width, height, false);
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int i = (y * width + x) * 4;
            int r = rgba[i] & 0xFF;
            int g = rgba[i + 1] & 0xFF;
            int b = rgba[i + 2] & 0xFF;
            int a = rgba[i + 3] & 0xFF;
            image.setPixelRGBA(x, y, a << 24 | b << 16 | g << 8 | r);
         }
      }
      return image;
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent.Post event) {
      tickAnimations();
   }

   private static synchronized void tickAnimations() {
      if (ANIMATED.isEmpty()) {
         return;
      }
      for (AnimatedCardTexture texture : ANIMATED.values()) {
         texture.tick();
      }
   }
}
