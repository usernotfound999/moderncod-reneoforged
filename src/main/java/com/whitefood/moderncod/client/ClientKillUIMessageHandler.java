package com.whitefood.moderncod.client;

import com.ibm.icu.impl.Pair;
import com.whitefood.moderncod.event.custom.PlayerImageManager;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class ClientKillUIMessageHandler {
   private static final Queue<Pair<String, Component>> messageQueue = new LinkedList<>();
   private static long displayUntil = 0L;
   private static Pair<String, Component> currentMessage = null;
   private static long animationStartTime = 0L;
   private static final long ANIMATION_DURATION = 500L;
   private static final int IMAGE_WIDTH = 128;
   private static final int IMAGE_HEIGHT = 32;
   private static final int PADDING = 10;
   private static final int TARGET_X_OFFSET = 20;
   private static Pair<String, Component> killStreakMessage = null;
   private static long killStreakMessageTime = 0L;
   private static boolean isKillStreakMessageActive = false;

   public ClientKillUIMessageHandler() {
   }

   public static void addMessages(String killerName, List<Component> messages) {
      for (Component msg : messages) {
         messageQueue.add(Pair.of(killerName, msg));
      }
   }

   public static void displayKillStreakMessage(int killStreak, String entityName) {
      String killStreakMessageText = killStreak + "杀";
      killStreakMessage = Pair.of(entityName, Component.literal(killStreakMessageText));
      killStreakMessageTime = System.currentTimeMillis() + 3000L;
      isKillStreakMessageActive = false;
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent.Post event) {
      if (event instanceof ClientTickEvent.Post) {
         tick();
      }
   }

   private static void tick() {
      if (currentMessage == null && !messageQueue.isEmpty()) {
         currentMessage = messageQueue.poll();
         animationStartTime = System.currentTimeMillis();
         displayUntil = System.currentTimeMillis() + 5000L;
      } else if (currentMessage != null && System.currentTimeMillis() > displayUntil) {
         currentMessage = null;
      }

      if (System.currentTimeMillis() > killStreakMessageTime) {
         isKillStreakMessageActive = false;
      }
   }

   @SubscribeEvent
   public static void onRenderGui(Post event) {
      render(event.getGuiGraphics());
   }

   @SubscribeEvent
   public static void onRenderGuiPre(Pre event) {
      if (isKillStreakMessageActive && event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         event.setCanceled(true);
      }

      if (isKillStreakMessageActive && event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR)) {
         event.setCanceled(true);
      }

      if (isKillStreakMessageActive && event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)) {
         event.setCanceled(true);
      }
   }

   private static void render(GuiGraphics guiGraphics) {
      Minecraft mc = Minecraft.getInstance();
      Font font = mc.font;
      int screenWidth = mc.getWindow().getGuiScaledWidth();
      int screenHeight = mc.getWindow().getGuiScaledHeight();
      long currentTime = System.currentTimeMillis();
      int targetX = screenWidth - 128 - 20;
      if (currentMessage != null) {
         if (currentTime - animationStartTime < 500L) {
            float animationProgress = (float)(currentTime - animationStartTime) / 500.0F;
            float easedProgress = easeOutCubic(animationProgress);
            int startX = screenWidth;
            int distance = targetX - startX;
            int currentX = startX + (int)(distance * easedProgress);
            renderElements(guiGraphics, font, currentX, screenHeight / 4, targetX);
         } else if (displayUntil - currentTime < 500L) {
            float animationProgress = (float)(displayUntil - currentTime) / 500.0F;
            float easedProgress = easeInCubic(animationProgress);
            int endX = screenWidth;
            int distance = endX - targetX;
            int currentX = targetX + (int)(distance * (1.0F - easedProgress));
            renderElements(guiGraphics, font, currentX, screenHeight / 4, targetX);
         } else {
            renderElements(guiGraphics, font, targetX, screenHeight / 4, targetX);
         }
      }

      if (killStreakMessage != null && currentTime < killStreakMessageTime) {
         float scale = 0.6F;
         int scaledImageWidth = (int)(128.0F * scale);
         int scaledImageHeight = (int)(32.0F * scale);
         int totalHeight = 9 * 2 + 2 + scaledImageHeight;
         int startY = screenHeight - totalHeight - 10;
         int messageX = (screenWidth - font.width((FormattedText)killStreakMessage.second)) / 2;
         renderKillStreakMessage(guiGraphics, font, messageX, startY, scale);
      }
   }

   private static void renderKillStreakMessage(GuiGraphics guiGraphics, Font font, int x, int y, float scale) {
      Minecraft mc = Minecraft.getInstance();
      int screenWidth = mc.getWindow().getGuiScaledWidth();
      int screenHeight = mc.getWindow().getGuiScaledHeight();
      String killerName = (String)killStreakMessage.first;
      Component messageComponent = (Component)killStreakMessage.second;
      guiGraphics.drawString(font, messageComponent, x, y, 16777215, true);
      y += 9 + 2;
      int scaledImageWidth = (int)(128.0F * scale);
      int scaledImageHeight = (int)(32.0F * scale);
      int imageX = (screenWidth - scaledImageWidth) / 2;
      int imageY = screenHeight - scaledImageHeight - 10;
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(imageX, imageY, 0.0F);
      guiGraphics.pose().scale(scale, scale, 1.0F);
      renderPlayerImageForKillStreak(guiGraphics, killerName, 0, 0);
      guiGraphics.pose().popPose();
   }

   private static void renderElements(GuiGraphics guiGraphics, Font font, int imageX, int baseY, int targetX) {
      if (currentMessage != null) {
         renderPlayerImageForQueue(guiGraphics, (String)currentMessage.first, imageX, baseY);
         String killerName = (String)currentMessage.first;
         Component messageComponent = (Component)currentMessage.second;
         int nameWidth = font.width(killerName);
         int messageWidth = font.width(messageComponent);
         int maxTextWidth = Math.max(nameWidth, messageWidth);
         int textX = targetX - maxTextWidth - 10;
         int textOffset = imageX - targetX;
         int animatedTextX = textX + textOffset;
         int nameY = baseY;
         int messageY = nameY + 9 + 2;
         guiGraphics.drawString(font, killerName, animatedTextX, nameY, 16777215, true);
         guiGraphics.drawString(font, messageComponent, animatedTextX, messageY, 16777215, true);
      }
   }

   private static float easeOutCubic(float x) {
      return (float)(1.0 - Math.pow(1.0F - x, 3.0));
   }

   private static float easeInCubic(float x) {
      return x * x * x;
   }

   private static void renderPlayerImageForQueue(GuiGraphics guiGraphics, String playerName, int x, int y) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         Player player = mc.level.players().stream().filter(p -> p.getGameProfile().getName().equals(playerName)).findFirst().orElse(null);
         String imagePath;
         if (player != null) {
            imagePath = ClientImageDataManager.getPlayerImage(player.getUUID());
         } else {
            imagePath = PlayerImageManager.DEFAULT_IMAGE.getPath();
         }

         ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("moderncod", imagePath);

         try {
            guiGraphics.blit(texture, x, y, 0.0F, 0.0F, 128, 32, 128, 32);
         } catch (Exception e) {
            guiGraphics.blit(PlayerImageManager.DEFAULT_IMAGE, x, y, 0.0F, 0.0F, 128, 32, 128, 32);
         }
      }
   }

   private static void renderPlayerImageForKillStreak(GuiGraphics guiGraphics, String playerName, int x, int y) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         Player player = mc.level.players().stream().filter(p -> p.getGameProfile().getName().equals(playerName)).findFirst().orElse(null);
         String imagePath;
         if (player != null) {
            imagePath = ClientImageDataManager.getPlayerImage(player.getUUID());
         } else {
            imagePath = PlayerImageManager.DEFAULT_IMAGE.getPath();
         }

         ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("moderncod", imagePath);

         try {
            guiGraphics.blit(texture, x, y, 0.0F, 0.0F, 128, 32, 128, 32);
         } catch (Exception e) {
            guiGraphics.blit(PlayerImageManager.DEFAULT_IMAGE, x, y, 0.0F, 0.0F, 128, 32, 128, 32);
         }
      }
   }
}
