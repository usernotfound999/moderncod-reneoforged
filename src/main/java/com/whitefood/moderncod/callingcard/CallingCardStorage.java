package com.whitefood.moderncod.callingcard;

import com.whitefood.moderncod.ModernCodMod;
import com.whitefood.moderncod.config.ModernCodConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Server-side, content-addressed, on-disk cache of custom calling card images.
 *
 * <p>Files are stored as {@code <hash>.png} inside {@code <gameDir>/moderncod/calling_card_cache}.
 * Only the bytes are kept on disk: the per-player "currently equipped" reference lives in that
 * player's persistent data (see {@code PlayerImageManager}).</p>
 */
public final class CallingCardStorage {
   private static final String CACHE_DIR = "moderncod/calling_card_cache";
   private static final String SUFFIX = ".png";

   private CallingCardStorage() {
   }

   public static Path directory() {
      Path dir = FMLPaths.GAMEDIR.get().resolve(CACHE_DIR);
      try {
         Files.createDirectories(dir);
      } catch (IOException e) {
         throw new UncheckedIOException("Could not create calling card cache directory", e);
      }
      return dir;
   }

   private static Path fileFor(String hash) {
      return directory().resolve(hash + SUFFIX);
   }

   public static boolean exists(String hash) {
      return CallingCard.isCustom(hash) && Files.isRegularFile(fileFor(hash));
   }

   /** @return the cached bytes, or {@code null} when the hash is unknown. */
   public static byte[] load(String hash) {
      if (!exists(hash)) {
         return null;
      }
      try {
         return Files.readAllBytes(fileFor(hash));
      } catch (IOException e) {
         ModernCodMod.LOGGER.warn("[moderncod] Failed to read calling card {}: {}", hash, e.toString());
         return null;
      }
   }

   /** Stores the bytes, returning {@code true} when the cache now contains the hash. */
   public static boolean store(String hash, byte[] data) {
      if (!CallingCard.isCustom(hash) || data == null || data.length == 0) {
         return false;
      }
      Path file = fileFor(hash);
      try {
         if (Files.isRegularFile(file)) {
            // Refresh the timestamp so eviction treats it as recently used.
            Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis()));
         } else {
            Files.write(file, data);
         }
         return true;
      } catch (IOException e) {
         ModernCodMod.LOGGER.warn("[moderncod] Failed to store calling card {}: {}", hash, e.toString());
         return false;
      }
   }

   /**
    * Deletes the least recently used files until the cache fits inside the configured budget.
    *
    * @param protectedHashes hashes currently used by online players; never evicted
    */
   public static void enforceLimit(Set<String> protectedHashes) {
      long cap = ModernCodConfig.maxCacheBytes();
      List<Path> files = listFiles();
      long total = files.stream().mapToLong(CallingCardStorage::sizeOf).sum();
      if (total <= cap) {
         return;
      }
      files.sort(Comparator.comparingLong(CallingCardStorage::lastModified));
      for (Path file : files) {
         if (total <= cap) {
            break;
         }
         String name = file.getFileName().toString();
         String hash = name.endsWith(SUFFIX) ? name.substring(0, name.length() - SUFFIX.length()) : name;
         if (protectedHashes != null && protectedHashes.contains(hash)) {
            continue;
         }
         long size = sizeOf(file);
         try {
            if (Files.deleteIfExists(file)) {
               total -= size;
            }
         } catch (IOException e) {
            ModernCodMod.LOGGER.warn("[moderncod] Failed to evict calling card {}: {}", hash, e.toString());
         }
      }
   }

   private static List<Path> listFiles() {
      List<Path> result = new ArrayList<>();
      try (Stream<Path> stream = Files.list(directory())) {
         stream.filter(Files::isRegularFile).forEach(result::add);
      } catch (IOException e) {
         ModernCodMod.LOGGER.warn("[moderncod] Failed to list calling card cache: {}", e.toString());
      }
      return result;
   }

   private static long sizeOf(Path file) {
      try {
         return Files.size(file);
      } catch (IOException e) {
         return 0L;
      }
   }

   private static long lastModified(Path file) {
      try {
         return Files.getLastModifiedTime(file).toMillis();
      } catch (IOException e) {
         return 0L;
      }
   }
}
