package com.whitefood.moderncod.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class OverlayCodHealthUI {
   private static final ResourceLocation HEALTH_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/modern_ui.png");
   private static float lastHealth = 20.0F;
   private static long lastDamageTime = 0L;
   private static float damageAmount = 0.0F;

   public OverlayCodHealthUI() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      if (player != null) {
         updateHealthTracking(player);
         if (!player.isCreative() && !player.isSpectator()) {
            renderHealthBar(event.getGuiGraphics(), player);
            if (player instanceof AbstractClientPlayer) {
               renderPlayerName(event.getGuiGraphics(), (AbstractClientPlayer)player, 20, event.getGuiGraphics().guiHeight() - 40);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPreRenderOverlay(Pre event) {
      if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
         event.setCanceled(true);
      }
   }

   private static void updateHealthTracking(Player player) {
      float currentHealth = player.getHealth();
      if (currentHealth < lastHealth) {
         lastDamageTime = System.currentTimeMillis();
         damageAmount = lastHealth - currentHealth;
      }

      lastHealth = currentHealth;
   }

   private static void renderHealthBar(GuiGraphics gui, Player player) {
      int x = 20;
      int y = gui.guiHeight() - 29;
      int barWidth = 73;
      int barHeight = 3;
      float health = player.getHealth();
      float maxHealth = player.getMaxHealth();
      float healthPercentage = health / maxHealth;
      int healthWidth = (int)(healthPercentage * barWidth);
      gui.blit(HEALTH_TEXTURE, x, y, 0, 3, barWidth, barHeight);
      gui.blit(HEALTH_TEXTURE, x, y, 0, 0, healthWidth, barHeight);
      long timeSinceDamage = System.currentTimeMillis() - lastDamageTime;
      if (timeSinceDamage < 1000L) {
         float animProgress = 1.0F - (float)Math.pow(1.0F - (float)timeSinceDamage / 1000.0F, 3.0);
         int damageWidth = (int)(damageAmount / maxHealth * barWidth * (1.0F - animProgress));
         if (damageWidth > 0) {
            gui.blit(HEALTH_TEXTURE, x + healthWidth, y, 0, 6, Math.min(damageWidth, barWidth - healthWidth), barHeight);
         }
      }
   }

   private static void renderPlayerName(GuiGraphics gui, AbstractClientPlayer player, int x, int y) {
      gui.pose().pushPose();
      gui.pose().scale(0.8F, 0.8F, 1.0F);
      gui.drawString(Minecraft.getInstance().font, player.getName().getString(), (int)(x / 0.8F), (int)(y / 0.8F), 16776960, false);
      gui.pose().popPose();
   }
}
