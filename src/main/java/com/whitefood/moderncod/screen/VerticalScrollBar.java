package com.whitefood.moderncod.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Minimal vertical scrollbar: a track plus a draggable handle exposing a normalized {@code 0..1}
 * position. The owning screen translates that into a pixel offset.
 */
public class VerticalScrollBar extends AbstractWidget {
   private static final int TRACK_COLOR = 0x60202020;
   private static final int HANDLE_COLOR = 0xFFB0B0B0;
   private static final int MIN_HANDLE_HEIGHT = 12;

   private double value;
   private int contentHeight;
   private final Runnable onChange;

   public VerticalScrollBar(int x, int y, int width, int height, int contentHeight, Runnable onChange) {
      super(x, y, width, height, Component.empty());
      this.contentHeight = contentHeight;
      this.onChange = onChange;
   }

   public void setContentHeight(int contentHeight) {
      this.contentHeight = contentHeight;
      this.clamp();
   }

   /** @return the pixel offset this bar currently represents. */
   public int scrollOffset() {
      return (int) Math.round(this.value * this.maxScroll());
   }

   /** Positions the handle so it represents {@code offset} pixels of scroll. */
   public void setScrollOffset(int offset) {
      int max = this.maxScroll();
      this.value = max <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, (double) offset / max));
      this.onChange.run();
   }

   private int maxScroll() {
      return Math.max(0, this.contentHeight - this.getHeight());
   }

   private void clamp() {
      if (this.value < 0.0) {
         this.value = 0.0;
      } else if (this.value > 1.0) {
         this.value = 1.0;
      }
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      int x = this.getX();
      int y = this.getY();
      int width = this.getWidth();
      int height = this.getHeight();
      guiGraphics.fill(x, y, x + width, y + height, TRACK_COLOR);
      int max = this.maxScroll();
      if (max <= 0) {
         return;
      }
      int handleHeight = Math.max(MIN_HANDLE_HEIGHT, (int)((double) height * height / this.contentHeight));
      int handleY = y + (int)(this.value * (height - handleHeight));
      guiGraphics.fill(x, handleY, x + width, handleY + handleHeight, HANDLE_COLOR);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.active && this.visible && button == 0 && this.isMouseOver(mouseX, mouseY)) {
         this.updateFromMouse(mouseY);
         return true;
      }
      return false;
   }

   @Override
   protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
      this.updateFromMouse(mouseY);
   }

   private void updateFromMouse(double mouseY) {
      int max = this.maxScroll();
      if (max <= 0) {
         return;
      }
      int height = this.getHeight();
      int handleHeight = Math.max(MIN_HANDLE_HEIGHT, (int)((double) height * height / this.contentHeight));
      double relative = (mouseY - this.getY() - handleHeight / 2.0) / (height - handleHeight);
      this.value = Math.max(0.0, Math.min(1.0, relative));
      this.onChange.run();
   }

   @Override
   protected void updateWidgetNarration(NarrationElementOutput narration) {
   }
}
