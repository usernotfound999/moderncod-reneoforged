package com.whitefood.moderncod.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.whitefood.moderncod.networking.CommonClasses;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.C2SMoveItemPacket;
import com.whitefood.moderncod.util.KeyBinding;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;

public class WheelMenu {
   public static final List<ResourceLocation> ITEMS = CommonClasses.ITEMS;
   private static final List<Item> ITEM_OBJECTS = new ArrayList<>();
   private static boolean isMenuOpen;
   private static int selectedIndex;
   private static final List<Integer> availableItems;
   private static long keyPressStartTime;
   private static final long LONG_PRESS_THRESHOLD = 180L;
   private static boolean wasKeyDown;
   private static final int COOLDOWN_TICKS = 5;
   private static int releaseCooldown;

   public WheelMenu() {
   }

   public static Item getItemByIndex(int index) {
      return index >= 0 && index < ITEM_OBJECTS.size() ? ITEM_OBJECTS.get(index) : null;
   }

   public static void setupClient(FMLClientSetupEvent event) {
      NeoForge.EVENT_BUS.addListener(WheelMenu::onKeyInput);
   }

   public static void onKeyInput(ClientTickEvent.Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         boolean isKeyDown = KeyBinding.WHEEL_MENU_KEY.isDown();
         long currentTime = System.currentTimeMillis();
         if (releaseCooldown > 0) {
            releaseCooldown--;
         }

         if (isKeyDown && !wasKeyDown) {
            keyPressStartTime = currentTime;
         }

         if (isKeyDown && currentTime - keyPressStartTime >= 180L && !isMenuOpen) {
            mc.setScreen(new WheelMenu.WheelMenuScreen());
            isMenuOpen = true;
            releaseCooldown = 5;
         }

         if (!isKeyDown && wasKeyDown && releaseCooldown == 0) {
            if (isMenuOpen) {
               mc.setScreen(null);
            }

            isMenuOpen = false;
         }

         wasKeyDown = isKeyDown;
      }
   }

   static {
      for (ResourceLocation resourceLocation : ITEMS) {
         Item item = (Item)BuiltInRegistries.ITEM.get(resourceLocation);
         if (item != null) {
            ITEM_OBJECTS.add(item);
         }
      }

      isMenuOpen = false;
      selectedIndex = -1;
      availableItems = new ArrayList<>();
      keyPressStartTime = 0L;
      wasKeyDown = false;
      releaseCooldown = 0;
   }

   private static class WheelMenuScreen extends Screen {
      protected WheelMenuScreen() {
         super(Component.literal("Wheel Menu"));
      }

      protected void init() {
         WheelMenu.availableItems.clear();
         Player player = Minecraft.getInstance().player;
         if (player != null) {
            for (int i = 0; i < WheelMenu.ITEMS.size(); i++) {
               Item item = (Item)BuiltInRegistries.ITEM.get(WheelMenu.ITEMS.get(i));
               if (item != null && this.hasItemInInventory(player, item)) {
                  WheelMenu.availableItems.add(i);
               }
            }
         }
      }

      public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
         this.drawTransparentBackground(guiGraphics);
         int centerX = this.width / 2;
         int centerY = this.height / 2;
         int radius = 80;
         int iconOffset = 40;
         this.drawCircularBackground(guiGraphics, centerX, centerY);
         if (WheelMenu.selectedIndex >= 0) {
            this.drawHighlight(guiGraphics, centerX, centerY, WheelMenu.selectedIndex);
         }

         int itemCount = WheelMenu.ITEMS.size();
         double sectorAngle = (Math.PI * 2) / itemCount;

         for (int i = 0; i < itemCount; i++) {
            double angle = 0.0 - (i * sectorAngle + sectorAngle / 2.0);
            int iconX = (int)(centerX + iconOffset * Math.cos(angle));
            int iconY = (int)(centerY + iconOffset * Math.sin(angle));
            Item item = (Item)BuiltInRegistries.ITEM.get(WheelMenu.ITEMS.get(i));
            if (item != null && WheelMenu.availableItems.contains(i)) {
               this.renderItemIcon(guiGraphics, item, iconX, iconY);
            }
         }
      }

      private void drawTransparentBackground(GuiGraphics guiGraphics) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.3F);
         guiGraphics.fillGradient(0, 0, this.width, this.height, 1342177280, 1342177280);
         RenderSystem.disableBlend();
      }

      private void drawHighlight(GuiGraphics guiGraphics, int centerX, int centerY, int sectorIndex) {
         double sectorAngle = (Math.PI * 2) / WheelMenu.ITEMS.size();
         double startAngle = sectorIndex * sectorAngle;
         double endAngle = startAngle + sectorAngle;
         int radius = 80;
         Tesselator tesselator = Tesselator.getInstance();
         BufferBuilder bufferBuilder = tesselator.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
         bufferBuilder.addVertex(centerX, centerY, 0.0F).setColor(255, 255, 255, 128);

         for (double angle = startAngle; angle <= endAngle; angle += 0.1) {
            int x = (int)(centerX + radius * Math.cos(angle));
            int y = (int)(centerY - radius * Math.sin(angle));
            bufferBuilder.addVertex(x, y, 0.0F).setColor(255, 255, 255, 128);
         }

         int finalX = (int)(centerX + radius * Math.cos(endAngle));
         int finalY = (int)(centerY - radius * Math.sin(endAngle));
         bufferBuilder.addVertex(finalX, finalY, 0.0F).setColor(255, 255, 255, 128);
         BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
         RenderSystem.disableBlend();
      }

      private void drawCircularBackground(GuiGraphics guiGraphics, int centerX, int centerY) {
         int radius = 80;
         Tesselator tesselator = Tesselator.getInstance();
         BufferBuilder bufferBuilder = tesselator.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.8F);
         bufferBuilder.addVertex(centerX, centerY, 0.0F).setColor(0, 0, 0, 128);

         for (double angle = 0.0; angle <= Math.PI * 2; angle += 0.1) {
            int x = (int)(centerX + radius * Math.cos(angle));
            int y = (int)(centerY - radius * Math.sin(angle));
            bufferBuilder.addVertex(x, y, 0.0F).setColor(0, 0, 0, 128);
         }

         bufferBuilder.addVertex(centerX + radius, centerY, 0.0F).setColor(0, 0, 0, 128);
         BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
         RenderSystem.disableBlend();
      }

      public void mouseMoved(double mouseX, double mouseY) {
         int centerX = this.width / 2;
         int centerY = this.height / 2;
         double dx = mouseX - centerX;
         double dy = mouseY - centerY;
         double distance = Math.sqrt(dx * dx + dy * dy);
         if (distance > 80.0) {
            WheelMenu.selectedIndex = -1;
         } else {
            double angle = Math.atan2(-dy, dx);
            if (angle < 0.0) {
               angle += Math.PI * 2;
            }

            double sectorAngle = (Math.PI * 2) / WheelMenu.ITEMS.size();
            int hoveredIndex = (int)(angle / sectorAngle) % WheelMenu.ITEMS.size();
            if (WheelMenu.availableItems.contains(hoveredIndex)) {
               WheelMenu.selectedIndex = hoveredIndex;
            } else {
               WheelMenu.selectedIndex = -1;
            }
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (button == 0 && WheelMenu.selectedIndex >= 0 && WheelMenu.availableItems.contains(WheelMenu.selectedIndex)) {
            ModMessages.sendToServer(new C2SMoveItemPacket(WheelMenu.selectedIndex));
            Minecraft.getInstance().setScreen(null);
            return true;
         } else {
            return super.mouseClicked(mouseX, mouseY, button);
         }
      }

      public void onClose() {
         super.onClose();
         WheelMenu.isMenuOpen = false;
      }

      public void onCloseMenu() {
         WheelMenu.isMenuOpen = false;
      }

      private void renderItemIcon(GuiGraphics guiGraphics, Item item, int x, int y) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         ItemStack stack = new ItemStack(item);
         int iconSize = 16;
         guiGraphics.renderItem(stack, x - iconSize / 2, y - iconSize / 2);
         RenderSystem.disableBlend();
      }

      private boolean hasItemInInventory(Player player, Item item) {
         Inventory inventory = player.getInventory();

         for (ItemStack stack : inventory.items) {
            if (stack.getItem() == item) {
               return true;
            }
         }

         return false;
      }
   }
}
