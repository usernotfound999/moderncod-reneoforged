package com.whitefood.moderncod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common configuration for the calling-card (nameplate) system.
 *
 * <p>Loaded on both the client and the dedicated server, so the client can honour the
 * upload limit while the server owns the on-disk cache budget.</p>
 */
public final class ModernCodConfig {
   public static final ModConfigSpec SPEC;

   private static final ModConfigSpec.BooleanValue CUSTOM_CARDS_ENABLED;
   private static final ModConfigSpec.ConfigValue<String> CALLING_CARDS_DIR;
   private static final ModConfigSpec.IntValue MAX_UPLOAD_KB;
   private static final ModConfigSpec.IntValue MAX_CACHE_MB;

   static {
      ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
      builder.comment("Calling card (nameplate) settings").push("calling_cards");
      CUSTOM_CARDS_ENABLED = builder
         .comment("Whether players may pick a custom calling card from the callingCardsDir folder.")
         .define("customCardsEnabled", true);
      CALLING_CARDS_DIR = builder
         .comment("Folder, relative to the config directory, scanned for custom calling card images.")
         .define("callingCardsDir", "moderncod/callingcards");
      MAX_UPLOAD_KB = builder
         .comment("Maximum size (in KB) of a custom calling card image. Larger files are rejected.")
         .defineInRange("maxUploadKb", 300, 1, 10240);
      MAX_CACHE_MB = builder
         .comment("Maximum size (in MB) the server-side calling card cache may use on disk.")
         .defineInRange("maxCacheMb", 256, 1, 10240);
      builder.pop();
      SPEC = builder.build();
   }

   private ModernCodConfig() {
   }

   public static boolean customCardsEnabled() {
      return CUSTOM_CARDS_ENABLED.get();
   }

   public static String callingCardsDir() {
      return CALLING_CARDS_DIR.get();
   }

   public static int maxUploadKb() {
      return MAX_UPLOAD_KB.get();
   }

   public static int maxUploadBytes() {
      return MAX_UPLOAD_KB.get() * 1024;
   }

   public static long maxCacheBytes() {
      return (long) MAX_CACHE_MB.get() * 1024L * 1024L;
   }
}
