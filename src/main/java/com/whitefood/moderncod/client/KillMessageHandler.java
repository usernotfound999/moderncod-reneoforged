package com.whitefood.moderncod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.whitefood.moderncod.client.data.WeaponHudRegistry;
import com.whitefood.moderncod.networking.data.GenericKillMessageData;
import com.whitefood.moderncod.networking.data.KillMessageData;
import com.whitefood.moderncod.networking.packet.S2CGenericKillMessagePacket;
import com.whitefood.moderncod.networking.packet.S2CKillMessagePacket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class KillMessageHandler {
   private static final List<KillMessageHandler.KillMessage> killMessages = new ArrayList<>();
   private static final ResourceLocation HEADSHOT_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/headshot.png");

   public KillMessageHandler() {
   }

   public static void onKillMessageReceived(S2CKillMessagePacket message) {
      KillMessageData data = message.getKillMessageData();
      addKillMessage(data.getKillerName(), data.getDeadName(), data.getGunId().toString(), data.isHeadKill(), data.getKillerTeam(), data.getDeadTeam());
   }

   public static void onGenericKillMessageReceived(S2CGenericKillMessagePacket message) {
      GenericKillMessageData data = message.getData();
      addKillMessage(data.getKillerName(), data.getDeadName(), data.getWeaponId(), data.isHeadKill(), data.getKillerTeam(), data.getDeadTeam());
   }

   private static void addKillMessage(String killerName, String deadName, String weaponId, boolean isHeadKill, String killerTeam, String deadTeam) {
      Minecraft minecraft = Minecraft.getInstance();
      Player player = minecraft.player;
      Team playerTeam = player != null ? player.getTeam() : null;
      Component killerNameComponent = Component.literal(killerName)
         .setStyle(Style.EMPTY.withColor(killerName.equals(player.getName().getString()) ? ChatFormatting.YELLOW : getTeamColor(killerTeam, playerTeam)));
      Component deadNameComponent = Component.literal(deadName)
         .setStyle(Style.EMPTY.withColor(deadName.equals(player.getName().getString()) ? ChatFormatting.YELLOW : getTeamColor(deadTeam, playerTeam)));
      ResourceLocation hudTexture = getHudTextureForWeaponId(weaponId);
      killMessages.add(0, new KillMessageHandler.KillMessage(killerNameComponent, deadNameComponent, hudTexture, isHeadKill));
      if (killMessages.size() > 5) {
         killMessages.remove(killMessages.size() - 1);
      }
   }

   @SubscribeEvent
   public static void onRenderGui(Post event) {
      render(event.getGuiGraphics());
   }

   public static void render(GuiGraphics guiGraphics) {
      int screenWidth = guiGraphics.guiWidth();
      int screenHeight = guiGraphics.guiHeight();
      int baseX = 20;
      int baseY = screenHeight / 2;
      int index = 0;
      Iterator<KillMessageHandler.KillMessage> iterator = killMessages.iterator();

      while (iterator.hasNext()) {
         KillMessageHandler.KillMessage message = iterator.next();
         int targetY = baseY + index * 15;
         int diff = targetY - message.yOffset;
         if (diff != 0) {
            int step = Math.signum(diff) == 1.0F ? 1 : -1;
            message.yOffset = message.yOffset + step * Math.min(Math.abs(diff), 2);
         }

         long elapsed = System.currentTimeMillis() - message.startTime;
         float alpha = elapsed < 6000L ? 1.0F : Math.max(0.0F, 1.0F - (float)(elapsed - 6000L) / 1000.0F);
         if (alpha < 0.2F) {
            message.yOffset = screenHeight * 2;
         }

         if (elapsed > 7000L) {
            iterator.remove();
         } else {
            int color = 16777215 | (int)(alpha * 255.0F) << 24;
            guiGraphics.drawString(Minecraft.getInstance().font, message.killerName, baseX, message.yOffset, color, true);
            if (message.gunHudTexture != null) {
               int textureX = baseX + Minecraft.getInstance().font.width(message.killerName) + 2;
               renderGunHudTexture(guiGraphics, message.gunHudTexture, textureX, message.yOffset - 2);
            }

            int nameOffset = 0;
            if (message.isHeadKill) {
               int headshotX = baseX + Minecraft.getInstance().font.width(message.killerName) + (message.gunHudTexture != null ? 44 : 0);
               renderHeadshotIcon(guiGraphics, headshotX, message.yOffset - 2);
               nameOffset = 10;
            }

            int deadNameX = baseX + Minecraft.getInstance().font.width(message.killerName) + (message.gunHudTexture != null ? 44 : 0) + nameOffset;
            guiGraphics.drawString(Minecraft.getInstance().font, message.deadName, deadNameX, message.yOffset, color, true);
            RenderSystem.disableBlend();
            index++;
         }
      }
   }

   private static void renderHeadshotIcon(GuiGraphics guiGraphics, int x, int y) {
      RenderSystem.enableDepthTest();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(HEADSHOT_TEXTURE, x, y, 0.0F, 0.0F, 8, 8, 8, 8);
   }

   private static void renderGunHudTexture(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y) {
      Minecraft mc = Minecraft.getInstance();
      RenderSystem.enableDepthTest();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.pose().pushPose();
      guiGraphics.pose().scale(-1.0F, 1.0F, 1.0F);
      guiGraphics.blit(texture, -x - 39, y + 13, 0.0F, 0.0F, 39, -13, 39, 13);
      guiGraphics.pose().popPose();
   }

   private static ResourceLocation getHudTextureForWeaponId(String weaponId) {
      try {
         ResourceLocation id = ResourceLocation.parse(weaponId);
         Optional<ClientGunIndex> optional = TimelessAPI.getClientGunIndex(id);
         return optional.isPresent() ? optional.get().getDefaultDisplay().getHUDTexture() : WeaponHudRegistry.get(weaponId);
      } catch (Exception e) {
         return WeaponHudRegistry.DEFAULT_TEXTURE;
      }
   }

   private static ChatFormatting getTeamColor(String teamName, Team playerTeam) {
      if (playerTeam != null) {
         return teamName.equals(playerTeam.getName()) ? ChatFormatting.BLUE : ChatFormatting.RED;
      } else {
         return ChatFormatting.WHITE;
      }
   }

   public static class KillMessage {
      public final Component killerName;
      public final Component deadName;
      public final ResourceLocation gunHudTexture;
      public final boolean isHeadKill;
      public int yOffset;
      public final long startTime;

      public KillMessage(Component killerName, Component deadName, ResourceLocation gunHudTexture, boolean isHeadKill) {
         this.killerName = killerName;
         this.deadName = deadName;
         this.gunHudTexture = gunHudTexture;
         this.isHeadKill = isHeadKill;
         this.yOffset = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 15;
         this.startTime = System.currentTimeMillis();
      }
   }
}
