package com.whitefood.moderncod.client;

import com.whitefood.moderncod.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientPacketHandler {
   public ClientPacketHandler() {
   }

   public static void handlePlayerSounds(double x, double y, double z, SoundEvent sound) {
      Level world = Minecraft.getInstance().level;
      Player player = Minecraft.getInstance().player;
      if (world != null && player != null) {
         ModSounds.playCustomSoundToPlayer(player, world, x, y, z, sound);
      }
   }
}
