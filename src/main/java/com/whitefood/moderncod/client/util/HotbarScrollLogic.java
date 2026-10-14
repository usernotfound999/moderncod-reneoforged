package com.whitefood.moderncod.client.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class HotbarScrollLogic {
   public HotbarScrollLogic() {
   }

   public static boolean tryScrollHotbar(Player player, double scrollDelta) {
      Inventory inventory = player.getInventory();
      int selected = inventory.selected;
      int hotbarSize = 9;
      boolean allAir = true;

      for (int i = 0; i < hotbarSize; i++) {
         if (!inventory.getItem(i).isEmpty()) {
            allAir = false;
            break;
         }
      }

      if (allAir) {
         return false;
      }

      int direction = scrollDelta > 0.0 ? -1 : 1;

      for (int i = 1; i <= hotbarSize; i++) {
         int newIndex = Math.floorMod(selected + direction * i, hotbarSize);
         if (!inventory.getItem(newIndex).isEmpty()) {
            inventory.selected = newIndex;
            return true;
         }
      }

      return false;
   }
}
