package com.whitefood.moderncod.gameroom.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.whitefood.moderncod.screen.ImageSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;

public class PlayerMainGUI extends Screen {
   private static final float WIDTH_RATIO = 0.6F;
   private static final float HEIGHT_RATIO = 0.7F;
   private static final float SPLIT_RATIO = 0.4F;
   private int currentWidth;
   private int currentHeight;
   private int currentSplitWidth;

   public PlayerMainGUI() {
      super(Component.literal("玩家主菜单"));
   }

   protected void init() {
      this.calculateDimensions();
      int x = (this.width - this.currentWidth) / 2;
      int y = (this.height - this.currentHeight) / 2;
      int buttonAreaX = x + this.currentSplitWidth + (int)(this.currentWidth * 0.02F);
      int buttonWidth = this.currentWidth - this.currentSplitWidth - (int)(this.currentWidth * 0.04F);
      int buttonHeight = (int)(this.currentHeight * 0.08F);
      int buttonSpacing = (int)(this.currentHeight * 0.03F);
      int buttonY = y + (int)(this.currentHeight * 0.05F);
      this.addRenderableWidget(
         Button.builder(Component.literal("房间列表"), button -> this.openRoomListGUI()).bounds(buttonAreaX, buttonY, buttonWidth, buttonHeight).build()
      );
      buttonY += buttonHeight + buttonSpacing;
      this.addRenderableWidget(
         Button.builder(Component.literal("枪械配装"), button -> this.handleGunLoadout())
            .bounds(buttonAreaX, buttonY, buttonWidth, buttonHeight)
            .build()
      );
      buttonY += buttonHeight + buttonSpacing;
      this.addRenderableWidget(
         Button.builder(Component.literal("名牌选择"), button -> this.openNameplateSelection())
            .bounds(buttonAreaX, buttonY, buttonWidth, buttonHeight)
            .build()
      );
      buttonY += buttonHeight + buttonSpacing;
      this.addRenderableWidget(
         Button.builder(Component.literal("数据统计"), button -> this.handleStatistics())
            .bounds(buttonAreaX, buttonY, buttonWidth, buttonHeight)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("关闭菜单"), button -> this.onClose())
            .bounds(buttonAreaX, y + this.currentHeight - (int)(this.currentHeight * 0.1F), buttonWidth, buttonHeight)
            .build()
      );
   }

   private void calculateDimensions() {
      this.currentWidth = (int)(this.width * 0.6F);
      this.currentHeight = (int)(this.height * 0.7F);
      this.currentSplitWidth = (int)(this.currentWidth * 0.4F);
      this.currentWidth = Math.max(this.currentWidth, 50);
      this.currentHeight = Math.max(this.currentHeight, 35);
      this.currentSplitWidth = Math.max(this.currentSplitWidth, 20);
   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(gui, mouseX, mouseY, partialTick);
      int x = (this.width - this.currentWidth) / 2;
      int y = (this.height - this.currentHeight) / 2;
      gui.fill(x, y, x + this.currentSplitWidth, y + this.currentHeight, -870704614);
      gui.fill(x + this.currentSplitWidth, y, x + this.currentSplitWidth + 2, y + this.currentHeight, -11184811);
      int titleFontSize = (int)(this.currentHeight * 0.05F);
      gui.drawCenteredString(this.font, "玩家菜单", x + this.currentWidth / 2, y + (int)(this.currentHeight * 0.02F), 16777215);
      this.renderPlayerModel(gui, x, y, partialTick);
      super.render(gui, mouseX, mouseY, partialTick);
   }

   private void renderPlayerModel(GuiGraphics gui, int x, int y, float partialTick) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         int modelX = x + this.currentSplitWidth / 2;
         int modelY = y + this.currentHeight + (int)(this.currentHeight * 0.08F) - 20;
         float modelScale = this.currentHeight * 0.4F;
         PoseStack poseStack = gui.pose();
         poseStack.pushPose();
         poseStack.translate(modelX, modelY, 50.0F);
         poseStack.scale(-modelScale, modelScale, modelScale);
         Quaternionf rotation = new Quaternionf().rotateY((float)Math.toRadians(0.0)).rotateX((float)Math.toRadians(0.0)).rotateZ((float)Math.toRadians(180.0));
         poseStack.mulPose(rotation);
         EntityRenderDispatcher renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
         renderDispatcher.setRenderShadow(false);
         renderDispatcher.render(player, 0.0, 0.0, 0.0, 180.0F, partialTick, poseStack, gui.bufferSource(), 15728880);
         renderDispatcher.setRenderShadow(true);
         poseStack.popPose();
         gui.flush();
      }
   }

   public void resize(Minecraft minecraft, int width, int height) {
      super.resize(minecraft, width, height);
      this.init();
   }

   private void openRoomListGUI() {
      Minecraft.getInstance().setScreen(new PlayerGameRoomGUI());
   }

   private void handleGunLoadout() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         player.sendSystemMessage(Component.literal("枪械配装功能即将开放"));
      }
   }

   private void openNameplateSelection() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         ImageSelectionScreen.open();
      }
   }

   private void handleStatistics() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         player.sendSystemMessage(Component.literal("数据统计功能即将开放"));
      }
   }

   public void onClose() {
      Minecraft.getInstance().setScreen(null);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
