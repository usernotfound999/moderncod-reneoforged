package com.whitefood.moderncod.codxp;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class HeadshotKillCondition implements KillCondition {
   public HeadshotKillCondition() {
   }

   @Override
   public boolean matches(EntityKillByGunEvent event) {
      return event.isHeadShot();
   }

   @Override
   public int getXPBonus() {
      return 120;
   }

   @Override
   public boolean shouldDisplayInUI() {
      return false;
   }

   @Override
   public Optional<Component> getSpecialText() {
      return Optional.of(Component.translatable("message.moderncod.head_kill"));
   }
}
