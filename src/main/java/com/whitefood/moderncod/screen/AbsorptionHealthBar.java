package com.whitefood.moderncod.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class AbsorptionHealthBar {
   private static final ResourceLocation NEW_HEALTH_BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/modern_ui.png");

   public AbsorptionHealthBar() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      if (player != null) {
         if (!player.isCreative() && !player.isSpectator()) {
            renderHealthBar(event.getGuiGraphics(), player);
         }
      }
   }

   public static void renderHealthBar(GuiGraphics gui, LocalPlayer player) {
      float absorption = player.getAbsorptionAmount();
      int xPos = 20;
      int yPos = gui.guiHeight() - 33;
      int barWidth = 73;
      int barHeight = 3;
      if (absorption > 0.0F) {
         float absorptionPercentage = Math.min(absorption / 60.0F, 1.0F);
         int absorptionWidth = (int)(absorptionPercentage * barWidth);
         gui.blit(NEW_HEALTH_BAR_TEXTURE, xPos, yPos, 0, 14, barWidth, barHeight);
         gui.blit(NEW_HEALTH_BAR_TEXTURE, xPos, yPos, 0, 10, absorptionWidth, barHeight);
      }
   }
}
