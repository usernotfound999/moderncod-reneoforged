package com.whitefood.moderncod.screen;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class HUDRenderer {
   private static final Minecraft mc = Minecraft.getInstance();
   private static int lastSelectedSlot = -1;
   private static float animationOffset = 0.0F;

   public HUDRenderer() {
   }

   @SubscribeEvent
   public static void onRenderOverlay(Post event) {
      if (mc.player != null && mc.level != null) {
         GuiGraphics graphics = event.getGuiGraphics();
         int screenWidth = mc.getWindow().getGuiScaledWidth();
         int screenHeight = mc.getWindow().getGuiScaledHeight();
         renderFourthSlotItem(graphics, screenWidth, screenHeight);
         renderGunHUDs(graphics, screenWidth, screenHeight);
      }
   }

   private static void renderFourthSlotItem(GuiGraphics graphics, int screenWidth, int screenHeight) {
      LocalPlayer player = mc.player;
      ItemStack slot4 = (ItemStack)player.getInventory().items.get(3);
      if (!slot4.isEmpty()) {
         int x = 120;
         int y = screenHeight - 40;
         int slotSize = 16;
         int bgPadding = 2;
         int bgX = x - bgPadding;
         int bgY = y - bgPadding;
         int bgSize = slotSize + bgPadding * 2;
         graphics.fill(bgX, bgY, bgX + bgSize, bgY + bgSize, 100663296);
         graphics.renderItem(slot4, x, y);
         renderKeyLabelBelowItem(graphics, "4", x, y, 16);
      }
   }

   private static void renderKeyLabelBelowItem(GuiGraphics graphics, String text, int x, int y, int itemSize) {
      Font font = Minecraft.getInstance().font;
      int width = font.width(text);
      int fontHeight = 9;
      int padding = 3;
      int textX = x + (itemSize - width) / 2;
      int textY = y + itemSize + 3;
      graphics.fill(textX - padding, textY - padding, textX + width + padding, textY + fontHeight + padding, 134217728);
      graphics.drawString(font, text, textX, textY, 16777215, false);
   }

   private static void renderGunHUDs(GuiGraphics graphics, int screenWidth, int screenHeight) {
      LocalPlayer player = mc.player;
      Inventory inv = player.getInventory();
      int found = 0;
      int baseX = screenWidth - 130;
      int y = screenHeight - 70;
      int totalWidth = 115;
      int maxHeight = 13;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = (ItemStack)inv.items.get(i);
         if (stack.getItem() instanceof IGun gunItem) {
            ResourceLocation gunId = gunItem.getGunId(stack);
            if (gunId != null) {
               Optional<ClientGunIndex> opt = TimelessAPI.getClientGunIndex(gunId);
               if (opt.isPresent()) {
                  GunDisplayInstance display = opt.get().getDefaultDisplay();
                  if (display != null && display.getHUDTexture() != null) {
                     ResourceLocation tex = display.getHUDTexture();
                     int gunX = baseX + found * 65;
                     int bgX = gunX - 1;
                     int bgY = y - 1;
                     int bgW = 41;
                     int bgH = 15;
                     graphics.fill(bgX, bgY, bgX + bgW, bgY + bgH, 100663296);
                     graphics.blit(tex, gunX, y, 0.0F, 0.0F, 39, 13, 39, 13);
                     renderKeyLabel(graphics, String.valueOf(i + 1), gunX, y);
                     if (++found >= 2) {
                        break;
                     }
                  }
               }
            }
         }
      }

      if (found > 0) {
         int lineY = y + maxHeight + 2;
         int lineXStart = baseX - 20;
         int lineXEnd = baseX + totalWidth;
         graphics.fill(lineXStart, lineY, lineXEnd, lineY + 1, -1);
      }
   }

   private static void renderKeyLabel(GuiGraphics graphics, String text, int gunX, int gunY) {
      Font font = Minecraft.getInstance().font;
      int width = font.width(text);
      int fontHeight = 9;
      int padding = 3;
      int textX = gunX - width - 5 - padding;
      int textY = gunY + (13 - fontHeight) / 2;
      graphics.fill(textX - padding, textY - padding, textX + width + padding, textY + fontHeight + padding, 134217728);
      graphics.drawString(font, text, textX, textY, 16777215, false);
   }

   private static void renderAdditionalHotbarSlots(GuiGraphics graphics, int screenWidth, int screenHeight) {
      LocalPlayer player = mc.player;
      Inventory inv = player.getInventory();
      int selected = inv.selected;
      if (lastSelectedSlot != selected) {
         animationOffset = (lastSelectedSlot - selected) * 2;
         lastSelectedSlot = selected;
      }

      if (Math.abs(animationOffset) > 0.5F) {
         animationOffset *= 0.2F;
      } else {
         animationOffset = 0.0F;
      }

      int slotSize = 16;
      int spacing = 4;
      int maxDisplay = 3;
      int totalWidth = maxDisplay * (slotSize + spacing) - spacing;
      int startX = screenWidth - 140 - totalWidth - 12 + (int)animationOffset;
      int y = screenHeight - 40;
      int displayed = 0;

      for (int offset = 0; offset < 9 && displayed < maxDisplay; offset++) {
         int slotIndex = selected + offset;
         if (slotIndex >= 9) {
            break;
         }

         ItemStack stack = (ItemStack)inv.items.get(slotIndex);
         if (!stack.isEmpty()) {
            int x = startX + (maxDisplay - 1 - displayed) * (slotSize + spacing);
            int bgPadding = 2;
            int bgX = x - bgPadding;
            int bgY = y - bgPadding;
            int bgSize = slotSize + bgPadding * 2;
            graphics.fill(bgX, bgY, bgX + bgSize, bgY + bgSize, 100663296);
            graphics.renderItem(stack, x, y);
            renderKeyLabelBelowItem(graphics, String.valueOf(slotIndex + 1), x, y, slotSize);
            if (slotIndex == selected) {
               graphics.fill(x - 2, y - 2, x + slotSize + 2, y + slotSize + 2, 117440511);
            }

            displayed++;
         }
      }
   }
}
