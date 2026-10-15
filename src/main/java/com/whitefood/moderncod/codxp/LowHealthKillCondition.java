package com.whitefood.moderncod.codxp;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class LowHealthKillCondition implements KillCondition {
   public LowHealthKillCondition() {
   }

   @Override
   public boolean matches(EntityKillByGunEvent event) {
      return event.getAttacker() instanceof Player living ? living.getHealth() / living.getMaxHealth() < 0.2F : false;
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
      return Optional.of(Component.translatable("message.moderncod.survivor_kill"));
   }
}
