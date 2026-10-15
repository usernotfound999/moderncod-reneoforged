package com.whitefood.moderncod.event.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber(modid = "moderncod")
public class LimitGunsCountEvent {
   public LimitGunsCountEvent() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (event instanceof PlayerTickEvent.Post && !event.getEntity().level().isClientSide && !event.getEntity().isCreative()) {
         Player player = event.getEntity();
         Inventory inventory = player.getInventory();
         Item restrictedItem = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun"));
         if (restrictedItem != null) {
            int maxAllowed = 3;
            int itemCount = 0;

            for (int i = 0; i < inventory.getContainerSize(); i++) {
               ItemStack stack = inventory.getItem(i);
               if (!stack.isEmpty() && stack.getItem() == restrictedItem) {
                  if (++itemCount > maxAllowed) {
                     dropExcessItem(player, stack, i);
                  }
               }
            }
         }
      }
   }

   private static void dropExcessItem(Player player, ItemStack stack, int slot) {
      player.drop(stack, true);
      player.getInventory().setItem(slot, ItemStack.EMPTY);
   }
}
