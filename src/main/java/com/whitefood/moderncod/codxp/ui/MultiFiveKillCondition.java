package com.whitefood.moderncod.codxp.ui;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.whitefood.moderncod.codxp.KillCondition;
import com.whitefood.moderncod.event.custom.KillStreakTrackerHandler;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class MultiFiveKillCondition implements KillCondition {
   int streak;

   public MultiFiveKillCondition() {
   }

   @Override
   public boolean matches(EntityKillByGunEvent event) {
      if (!(event.getAttacker() instanceof Player)) {
         return false;
      }

      Player player = (Player)event.getAttacker();
      this.streak = KillStreakTrackerHandler.getKillStreak(player);
      return this.streak >= 5 && this.streak % 5 == 0;
   }

   @Override
   public int getXPBonus() {
      return 150;
   }

   @Override
   public boolean shouldDisplayInUI() {
      return true;
   }

   @Override
   public Optional<Component> getSpecialText() {
      return Optional.of(Component.translatable("message.moderncod.multi_five_kill", new Object[]{this.streak}).withStyle(ChatFormatting.GOLD));
   }
}
