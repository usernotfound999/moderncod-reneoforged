package com.whitefood.moderncod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.whitefood.moderncod.item.BaseItemShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class ShieldHUDOverlay {
   public ShieldHUDOverlay() {
   }

   @SubscribeEvent
   public static void onRenderHUD(Post event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null && mc.level != null) {
         ItemStack heldStack = getHeldShieldItem(player);
         if (heldStack != null) {
            BaseItemShield shieldItem = (BaseItemShield)heldStack.getItem();
            String nbtKey = shieldItem.getNBTKey();
            CompoundTag tag = player.getPersistentData();
            boolean isUsing = tag.getBoolean(nbtKey);
            if (isUsing) {
               long startTime = tag.getLong("ShieldStartTime");
               int duration = 44;
               long elapsed = mc.level.getGameTime() - startTime;
               float progress = Mth.clamp((float)elapsed / duration, 0.0F, 1.0F);
               float timeLeft = Mth.clamp((float)(duration - elapsed) / 20.0F, 0.0F, duration / 20.0F);
               renderProgressBar(event.getGuiGraphics(), progress, timeLeft, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
            } else if (shieldItem.canStartUsing(player)) {
               renderUseTip(event.getGuiGraphics(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
            }
         }
      }
   }

   private static ItemStack getHeldShieldItem(Player player) {
      ItemStack main = player.getMainHandItem();
      return main.getItem() instanceof BaseItemShield ? main : null;
   }

   private static void renderProgressBar(GuiGraphics guiGraphics, float progress, float timeLeft, int screenWidth, int screenHeight) {
      PoseStack poseStack = guiGraphics.pose();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      int barWidth = 100;
      int barHeight = 8;
      int x = (screenWidth - barWidth) / 2;
      int y = screenHeight / 2 + 40;
      guiGraphics.fill(x, y, x + barWidth, y + barHeight, -1442840576);
      guiGraphics.fill(x, y, x + (int)(barWidth * progress), y + barHeight, -16733441);
      Minecraft mc = Minecraft.getInstance();
      Font font = mc.font;
      String text = String.format("%.1f秒", timeLeft);
      Component cancelTip = Component.translatable("hud.moderncod.shield.cancel_tip");
      guiGraphics.drawString(font, text, x - font.width(text) - 6, y + 1, 16777215, true);
      guiGraphics.drawString(font, cancelTip, x + (barWidth - font.width(cancelTip)) / 2, y - 10, 16777215, true);
      RenderSystem.disableBlend();
   }

   private static void renderUseTip(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
      Font font = Minecraft.getInstance().font;
      Component tip = Component.translatable("hud.moderncod.shield.use_tip");
      int x = (screenWidth - font.width(tip)) / 2;
      int y = screenHeight / 2 + 40;
      PoseStack poseStack = guiGraphics.pose();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.fill(x - 4, y - 2, x + font.width(tip) + 4, y + 10, 134217728);
      guiGraphics.drawString(font, tip, x, y, 16777215, true);
      RenderSystem.disableBlend();
   }
}
