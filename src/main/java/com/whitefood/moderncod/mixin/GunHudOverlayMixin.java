package com.whitefood.moderncod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

@OnlyIn(Dist.CLIENT)
@Mixin(GunHudOverlay.class)
public abstract class GunHudOverlayMixin implements LayeredDraw.Layer {
   private static long checkAmmoTimestamp = -1L;
   private static int cacheMaxAmmoCount = 0;
   private static int cacheInventoryAmmoCount = 0;

   public GunHudOverlayMixin() {
   }

   @Override
   public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
      int width = graphics.guiWidth();
      int height = graphics.guiHeight();
      if ((Boolean)RenderConfig.GUN_HUD_ENABLE.get()) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player instanceof IClientPlayerGunOperator) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof IGun iGun) {
               ResourceLocation var27 = iGun.getGunId(stack);
               GunData gunData = TimelessAPI.getClientGunIndex(var27).<GunData>map(ClientGunIndex::getGunData).orElse(null);
               GunDisplayInstance display = (GunDisplayInstance)TimelessAPI.getGunDisplay(stack).orElse(null);
               if (gunData != null && display != null) {
                  int ammoCount = iGun.getCurrentAmmoCount(stack) + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
                  int ammoCountColor = ammoCount < cacheMaxAmmoCount * 0.25 ? 16733525 : 16777215;
                  String currentAmmoCountText;
                  if (display.getAmmoCountStyle() == AmmoCountStyle.PERCENT) {
                     currentAmmoCountText = String.format("%03d%%", (int)((float)ammoCount / (cacheMaxAmmoCount == 0 ? 1 : cacheMaxAmmoCount) * 100.0F));
                  } else {
                     currentAmmoCountText = String.format("%03d", ammoCount);
                  }

                  this.handleCacheCount(player, stack, gunData, iGun);
                  PoseStack poseStack = graphics.pose();
                  Font font = mc.font;
                  poseStack.pushPose();
                  poseStack.scale(1.8F, 1.8F, 1.0F);
                  graphics.drawString(font, currentAmmoCountText, (width - 70) / 1.8F, (height - 45) / 1.8F, ammoCountColor, false);
                  poseStack.popPose();
                  Component gunName = stack.getHoverName();
                  int GunNameColor = 16777215;
                  if (gunName != null) {
                     String gunNameString = gunName.getString();
                     poseStack.pushPose();
                     poseStack.scale(1.0F, 1.0F, 1.0F);
                     graphics.drawString(font, gunNameString, width - font.width(gunNameString) - 20, height - 15, GunNameColor, false);
                     poseStack.popPose();
                  }

                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableBlend();
                  String inventoryAmmoCountText = String.format("%04d", cacheInventoryAmmoCount);
                  int inventoryAmmoCountColor = iGun.useDummyAmmo(stack) ? 5636095 : 11184810;
                  poseStack.pushPose();
                  poseStack.scale(0.8F, 0.8F, 1.0F);
                  graphics.drawString(
                     font,
                     inventoryAmmoCountText,
                     (width - 62 + mc.font.width(currentAmmoCountText) * 1.5F) / 0.8F,
                     (height - 43) / 0.8F,
                     inventoryAmmoCountColor,
                     false
                  );
                  poseStack.popPose();
                  RenderSystem.enableDepthTest();
                  RenderSystem.setShader(GameRenderer::getPositionTexShader);
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  ResourceLocation hudTexture = display.getHUDTexture();
                  ResourceLocation hudEmptyTexture = display.getHudEmptyTexture();
                  if (ammoCount <= 0) {
                     if (hudEmptyTexture == null) {
                        RenderSystem.setShaderColor(1.0F, 0.3F, 0.3F, 1.0F);
                     } else {
                        hudTexture = hudEmptyTexture;
                     }
                  }

                  poseStack.pushPose();
                  poseStack.scale(1.5F, 1.5F, 1.0F);
                  graphics.blit(hudTexture, (int)((width - 142) / 1.5F), (int)((height - 48) / 1.5F), 0.0F, 0.0F, 39, 13, 39, 13);
                  poseStack.popPose();
                  FireMode fireMode = IGun.getMainhandFireMode(player);

                  ResourceLocation fireModeTexture = switch (fireMode) {
                     case AUTO -> ResourceLocation.fromNamespaceAndPath("tacz", "textures/hud/fire_mode_auto.png");
                     case BURST -> ResourceLocation.fromNamespaceAndPath("tacz", "textures/hud/fire_mode_burst.png");
                     default -> ResourceLocation.fromNamespaceAndPath("tacz", "textures/hud/fire_mode_semi.png");
                  };
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  graphics.blit(
                     fireModeTexture, (int)(width - 62.5 + mc.font.width(currentAmmoCountText) * 1.5), height - 39, 0.0F, 0.0F, 10, 10, 10, 10
                  );
               }
            }
         }
      }
   }

   private void handleCacheCount(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun) {
      if (System.currentTimeMillis() - checkAmmoTimestamp > 200L) {
         checkAmmoTimestamp = System.currentTimeMillis();
         cacheMaxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(stack, gunData);
         if (IGunOperator.fromLivingEntity(player).needCheckAmmo()) {
            if (iGun.useDummyAmmo(stack)) {
               cacheInventoryAmmoCount = iGun.getDummyAmmoAmount(stack);
            } else {
               this.handleInventoryAmmo(stack, player.getInventory());
            }
         } else {
            cacheInventoryAmmoCount = 9999;
         }
      }
   }

   private void handleInventoryAmmo(ItemStack stack, Inventory inventory) {
      cacheInventoryAmmoCount = 0;

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack inventoryItem = inventory.getItem(i);
         Item var5 = inventoryItem.getItem();
         if (var5 instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(stack, inventoryItem)) {
            cacheInventoryAmmoCount = cacheInventoryAmmoCount + inventoryItem.getCount();
         }

         if (var5 instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(stack, inventoryItem)) {
            if (iAmmoBox.isAllTypeCreative(inventoryItem) || iAmmoBox.isCreative(inventoryItem)) {
               cacheInventoryAmmoCount = 9999;
               return;
            }

            cacheInventoryAmmoCount = cacheInventoryAmmoCount + iAmmoBox.getAmmoCount(inventoryItem);
         }
      }
   }
}
