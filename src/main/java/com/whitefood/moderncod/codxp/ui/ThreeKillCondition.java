package com.whitefood.moderncod.codxp.ui;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.whitefood.moderncod.codxp.KillCondition;
import com.whitefood.moderncod.event.custom.KillStreakTrackerHandler;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ThreeKillCondition implements KillCondition {
   public ThreeKillCondition() {
   }

   @Override
   public boolean matches(EntityKillByGunEvent event) {
      if (!(event.getAttacker() instanceof Player)) {
         return false;
      }

      Player player = (Player)event.getAttacker();
      return KillStreakTrackerHandler.getKillStreak(player) == 3;
   }

   @Override
   public int getXPBonus() {
      return 130;
   }

   @Override
   public boolean shouldDisplayInUI() {
      return true;
   }

   @Override
   public Optional<Component> getSpecialText() {
      return Optional.of(Component.translatable("message.moderncod.three_kill"));
   }
}
