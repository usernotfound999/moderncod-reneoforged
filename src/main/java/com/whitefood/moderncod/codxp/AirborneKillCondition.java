package com.whitefood.moderncod.codxp;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class AirborneKillCondition implements KillCondition {
   public AirborneKillCondition() {
   }

   @Override
   public boolean matches(EntityKillByGunEvent event) {
      Entity killer = event.getAttacker();
      return killer != null && !killer.onGround();
   }

   @Override
   public int getXPBonus() {
      return 110;
   }

   @Override
   public boolean shouldDisplayInUI() {
      return false;
   }

   @Override
   public Optional<Component> getSpecialText() {
      return Optional.of(Component.translatable("message.moderncod.jump_kill"));
   }
}
