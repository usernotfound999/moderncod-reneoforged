package com.whitefood.moderncod.codxp;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class DefaultKillCondition implements KillCondition {
   public DefaultKillCondition() {
   }

   @Override
   public boolean matches(EntityKillByGunEvent event) {
      return true;
   }

   @Override
   public int getXPBonus() {
      return 100;
   }

   @Override
   public boolean shouldDisplayInUI() {
      return false;
   }

   @Override
   public Optional<Component> getSpecialText() {
      return Optional.empty();
   }
}
