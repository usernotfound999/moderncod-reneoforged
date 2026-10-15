package com.whitefood.moderncod.gameroom.handler;

import com.whitefood.moderncod.gameroom.manager.TeamManager;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class FriendlyFireHandler {
   public static TeamManager teamManager;

   public FriendlyFireHandler() {
   }

   public static void setTeamManager(TeamManager manager) {
      teamManager = manager;
   }

   @SubscribeEvent
   public static void onLivingHurt(LivingIncomingDamageEvent event) {
      Entity source = event.getSource().getEntity();
      LivingEntity target = event.getEntity();
      if (source instanceof Player && target instanceof Player && teamManager != null) {
         UUID attackerId = source.getUUID();
         UUID victimId = target.getUUID();
         String teamA = teamManager.getPlayerTeam(attackerId);
         String teamB = teamManager.getPlayerTeam(victimId);
         if (teamA != null && teamA.equals(teamB)) {
            event.setCanceled(true);
         }
      }
   }
}
