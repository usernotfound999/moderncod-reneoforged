package com.whitefood.moderncod.event.custom;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.whitefood.moderncod.codxp.AerialKillCondition;
import com.whitefood.moderncod.codxp.AirborneKillCondition;
import com.whitefood.moderncod.codxp.HeadshotKillCondition;
import com.whitefood.moderncod.codxp.KillCondition;
import com.whitefood.moderncod.codxp.LowHealthKillCondition;
import com.whitefood.moderncod.codxp.other.AxeKillCondition;
import com.whitefood.moderncod.codxp.other.BowKillCondition;
import com.whitefood.moderncod.codxp.other.LRTacticalMeleeKillCondition;
import com.whitefood.moderncod.codxp.other.SwordKillCondition;
import com.whitefood.moderncod.codxp.ui.MultiFiveKillCondition;
import com.whitefood.moderncod.codxp.ui.ThreeKillCondition;
import com.whitefood.moderncod.command.scoreboard.ModCriteriaTriggers;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.S2CKillXPMessagePacket;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.DEDICATED_SERVER)
public class KillXPServerHandler {
   private static final List<KillCondition> CONDITIONS = List.of(
      new AxeKillCondition(),
      new SwordKillCondition(),
      new LRTacticalMeleeKillCondition(),
      new BowKillCondition(),
      new MultiFiveKillCondition(),
      new ThreeKillCondition(),
      new HeadshotKillCondition(),
      new LowHealthKillCondition(),
      new AirborneKillCondition(),
      new AerialKillCondition()
   );
   private static final int DEFAULT_XP = 100;

   public KillXPServerHandler() {
   }

   @SubscribeEvent
   public static void onEntityDeath(EntityKillByGunEvent event) {
      Entity killer = event.getAttacker();
      if (killer instanceof ServerPlayer player) {
         List<Component> matchedTexts = new ArrayList<>();
         int totalXP = 100;

         for (KillCondition condition : CONDITIONS) {
            if (condition.matches(event)) {
               condition.getSpecialText().ifPresent(matchedTexts::add);
               totalXP += condition.getXPBonus();
               condition.sendKillUIMessage(event);
            }
         }

         ModMessages.sendToPlayer(new S2CKillXPMessagePacket(totalXP, matchedTexts), player);
         ModCriteriaTriggers.COD_XP.get().trigger(player, totalXP);
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer killer) {
         LivingEntity var8 = event.getEntity();
         DamageSource source = event.getSource();
         ArrayList matchedTexts = new ArrayList();
         int totalXP = 100;

         for (KillCondition condition : CONDITIONS) {
            if (condition.matches(killer, var8, source)) {
               if (condition.shouldDisplayInUI()) {
                  condition.sendKillUIMessage(killer, var8, source);
               }

               condition.getSpecialText().ifPresent(matchedTexts::add);
               totalXP += condition.getXPBonus();
            }
         }

         if (!matchedTexts.isEmpty()) {
            ModMessages.sendToPlayer(new S2CKillXPMessagePacket(totalXP, matchedTexts), killer);
            ModCriteriaTriggers.COD_XP.get().trigger(killer, totalXP);
         }
      }
   }
}
