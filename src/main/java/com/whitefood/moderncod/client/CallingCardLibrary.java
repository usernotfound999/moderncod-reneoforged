package com.whitefood.moderncod.client;

import com.whitefood.moderncod.ModernCodMod;
import com.whitefood.moderncod.callingcard.CallingCard;
import com.whitefood.moderncod.config.ModernCodConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Client-side scanner for the config-driven calling card folder.
 *
 * <p>The folder lives under the <b>game</b> directory (default
 * {@code moderncod/custom_calling_cards}) rather than the config directory. PNG and WebP files
 * (WebP may even be animated) within the configured size limit are accepted; anything invalid is
 * kept in the list with a non-null {@link Entry#error} so the selection screen can warn instead of
 * silently hiding it.</p>
 */
public final class CallingCardLibrary {
   public static final class Entry {
      private final String fileName;
      private final Path path;
      private final byte[] data;
      private final String error;

      private Entry(String fileName, Path path, byte[] data, String error) {
         this.fileName = fileName;
         this.path = path;
         this.data = data;
         this.error = error;
      }

      public String fileName() {
         return this.fileName;
      }

      public Path path() {
         return this.path;
      }

      /** Raw bytes, or {@code null} when the file could not be read / was rejected. */
      public byte[] data() {
         return this.data;
      }

      /** {@code null} when the file is a usable image, otherwise a human readable reason. */
      public String error() {
         return this.error;
      }

      public boolean valid() {
         return this.error == null && this.data != null;
      }
   }

   private static List<Entry> cache;

   private CallingCardLibrary() {
   }

   public static Path directory() {
      return FMLPaths.GAMEDIR.get().resolve(ModernCodConfig.callingCardsDir());
   }

   public static synchronized List<Entry> entries() {
      return entries(false);
   }

   public static synchronized List<Entry> entries(boolean refresh) {
      if (cache != null && !refresh) {
         return cache;
      }
      List<Entry> result = new ArrayList<>();
      Path directory = directory();
      try {
         if (!Files.isDirectory(directory)) {
            Files.createDirectories(directory);
            cache = result;
            return result;
         }
         try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
               .filter(path -> {
                  String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                  return name.endsWith(".png") || name.endsWith(".webp");
               })
               .forEach(path -> result.add(readEntry(path)));
         }
      } catch (IOException e) {
         ModernCodMod.LOGGER.warn("[moderncod] Failed to scan calling card folder {}: {}", directory, e.toString());
      }
      result.sort(Comparator.comparing(Entry::fileName));
      cache = result;
      return result;
   }

   /** Invalidates the cached scan so the next call re-reads the folder. */
   public static synchronized void invalidate() {
      cache = null;
   }

   private static Entry readEntry(Path path) {
      String fileName = path.getFileName().toString();
      long size;
      try {
         size = Files.size(path);
      } catch (IOException e) {
         return new Entry(fileName, path, null, "无法读取文件");
      }
      int maxBytes = ModernCodConfig.maxUploadBytes();
      if (size <= 0L) {
         return new Entry(fileName, path, null, "无法读取文件");
      }
      if (size > maxBytes) {
         return new Entry(fileName, path, null, "文件过大（最大 " + (maxBytes / 1024) + " KB）");
      }
      byte[] data;
      try {
         data = Files.readAllBytes(path);
      } catch (IOException e) {
         return new Entry(fileName, path, null, "无法读取文件");
      }
      String error = CallingCard.validate(data, maxBytes);
      return new Entry(fileName, path, error == null ? data : null, error);
   }
}
