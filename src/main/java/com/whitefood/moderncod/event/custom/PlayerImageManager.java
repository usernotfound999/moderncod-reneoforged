package com.whitefood.moderncod.event.custom;

import com.whitefood.moderncod.callingcard.CallingCard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side persistent storage of a player's equipped calling card.
 *
 * <p>A player either uses a built-in resource path ({@code TAG_IMAGE_PATH}) or a custom image
 * addressed by hash ({@code TAG_IMAGE_HASH}). When the hash is non-empty it wins.</p>
 */
public class PlayerImageManager {
   public static final ResourceLocation DEFAULT_IMAGE = CallingCard.DEFAULT_IMAGE;
   private static final String TAG_IMAGE_PATH = "moderncod:image_path";
   private static final String TAG_IMAGE_HASH = "moderncod:image_hash";

   public PlayerImageManager() {
   }

   /** @return the custom image hash, or {@link CallingCard#NO_HASH} when a built-in card is equipped. */
   public static String getPlayerImageHash(Player player) {
      CompoundTag data = player.getPersistentData();
      String hash = data.getString(TAG_IMAGE_HASH);
      return hash != null ? hash : CallingCard.NO_HASH;
   }

   /** @return the built-in resource path, falling back to the default image. */
   public static String getPlayerImagePath(Player player) {
      CompoundTag data = player.getPersistentData();
      String path = data.getString(TAG_IMAGE_PATH);
      return path == null || path.isEmpty() ? DEFAULT_IMAGE.getPath() : path;
   }

   /** Equips a built-in resource and clears any custom hash. */
   public static void setPlayerImage(Player player, String imagePath) {
      CompoundTag data = player.getPersistentData();
      data.putString(TAG_IMAGE_PATH, imagePath != null && !imagePath.isEmpty() ? imagePath : DEFAULT_IMAGE.getPath());
      data.putString(TAG_IMAGE_HASH, CallingCard.NO_HASH);
   }

   /** Equips a custom image (by hash) and resets the built-in path to the default. */
   public static void setPlayerImageHash(Player player, String hash) {
      CompoundTag data = player.getPersistentData();
      data.putString(TAG_IMAGE_HASH, hash != null ? hash : CallingCard.NO_HASH);
      data.putString(TAG_IMAGE_PATH, DEFAULT_IMAGE.getPath());
   }
}
