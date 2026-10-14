package com.whitefood.moderncod.codxp;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class AerialKillCondition implements KillCondition {
   public AerialKillCondition() {
   }

   @Override
   public boolean matches(EntityKillByGunEvent event) {
      Entity killedEntity = event.getKilledEntity();
      return killedEntity != null && !killedEntity.onGround();
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
      return Optional.of(Component.translatable("message.moderncod.aeria_kill"));
   }
}
