package com.whitefood.moderncod.event;

import com.whitefood.moderncod.networking.DamageIndicatorMessage;
import com.whitefood.moderncod.networking.ModMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod")
public class HurtHandler {
   public HurtHandler() {
   }

   @SubscribeEvent
   public static void onLivingDamage(LivingIncomingDamageEvent event) {
      DamageSource source = event.getSource();
      Entity attacker = source.getEntity();
      if (attacker instanceof LivingEntity && event.getEntity() instanceof ServerPlayer target) {
         Vec3 attackerPos = attacker.position();
         DamageIndicatorMessage.DamageIndicator indicator = new DamageIndicatorMessage.DamageIndicator();
         indicator.x = (float)attackerPos.x;
         indicator.z = (float)attackerPos.z;
         ModMessages.sendToPlayer(indicator, target);
      }
   }
}
