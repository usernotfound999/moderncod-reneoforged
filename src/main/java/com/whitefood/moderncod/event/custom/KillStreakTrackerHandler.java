package com.whitefood.moderncod.event.custom;

import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.S2CKillStreakPacket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod")
public class KillStreakTrackerHandler {
   private static final Map<UUID, KillStreakTrackerHandler.PlayerKillData> playerKillData = new ConcurrentHashMap<>();

   public KillStreakTrackerHandler() {
   }

   @SubscribeEvent
   public static void onKill(LivingDeathEvent event) {
      if (event.getSource().getEntity() instanceof Player) {
         ServerPlayer serverPlayer = (ServerPlayer)event.getSource().getEntity();
         KillStreakTrackerHandler.PlayerKillData data = playerKillData.computeIfAbsent(
            serverPlayer.getUUID(), k -> new KillStreakTrackerHandler.PlayerKillData()
         );
         data.incrementKills();
         data.setLastKillTime(System.currentTimeMillis());
         int killStreak = data.getKills();
         String entityName = event.getEntity().getName().getString();
         S2CKillStreakPacket packet = new S2CKillStreakPacket(killStreak, entityName);
         ModMessages.sendToPlayer(packet, serverPlayer);
      }
   }

   @SubscribeEvent
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof Player) {
         playerKillData.remove(event.getEntity().getUUID());
      }
   }

   public static void resetKillStreak(Entity entity) {
      if (entity instanceof Player) {
         playerKillData.remove(entity.getUUID());
      }
   }

   public static int getKillStreak(Player player) {
      KillStreakTrackerHandler.PlayerKillData data = playerKillData.get(player.getUUID());
      return data != null ? data.getKills() : 0;
   }

   public static int getCurrentStreak(Player player) {
      KillStreakTrackerHandler.PlayerKillData data = playerKillData.get(player.getUUID());
      return data != null ? data.getKills() : 0;
   }

   private static class PlayerKillData {
      private int kills;
      private long lastKillTime;

      private PlayerKillData() {
      }

      public void incrementKills() {
         this.kills++;
      }

      public int getKills() {
         if (System.currentTimeMillis() - this.lastKillTime > 30000L) {
            this.kills = 0;
         }

         return this.kills;
      }

      public void setLastKillTime(long time) {
         this.lastKillTime = time;
      }
   }
}
