package com.whitefood.moderncod.codxp.other;

import com.whitefood.moderncod.codxp.KillCondition;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;

public class AxeKillCondition implements KillCondition {
   public AxeKillCondition() {
   }

   @Override
   public boolean matches(ServerPlayer killer, LivingEntity victim, DamageSource source) {
      return killer.getMainHandItem().getItem() instanceof AxeItem;
   }

   @Override
   public int getXPBonus() {
      return 150;
   }

   @Override
   public Optional<Component> getSpecialText() {
      return Optional.of(Component.translatable("message.moderncod.axe_kill"));
   }

   @Override
   public boolean shouldDisplayInUI() {
      return false;
   }
}
