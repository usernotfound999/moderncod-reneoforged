package com.whitefood.moderncod.gameroom.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class RespawnUtil {
   public RespawnUtil() {
   }

   public static void teleportToWorldSpawnAndSetRespawn(ServerPlayer player) {
      if (player != null) {
         Level world = player.serverLevel().getLevel();
         BlockPos worldSpawnPos = world.getSharedSpawnPos();
         float worldSpawnAngle = world.getSharedSpawnAngle();
         teleportToWorldSpawn(player, worldSpawnPos, worldSpawnAngle);
         setPlayerRespawnToWorldSpawn(player, world, worldSpawnPos, worldSpawnAngle);
      }
   }

   private static void teleportToWorldSpawn(ServerPlayer player, BlockPos spawnPos, float spawnAngle) {
      double x = spawnPos.getX() + 0.5;
      double y = spawnPos.getY() + 1;
      double z = spawnPos.getZ() + 0.5;
      player.teleportTo(player.serverLevel().getLevel(), x, y, z, 0.0F, 0.0F);
   }

   private static void setPlayerRespawnToWorldSpawn(ServerPlayer player, Level world, BlockPos spawnPos, float spawnAngle) {
      player.setRespawnPosition(world.dimension(), spawnPos, spawnAngle, true, false);
   }
}
