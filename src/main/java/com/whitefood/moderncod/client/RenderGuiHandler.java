package com.whitefood.moderncod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.whitefood.moderncod.networking.DamageIndicatorMessage;
import com.whitefood.moderncod.networking.RenderLevelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class RenderGuiHandler {
   public static DamageIndicatorMessage.DamageIndicator lastIndicator = new DamageIndicatorMessage.DamageIndicator();
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/indicator.png");

   public RenderGuiHandler() {
   }

   @SubscribeEvent
   public static void onRenderGui(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (lastIndicator.startTick == -1.0F) {
         lastIndicator.startTick = RenderLevelHandler.curTick;
      }

      if (RenderLevelHandler.curTick < lastIndicator.startTick + 20.0F) {
         LocalPlayer player = mc.player;
         Vec3 lookVec3 = player.getLookAngle();
         Vec2 lookVec2 = new Vec2((float)lookVec3.x, (float)lookVec3.z).normalized();
         double dx = lastIndicator.x - player.getX();
         double dz = lastIndicator.z - player.getZ();
         Vec2 srcVec2 = new Vec2((float)dx, (float)dz).normalized();
         double dot = lookVec2.dot(srcVec2);
         double angle = Math.acos(dot);
         double cross = lookVec2.x * srcVec2.y - lookVec2.y * srcVec2.x;
         if (cross < 0.0) {
            angle = (Math.PI * 2) - angle;
         }

         double rawAngle = angle;
         angle -= Math.PI / 2;
         if (angle < 0.0) {
            angle += Math.PI * 2;
         }

         int xc = mc.getWindow().getGuiScaledWidth() / 2;
         int yc = mc.getWindow().getGuiScaledHeight() / 2;
         double distance = 50.0;
         double x = xc + distance * Math.cos(angle);
         double y = yc + distance * Math.sin(angle);
         PoseStack poseStack = event.getGuiGraphics().pose();
         poseStack.pushPose();
         poseStack.translate(x, y, 0.0);
         poseStack.mulPose(Axis.ZP.rotation((float)rawAngle));
         event.getGuiGraphics().blit(TEXTURE, -16, -8, 0.0F, 0.0F, 32, 16, 32, 16);
         poseStack.popPose();
      } else {
         lastIndicator.startTick = 0.0F;
      }
   }
}
