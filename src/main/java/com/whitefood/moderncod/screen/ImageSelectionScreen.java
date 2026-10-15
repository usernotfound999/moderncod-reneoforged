package com.whitefood.moderncod.screen;

import com.whitefood.moderncod.callingcard.CallingCard;
import com.whitefood.moderncod.client.CallingCardLibrary;
import com.whitefood.moderncod.client.CallingCardTextures;
import com.whitefood.moderncod.config.ModernCodConfig;
import com.whitefood.moderncod.gameroom.client.PlayerMainGUI;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.C2SPlayerImagePacket;
import com.whitefood.moderncod.networking.packet.C2SUploadCallingCardPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Calling card (nameplate) selection screen.
 *
 * <p>Shows the built-in images plus every valid image found in {@code config/moderncod/callingcards},
 * behind a draggable / wheel-driven scrollbar. Invalid custom files are shown as a red placeholder
 * with a warning instead of ever falling back to the purple/black missing texture.</p>
 */
public class ImageSelectionScreen extends Screen {
   private static final List<String> BUILTIN_IMAGES = Arrays.asList(
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
   private static final int COLUMNS = 2;
   private static final int CELL_WIDTH = 128;
   private static final int CELL_HEIGHT = 32;
   private static final int STEP_X = 138;
   private static final int STEP_Y = 42;
   private static final int GRID_TOP = 50;
   private static final int BOTTOM_MARGIN = 40;
   private static final int GRID_LEFT_MARGIN = 200;
   private static final int SCROLL_STEP = 20;
   private static final int SELECT_COLOR = -2130706688;
   private static final int WARNING_COLOR = 0xFFFF5555;
   private static final int PLACEHOLDER_COLOR = 0x80000000;

   private final List<CardEntry> entries = new ArrayList<>();
   private int selectedIndex = -1;
   private String warning = null;
   private VerticalScrollBar scrollBar;
   private int gridX;
   private int viewportHeight;

   protected ImageSelectionScreen() {
      super(Component.literal("选择你的名片"));
   }

   @Override
   protected void init() {
      super.init();
      this.loadEntries();
      this.gridX = this.width / 2 - GRID_LEFT_MARGIN;
      int gridBottom = this.height - BOTTOM_MARGIN;
      this.viewportHeight = Math.max(CELL_HEIGHT, gridBottom - GRID_TOP);
      int contentHeight = this.rows() * STEP_Y;
      this.scrollBar = new VerticalScrollBar(
         this.gridX + COLUMNS * STEP_X + 4, GRID_TOP, 8, this.viewportHeight, contentHeight, () -> { }
      );
      this.scrollBar.visible = contentHeight > this.viewportHeight;
      this.addRenderableWidget(this.scrollBar);

      int buttonWidth = 100;
      int buttonHeight = 20;
      int rightPanelX = this.width / 2 + 100;
      int buttonY = this.height / 2 + 20;
      this.addRenderableWidget(
         Button.builder(Component.literal("确定选中"), button -> this.confirmSelection())
            .bounds(rightPanelX, buttonY, buttonWidth, buttonHeight)
            .build()
      );
      buttonY += buttonHeight + 10;
      if (ModernCodConfig.customCardsEnabled()) {
         this.addRenderableWidget(
            Button.builder(Component.literal("刷新自定义"), button -> this.refreshCustom())
               .bounds(rightPanelX, buttonY, buttonWidth, buttonHeight)
               .build()
         );
         buttonY += buttonHeight + 10;
      }
      this.addRenderableWidget(
         Button.builder(Component.literal("关闭菜单"), button -> this.onClose())
            .bounds(rightPanelX, buttonY, buttonWidth, buttonHeight)
            .build()
      );
   }

   private void loadEntries() {
      this.entries.clear();
      for (String path : BUILTIN_IMAGES) {
         this.entries.add(new CardEntry(path, null, CallingCard.builtinTexture(path), CELL_WIDTH, CELL_HEIGHT, null));
      }
      if (!ModernCodConfig.customCardsEnabled()) {
         return;
      }
      for (CallingCardLibrary.Entry file : CallingCardLibrary.entries()) {
         String error = file.error();
         ResourceLocation texture = null;
         int textureWidth = CELL_WIDTH;
         int textureHeight = CELL_HEIGHT;
         if (error == null) {
            byte[] data = file.data();
            String hash = CallingCard.hash(data);
            if (CallingCardTextures.register(hash, data)) {
               texture = CallingCardTextures.location(hash);
               int[] size = CallingCardTextures.size(hash);
               if (size != null) {
                  textureWidth = size[0];
                  textureHeight = size[1];
               }
            } else {
               error = "无法解析图片";
            }
         }
         this.entries.add(new CardEntry(null, file, texture, textureWidth, textureHeight, error));
      }
   }

   private int rows() {
      return (this.entries.size() + COLUMNS - 1) / COLUMNS;
   }

   private void refreshCustom() {
      CallingCardLibrary.invalidate();
      this.warning = null;
      this.selectedIndex = -1;
      Minecraft.getInstance().setScreen(new ImageSelectionScreen());
   }

   private void confirmSelection() {
      if (this.selectedIndex < 0 || this.selectedIndex >= this.entries.size()) {
         return;
      }
      CardEntry entry = this.entries.get(this.selectedIndex);
      if (entry.error != null) {
         this.warning = entry.error;
         return;
      }
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (player == null) {
         return;
      }
      String playerName = player.getGameProfile().getName();
      if (entry.builtinPath != null) {
         ModMessages.sendToServer(new C2SPlayerImagePacket(playerName, entry.builtinPath));
      } else {
         byte[] data = entry.file.data();
         String error = CallingCard.validate(data, ModernCodConfig.maxUploadBytes());
         if (error != null) {
            this.warning = error;
            return;
         }
         CallingCardTextures.register(CallingCard.hash(data), data);
         ModMessages.sendToServer(new C2SUploadCallingCardPacket(playerName, entry.file.fileName(), data));
      }
      this.onClose();
   }

   @Override
   public void onClose() {
      super.onClose();
      Minecraft.getInstance().setScreen(new PlayerMainGUI());
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.scrollBar != null && this.scrollBar.visible) {
         this.scrollBar.setScrollOffset(this.scrollBar.scrollOffset() - (int)(scrollY * SCROLL_STEP));
         return true;
      }
      return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int scrollOffset = this.scrollBar != null ? this.scrollBar.scrollOffset() : 0;
         for (int i = 0; i < this.entries.size(); i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int x = this.gridX + col * STEP_X;
            int y = GRID_TOP - scrollOffset + row * STEP_Y;
            if (mouseX >= x && mouseX <= x + CELL_WIDTH && mouseY >= y && mouseY <= y + CELL_HEIGHT
               && mouseY >= GRID_TOP && mouseY <= GRID_TOP + this.viewportHeight) {
               this.selectedIndex = i;
               this.warning = null;
               break;
            }
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
      int scrollOffset = this.scrollBar != null ? this.scrollBar.scrollOffset() : 0;
      int viewportBottom = GRID_TOP + this.viewportHeight;

      for (int i = 0; i < this.entries.size(); i++) {
         int row = i / COLUMNS;
         int col = i % COLUMNS;
         int x = this.gridX + col * STEP_X;
         int y = GRID_TOP - scrollOffset + row * STEP_Y;
         if (y + CELL_HEIGHT < GRID_TOP || y > viewportBottom) {
            continue;
         }
         this.drawCard(guiGraphics, this.entries.get(i), x, y);
         if (i == this.selectedIndex) {
            guiGraphics.fill(x - 2, y - 2, x + CELL_WIDTH + 2, y + CELL_HEIGHT + 2, SELECT_COLOR);
         }
      }

      if (this.warning != null) {
         guiGraphics.drawString(this.font, "⚠ " + this.warning, this.gridX, GRID_TOP - 14, WARNING_COLOR, true);
      }

      int rightPanelX = this.width / 2 + 100;
      if (this.selectedIndex >= 0 && this.selectedIndex < this.entries.size()) {
         CardEntry selected = this.entries.get(this.selectedIndex);
         if (selected.texture != null) {
            guiGraphics.blit(selected.texture, rightPanelX, this.height / 2 - 80, CELL_WIDTH, CELL_HEIGHT,
               0.0F, 0.0F, selected.textureWidth, selected.textureHeight, selected.textureWidth, selected.textureHeight);
         }
      }

      super.render(guiGraphics, mouseX, mouseY, partialTicks);
   }

   private void drawCard(GuiGraphics guiGraphics, CardEntry entry, int x, int y) {
      if (entry.texture != null) {
         guiGraphics.blit(entry.texture, x, y, CELL_WIDTH, CELL_HEIGHT,
            0.0F, 0.0F, entry.textureWidth, entry.textureHeight, entry.textureWidth, entry.textureHeight);
      } else {
         // Never draw the missing texture: paint an explicit placeholder instead.
         guiGraphics.fill(x, y, x + CELL_WIDTH, y + CELL_HEIGHT, PLACEHOLDER_COLOR);
         guiGraphics.drawCenteredString(this.font, "不可用", x + CELL_WIDTH / 2, y + CELL_HEIGHT / 2 - 4, WARNING_COLOR);
      }
   }

   public static void open() {
      Minecraft minecraft = Minecraft.getInstance();
      if (!(minecraft.screen instanceof ImageSelectionScreen)) {
         minecraft.setScreen(new ImageSelectionScreen());
      }
   }

   private static final class CardEntry {
      private final String builtinPath;
      private final CallingCardLibrary.Entry file;
      private final ResourceLocation texture;
      private final int textureWidth;
      private final int textureHeight;
      private final String error;

      private CardEntry(String builtinPath, CallingCardLibrary.Entry file,
                        ResourceLocation texture, int textureWidth, int textureHeight, String error) {
         this.builtinPath = builtinPath;
         this.file = file;
         this.texture = texture;
         this.textureWidth = textureWidth;
         this.textureHeight = textureHeight;
         this.error = error;
      }
   }
}
