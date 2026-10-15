package com.whitefood.moderncod.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class ClientArmorHitHandler {
   private static long showUntil = 0L;
   private static long showUntilBroken = 0L;
   private static final int DISPLAY_TIME = 500;
   private static final int DISPLAY_TIME_BROKEN = 700;
   public static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/shield.png");
   public static final ResourceLocation BROKEN_ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/shield_broken.png");
   private static float currentRotationAngle = 0.0F;

   public ClientArmorHitHandler() {
   }

   public static void showArmorHitIndicator() {
      showUntil = System.currentTimeMillis() + 500L;
      currentRotationAngle = -15.0F + (float)(Math.random() * 30.0);
   }

   public static void showArmorBrokenIndicator() {
      showUntilBroken = System.currentTimeMillis() + 700L;
      currentRotationAngle = -15.0F + (float)(Math.random() * 30.0);
   }

   public static boolean shouldRender() {
      return System.currentTimeMillis() < showUntil;
   }

   public static boolean shouldRenderBroken() {
      return System.currentTimeMillis() < showUntilBroken;
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      if (player != null) {
         renderIndicator(event.getGuiGraphics());
      }
   }

   public static void renderIndicator(GuiGraphics gui) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         Window window = mc.getWindow();
         int screenWidth = window.getGuiScaledWidth();
         int screenHeight = window.getGuiScaledHeight();
         int iconSize = 16;
         int x = screenWidth / 2 - iconSize / 2;
         int y = screenHeight / 2 - iconSize + 25;
         if (shouldRenderBroken()) {
            renderBrokenIndicator(gui, x, y, iconSize);
         } else if (shouldRender()) {
            renderNormalIndicator(gui, x, y, iconSize);
         }
      }
   }

   public static void renderNormalIndicator(GuiGraphics gui, int x, int y, int iconSize) {
      PoseStack poseStack = gui.pose();
      poseStack.pushPose();
      poseStack.translate(x + iconSize / 2, y + iconSize / 2, 0.0F);
      Matrix4f rotationMatrix = new Matrix4f();
      rotationMatrix.rotationZ((float)Math.toRadians(currentRotationAngle));
      poseStack.last().pose().mul(rotationMatrix);
      poseStack.scale(0.7F, 0.7F, 1.0F);
      poseStack.translate(-(x + iconSize / 2), -(y + iconSize / 2), 0.0F);
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, ICON_TEXTURE);
      gui.blit(ICON_TEXTURE, x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
      RenderSystem.disableBlend();
      poseStack.popPose();
   }

   public static void renderBrokenIndicator(GuiGraphics gui, int x, int y, int iconSize) {
      PoseStack poseStack = gui.pose();
      poseStack.pushPose();
      poseStack.translate(x + iconSize / 2, y + iconSize / 2, 0.0F);
      Matrix4f rotationMatrix = new Matrix4f();
      rotationMatrix.rotationZ((float)Math.toRadians(currentRotationAngle));
      poseStack.last().pose().mul(rotationMatrix);
      poseStack.scale(0.7F, 0.7F, 1.0F);
      poseStack.translate(-(x + iconSize / 2), -(y + iconSize / 2), 0.0F);
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, BROKEN_ICON_TEXTURE);
      gui.blit(BROKEN_ICON_TEXTURE, x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
      RenderSystem.disableBlend();
      poseStack.popPose();
   }
}
