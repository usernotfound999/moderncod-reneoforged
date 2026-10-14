package com.whitefood.moderncod.item.custom;

import com.whitefood.moderncod.item.BaseItemShield;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemShield extends BaseItemShield {
   public ItemShield(Properties properties) {
      super(properties);
   }

   @Override
   public boolean canStartUsing(Player player) {
      return player.getAbsorptionAmount() < 60.0F;
   }

   @Override
   protected void onUseCompleted(Level world, Player player, ItemStack stack) {
      if (player.getAbsorptionAmount() < 60.0F) {
         player.setAbsorptionAmount(60.0F);
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
      }
   }

   @Override
   public String getNBTKey() {
      return "IsUsingShield";
   }
}
