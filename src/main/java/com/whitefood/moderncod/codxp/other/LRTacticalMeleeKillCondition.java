package com.whitefood.moderncod.codxp.other;

import com.whitefood.moderncod.codxp.KillCondition;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public class LRTacticalMeleeKillCondition implements KillCondition {
   private static final ResourceLocation LRTACTICAL_MELEE_ID = ResourceLocation.fromNamespaceAndPath("lrtactical", "melee");

   public LRTacticalMeleeKillCondition() {
   }

   @Override
   public boolean matches(ServerPlayer killer, LivingEntity victim, DamageSource source) {
      ItemStack itemStack = killer.getMainHandItem();
      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
      return LRTACTICAL_MELEE_ID.equals(itemId);
   }

   @Override
   public int getXPBonus() {
      return 150;
   }

   @Override
   public Optional<Component> getSpecialText() {
      return Optional.of(Component.translatable("message.moderncod.lr_melee_kill"));
   }

   @Override
   public boolean shouldDisplayInUI() {
      return false;
   }
}
