package com.whitefood.moderncod.item.custom;

import com.whitefood.moderncod.item.BaseItemShield;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemSmallShield extends BaseItemShield {
   public ItemSmallShield(Properties properties) {
      super(properties);
   }

   @Override
   public boolean canStartUsing(Player player) {
      return player.getAbsorptionAmount() < 60.0F;
   }

   @Override
   protected void onUseCompleted(Level level, Player player, ItemStack stack) {
      float current = player.getAbsorptionAmount();
      float newAmount = Math.min(current + 20.0F, 60.0F);
      player.setAbsorptionAmount(newAmount);
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }
   }

   @Override
   public String getNBTKey() {
      return "IsUsingSmallShield";
   }
}
