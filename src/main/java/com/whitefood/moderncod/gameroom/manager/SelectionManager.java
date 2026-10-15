package com.whitefood.moderncod.gameroom.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public class SelectionManager {
   private static final Map<UUID, BlockPos> pos1Map = new HashMap<>();
   private static final Map<UUID, BlockPos> pos2Map = new HashMap<>();

   public SelectionManager() {
   }

   public static void setPos1(ServerPlayer player, BlockPos pos) {
      pos1Map.put(player.getUUID(), pos);
   }

   public static void setPos2(ServerPlayer player, BlockPos pos) {
      pos2Map.put(player.getUUID(), pos);
   }

   @Nullable
   public static AABB getSelectedArea(ServerPlayer player) {
      BlockPos pos1 = pos1Map.get(player.getUUID());
      BlockPos pos2 = pos2Map.get(player.getUUID());
      return pos1 != null && pos2 != null ? new AABB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ()) : null;
   }

   public static void clear(ServerPlayer player) {
      pos1Map.remove(player.getUUID());
      pos2Map.remove(player.getUUID());
   }
}
