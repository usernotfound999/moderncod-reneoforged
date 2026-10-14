package com.whitefood.moderncod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.whitefood.moderncod.item.BaseItemShield;
import com.whitefood.moderncod.item.ModItems;
import com.whitefood.moderncod.item.render.RendererShield;
import com.whitefood.moderncod.item.render.RendererSmallShield;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class ModClientExtensions {
   public ModClientExtensions() {
   }

   @SubscribeEvent
   public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
      if (ModItems.SHIELD.get() instanceof BaseItemShield) {
         event.registerItem(shieldExtension(new RendererShield()), ModItems.SHIELD.get());
      }

      if (ModItems.SMALL_SHIELD.get() instanceof BaseItemShield) {
         event.registerItem(shieldExtension(new RendererSmallShield()), ModItems.SMALL_SHIELD.get());
      }
   }

   private static IClientItemExtensions shieldExtension(BlockEntityWithoutLevelRenderer renderer) {
      return new IClientItemExtensions() {
         @Override
         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
         }

         @Override
         public boolean applyForgeHandTransform(
            PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProgress, float swingProgress
         ) {
            int i = arm == HumanoidArm.RIGHT ? 1 : -1;
            poseStack.translate(i * 0.56F, -0.52F, -0.72F);
            if (player.getUseItem() == itemInHand) {
               poseStack.translate(0.05, 0.05, 0.05);
            }

            if (itemInHand.getItem() instanceof BaseItemShield shield) {
               shield.ensureIdleAnimation(player, itemInHand);
            }

            return true;
         }
      };
   }
}
