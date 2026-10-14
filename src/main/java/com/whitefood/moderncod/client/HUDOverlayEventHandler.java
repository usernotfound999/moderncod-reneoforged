package com.whitefood.moderncod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class HUDOverlayEventHandler {
   public HUDOverlayEventHandler() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Pre event) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (event.getName().equals(VanillaGuiLayers.FOOD_LEVEL) && player.getFoodData().getFoodLevel() == 20) {
         event.setCanceled(true);
      }

      if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR)) {
         event.setCanceled(true);
      }

      if (event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)) {
         event.setCanceled(true);
      }

      if (event.getName().equals(VanillaGuiLayers.HOTBAR) && !player.isCreative() && !player.isSpectator()) {
         event.setCanceled(true);
      }
   }
}
