package com.whitefood.moderncod.gameroom.base;

import com.whitefood.moderncod.gameroom.manager.GameRoomManager;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = "moderncod")
public class PlayerKillListener {
   public PlayerKillListener() {
   }

   @SubscribeEvent
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer victim) {
         if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            if (!killer.getUUID().equals(victim.getUUID())) {
               UUID killerId = killer.getUUID();
               UUID victimId = victim.getUUID();
               GameRoom room = findPlayerRoom(killerId);
               if (room != null && room.isRunning() && room.hasPlayer(victimId)) {
                  if (room.getGoal() != null) {
                     room.getGoal().onPlayerKill(killer, victim, room);
                  }
               }
            }
         }
      }
   }

   private static GameRoom findPlayerRoom(UUID playerId) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server == null) {
         return null;
      }

      for (ServerLevel level : server.getAllLevels()) {
         GameRoomManager manager = GameRoomManager.getForLevel(level);

         for (GameRoom room : manager.getAllRooms()) {
            if (room.isRunning() && room.hasPlayer(playerId)) {
               return room;
            }
         }
      }

      return null;
   }
}
