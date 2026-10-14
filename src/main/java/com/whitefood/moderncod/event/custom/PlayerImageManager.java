package com.whitefood.moderncod.event.custom;

import com.whitefood.moderncod.client.ClientImageDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PlayerImageManager {
   public static final ResourceLocation DEFAULT_IMAGE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/default_image.png");
   private static final String TAG_IMAGE_PATH = "moderncod:image_path";

   public PlayerImageManager() {
   }

   public static String getPlayerImage(Player player) {
      if (player.level().isClientSide) {
         return ClientImageDataManager.getPlayerImage(player.getUUID());
      }
      CompoundTag data = player.getPersistentData();
      String path = data.getString(TAG_IMAGE_PATH);
      return path == null || path.isEmpty() ? DEFAULT_IMAGE.getPath() : path;
   }

   public static void setPlayerImage(Player player, String imagePath) {
      player.getPersistentData().putString(TAG_IMAGE_PATH, imagePath != null ? imagePath : DEFAULT_IMAGE.getPath());
   }
}
