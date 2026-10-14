package com.whitefood.moderncod.client;

import com.whitefood.moderncod.networking.packet.S2CKillXPMessagePacket;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class KillXPHandler {
   private static int accumulatedXP = 0;
   private static final Map<Component, KillXPHandler.SpecialTextEntry> specialTexts = new LinkedHashMap<>();
   private static long lastKillTime = 0L;
   private static float alpha = 1.0F;
   private static long startTime = 0L;

   public KillXPHandler() {
   }

   public static void onKillXPReceived(S2CKillXPMessagePacket packet) {
      int xpAmount = packet.getXpAmount();
      int conditionCount = packet.getSpecialTexts().size() + 1;
      int adjustedXP = xpAmount - (conditionCount - 1) * 100;
      if (System.currentTimeMillis() - lastKillTime < 4000L) {
         accumulatedXP += adjustedXP;
      } else {
         accumulatedXP = adjustedXP;
         specialTexts.clear();
      }

      for (Component text : packet.getSpecialTexts()) {
         if (specialTexts.containsKey(text)) {
            specialTexts.remove(text);
         }

         specialTexts.put(text, new KillXPHandler.SpecialTextEntry(text));
      }

      lastKillTime = System.currentTimeMillis();
      startTime = System.currentTimeMillis();
      alpha = 1.0F;
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      GuiGraphics guiGraphics = event.getGuiGraphics();
      int screenWidth = guiGraphics.guiWidth();
      int screenHeight = guiGraphics.guiHeight();
      float scale = 0.7F;
      int baseX = screenWidth / 2 + 10;
      int baseY = screenHeight / 2 + 7;
      int scaledBaseX = (int)(baseX / scale);
      int scaledBaseY = (int)(baseY / scale);
      updateSpecialTexts();
      long elapsed = System.currentTimeMillis() - startTime;
      if (elapsed < 3000L) {
         alpha = 1.0F;
      } else {
         if (elapsed >= 3500L) {
            alpha = 0.0F;
            return;
         }

         alpha = Math.max(0.2F, 1.0F - (float)(elapsed - 3000L) / 500.0F);
      }

      if (alpha > 0.2F) {
         guiGraphics.pose().pushPose();
         guiGraphics.pose().scale(scale, scale, 1.0F);
         int color = 16777215 | (int)(alpha * 255.0F) << 24;
         Component xpMessage = Component.literal("+XP " + accumulatedXP).withStyle(ChatFormatting.YELLOW);
         guiGraphics.drawString(Minecraft.getInstance().font, xpMessage, scaledBaseX, scaledBaseY, color);
         renderSpecialTexts(guiGraphics, scaledBaseX, scaledBaseY + 10, color);
         guiGraphics.pose().popPose();
      }
   }

   private static void updateSpecialTexts() {
      Iterator<Entry<Component, KillXPHandler.SpecialTextEntry>> iterator = specialTexts.entrySet().iterator();
      long currentTime = System.currentTimeMillis();

      while (iterator.hasNext()) {
         Entry<Component, KillXPHandler.SpecialTextEntry> entry = iterator.next();
         KillXPHandler.SpecialTextEntry textEntry = entry.getValue();
         long elapsed = currentTime - textEntry.startTime;
         if (elapsed < 3000L) {
            textEntry.alpha = 1.0F;
         } else if (elapsed < 3500L) {
            textEntry.alpha = Math.max(0.2F, 1.0F - (float)(elapsed - 3000L) / 500.0F);
         } else {
            iterator.remove();
         }
      }
   }

   private static void renderSpecialTexts(GuiGraphics guiGraphics, int x, int y, int color) {
      int previousY = y;

      for (KillXPHandler.SpecialTextEntry entry : specialTexts.values()) {
         int targetY = previousY;
         if (entry.yOffset == 0) {
            entry.yOffset = previousY;
         } else {
            int diff = targetY - entry.yOffset;
            if (diff != 0) {
               int step = Integer.signum(diff);
               int moveAmount = Math.min(Math.abs(diff), 3);
               entry.yOffset += step * moveAmount;
            }
         }

         int textColor = 16777215 | (int)(entry.alpha * 255.0F) << 24;
         guiGraphics.drawString(Minecraft.getInstance().font, entry.text, x + 10, entry.yOffset, textColor);
         previousY = targetY + 10;
      }
   }

   private static class SpecialTextEntry {
      final Component text;
      int yOffset;
      float alpha = 1.0F;
      final long startTime;

      SpecialTextEntry(Component text) {
         this.text = text;
         this.yOffset = 0;
         this.startTime = System.currentTimeMillis();
      }
   }
}
