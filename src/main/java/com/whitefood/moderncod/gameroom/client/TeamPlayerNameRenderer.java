package com.whitefood.moderncod.gameroom.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.whitefood.moderncod.gameroom.manager.TeamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class TeamPlayerNameRenderer {
   public static TeamManager teamManager;

   public TeamPlayerNameRenderer() {
   }

   public static void setTeamManager(TeamManager manager) {
      teamManager = manager;
   }

   @SubscribeEvent
   public static void onRenderNameTag(RenderNameTagEvent event) {
      if (teamManager != null) {
         if (event.getEntity() instanceof Player targetPlayer) {
            Minecraft mc = Minecraft.getInstance();
            Player localPlayer = mc.player;
            if (localPlayer != null && targetPlayer != localPlayer) {
               String targetTeam = teamManager.getPlayerTeam(targetPlayer.getUUID());
               String localTeam = teamManager.getPlayerTeam(localPlayer.getUUID());
               if (targetTeam != null && localTeam != null) {
                  String targetRoom = getRoomFromTeamName(targetTeam);
                  String localRoom = getRoomFromTeamName(localTeam);
                  if (targetRoom.equals(localRoom)) {
                     if (!targetTeam.equals(localTeam)) {
                        event.setCanRender(TriState.FALSE);
                     } else {
                        event.setCanRender(TriState.DEFAULT);
                        double distanceSquared = localPlayer.distanceToSqr(targetPlayer);
                        renderTeamPlayerNameTag(
                           targetPlayer,
                           targetPlayer.getDisplayName().getString(),
                           event.getPoseStack(),
                           event.getMultiBufferSource(),
                           event.getPackedLight(),
                           event.getEntityRenderer(),
                           distanceSquared
                        );
                     }
                  }
               } else {
                  event.setCanRender(TriState.FALSE);
               }
            }
         }
      }
   }

   private static String getRoomFromTeamName(String teamName) {
      if (teamName != null && !teamName.isEmpty()) {
         int lastCharIndex = teamName.length() - 1;
         char lastChar = teamName.charAt(lastCharIndex);
         return Character.isLetter(lastChar) ? teamName.substring(0, lastCharIndex) : teamName;
      } else {
         return "";
      }
   }

   private static void renderTeamPlayerNameTag(
      Player player,
      String displayName,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      EntityRenderer<?> renderer,
      double distanceSquared
   ) {
      double height = player.getBbHeight() + 0.5;
      poseStack.pushPose();
      poseStack.translate(0.0, height, 0.0);
      EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      poseStack.mulPose(dispatcher.cameraOrientation());
      float scale = 0.025F;
      poseStack.scale(-scale, -scale, scale);
      Matrix4f matrix = poseStack.last().pose();
      Font font = renderer.getFont();
      int textWidth = font.width(displayName);
      int textBackgroundOpacity = (int)(distanceSquared * 255.0 / 10.0);
      if (textBackgroundOpacity > 255) {
         textBackgroundOpacity = 255;
      }

      int backgroundColor = textBackgroundOpacity << 24;
      font.drawInBatch(displayName, -textWidth / 2.0F, 0.0F, 16777215, false, matrix, buffer, DisplayMode.POLYGON_OFFSET, backgroundColor, packedLight);
      drawGreenTriangle(poseStack, buffer, packedLight, textWidth);
      poseStack.popPose();
   }

   private static void drawGreenTriangle(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int textWidth) {
      float triangleSize = 6.0F;
      float xOffset = -triangleSize / 2.0F;
      float yOffset = -triangleSize - 2.0F;
      Matrix4f matrix = poseStack.last().pose();
      ResourceLocation textLocation = ResourceLocation.parse("textures/font/ascii.png");
      VertexConsumer builder = buffer.getBuffer(RenderType.text(textLocation));
      builder.addVertex(matrix, xOffset, yOffset, 0.0F)
         .setColor(0.0F, 1.0F, 0.0F, 1.0F)
         .setUv(0.0F, 0.0F)
         .setUv1(0, 10)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      builder.addVertex(matrix, xOffset + triangleSize, yOffset, 0.0F)
         .setColor(0.0F, 1.0F, 0.0F, 1.0F)
         .setUv(1.0F, 0.0F)
         .setUv1(0, 10)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
      builder.addVertex(matrix, xOffset + triangleSize / 2.0F, yOffset - triangleSize, 0.0F)
         .setColor(0.0F, 1.0F, 0.0F, 1.0F)
         .setUv(0.5F, 1.0F)
         .setUv1(0, 10)
         .setLight(packedLight)
         .setNormal(0.0F, 1.0F, 0.0F);
   }
}
