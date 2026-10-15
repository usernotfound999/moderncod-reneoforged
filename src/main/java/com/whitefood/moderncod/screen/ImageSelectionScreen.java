package com.whitefood.moderncod.screen;

import com.whitefood.moderncod.gameroom.client.PlayerMainGUI;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.C2SPlayerImagePacket;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ImageSelectionScreen extends Screen {
   private static final List<String> AVAILABLE_IMAGES = Arrays.asList(
      "textures/gui/default_image.png",
      "textures/gui/image1.png",
      "textures/gui/image2.png",
      "textures/gui/image3.png",
      "textures/gui/image4.png",
      "textures/gui/image5.png",
      "textures/gui/image6.png",
      "textures/gui/image7.png",
      "textures/gui/image8.png",
      "textures/gui/image9.png",
      "textures/gui/image10.png",
      "textures/gui/image11.png",
      "textures/gui/image12.png",
      "textures/gui/image13.png"
   );
   private static final int IMAGE_HIGH = 32;
   private static final int IMAGE_WIDTH = 128;
   private static final int PADDING = 10;
   private static final int SCROLL_SPEED = 10;
   private int scrollOffset = 0;
   private int selectedImageIndex = -1;

   protected ImageSelectionScreen() {
      super(Component.literal("Select Your Image"));
   }

   protected void init() {
      super.init();
      int buttonWidth = 100;
      int buttonHeight = 20;
      int rightPanelX = this.width / 2 + 100;
      this.addRenderableWidget(Button.builder(Component.literal("确定选中"), button -> {
         if (this.selectedImageIndex >= 0 && this.selectedImageIndex < AVAILABLE_IMAGES.size()) {
            String imagePath = AVAILABLE_IMAGES.get(this.selectedImageIndex);
            Minecraft.getInstance().tell(() -> {
               Player player = Minecraft.getInstance().player;
               ModMessages.sendToServer(new C2SPlayerImagePacket(player.getGameProfile().getName(), imagePath));
            });
            this.onClose();
         }
      }).bounds(rightPanelX, this.height / 2 + 20, buttonWidth, buttonHeight).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("关闭菜单"), button -> this.onClose())
            .bounds(rightPanelX, this.height / 2 + 50, buttonWidth, buttonHeight)
            .build()
      );
   }

   public void onClose() {
      super.onClose();
      Minecraft.getInstance().setScreen(new PlayerMainGUI());
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      this.scrollOffset += (int)(-scrollY * 10.0);
      this.scrollOffset = Math.max(this.scrollOffset, 0);
      return true;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int gridX = this.width / 2 - 200;
         int gridY = 50 - this.scrollOffset;

         for (int i = 0; i < AVAILABLE_IMAGES.size(); i++) {
            int row = i / 2;
            int col = i % 2;
            int x = gridX + col * 138;
            int y = gridY + row * 42;
            if (mouseX >= x && mouseX <= x + 128 && mouseY >= y && mouseY <= y + 32) {
               this.selectedImageIndex = i;
               break;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
      int gridX = this.width / 2 - 200;
      int gridY = 50 - this.scrollOffset;

      for (int i = 0; i < AVAILABLE_IMAGES.size(); i++) {
         int row = i / 2;
         int col = i % 2;
         int x = gridX + col * 138;
         int y = gridY + row * 42;
         ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("moderncod", AVAILABLE_IMAGES.get(i));
         guiGraphics.blit(texture, x, y, 0.0F, 0.0F, 128, 32, 128, 32);
         if (i == this.selectedImageIndex) {
            guiGraphics.fill(x - 2, y - 2, x + 128 + 2, y + 32 + 2, -2130706688);
         }
      }

      int rightPanelX = this.width / 2 + 100;
      if (this.selectedImageIndex >= 0 && this.selectedImageIndex < AVAILABLE_IMAGES.size()) {
         ResourceLocation selectedTexture = ResourceLocation.fromNamespaceAndPath("moderncod", AVAILABLE_IMAGES.get(this.selectedImageIndex));
         guiGraphics.blit(selectedTexture, rightPanelX, this.height / 2 - 80, 0.0F, 0.0F, 128, 32, 128, 32);
      }

      super.render(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public static void open() {
      Minecraft mc = Minecraft.getInstance();
      if (!(mc.screen instanceof ImageSelectionScreen)) {
         mc.setScreen(new ImageSelectionScreen());
      }
   }
}
