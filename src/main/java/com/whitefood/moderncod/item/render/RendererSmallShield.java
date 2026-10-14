package com.whitefood.moderncod.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.whitefood.moderncod.item.custom.ItemSmallShield;
import com.whitefood.moderncod.item.model.ModelSmallShield;
import com.whitefood.moderncod.util.AnimUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class RendererSmallShield extends GeoItemRenderer<ItemSmallShield> {
   private static final float SCALE_RECIPROCAL = 0.0625F;
   protected boolean renderArms = false;
   protected MultiBufferSource currentBuffer;
   protected RenderType renderType;
   public ItemDisplayContext transformType;
   protected ItemSmallShield animatable;
   private final Set<String> hiddenBones = new HashSet<>();
   private final Set<String> suppressedBones = new HashSet<>();

   public RendererSmallShield() {
      super(new ModelSmallShield());
   }

   public RenderType getRenderType(ItemSmallShield animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
      return RenderType.entityTranslucent(this.getTextureLocation(animatable));
   }

   public void renderByItem(
      ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int p_239207_6_
   ) {
      this.transformType = transformType;
      if (this.animatable != null) {
         this.animatable.getTransformType(transformType);
      }

      super.renderByItem(stack, transformType, matrixStack, bufferIn, combinedLightIn, p_239207_6_);
   }

   public void actuallyRender(
      PoseStack matrixStackIn,
      ItemSmallShield animatable,
      BakedGeoModel model,
      RenderType type,
      MultiBufferSource renderTypeBuffer,
      VertexConsumer vertexBuilder,
      boolean isRenderer,
      float partialTicks,
      int packedLightIn,
      int packedOverlayIn,
      int color
   ) {
      this.currentBuffer = renderTypeBuffer;
      this.renderType = type;
      this.animatable = animatable;
      super.actuallyRender(
         matrixStackIn,
         animatable,
         model,
         type,
         renderTypeBuffer,
         vertexBuilder,
         isRenderer,
         partialTicks,
         packedLightIn,
         packedOverlayIn,
         color
      );
      if (this.renderArms) {
         this.renderArms = false;
      }
   }

   public void renderRecursively(
      PoseStack stack,
      ItemSmallShield animatable,
      GeoBone bone,
      RenderType type,
      MultiBufferSource buffer,
      VertexConsumer bufferIn,
      boolean isReRender,
      float partialTick,
      int packedLightIn,
      int packedOverlayIn,
      int color
   ) {
      Minecraft mc = Minecraft.getInstance();
      String name = bone.getName();
      boolean renderingArms = false;
      if (!name.equals("lefthand_pos") && !name.equals("righthand_pos")) {
         bone.setHidden(this.hiddenBones.contains(name));
      } else {
         bone.setHidden(true);
         renderingArms = true;
      }

      if (this.transformType.firstPerson() && renderingArms) {
         AbstractClientPlayer player = mc.player;
         float armsAlpha = player.isInvisible() ? 0.15F : 1.0F;
         PlayerRenderer playerRenderer = (PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(player);
         PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>)playerRenderer.getModel();
         stack.pushPose();
         RenderUtil.translateMatrixToBone(stack, bone);
         RenderUtil.translateToPivotPoint(stack, bone);
         RenderUtil.rotateMatrixAroundBone(stack, bone);
         RenderUtil.scaleMatrixForBone(stack, bone);
         RenderUtil.translateAwayFromPivotPoint(stack, bone);
         ResourceLocation loc = player.getSkin().texture();
         VertexConsumer armBuilder = this.currentBuffer.getBuffer(RenderType.entitySolid(loc));
         VertexConsumer sleeveBuilder = this.currentBuffer.getBuffer(RenderType.entityTranslucent(loc));
         if (name.equals("lefthand_pos")) {
            stack.translate(-0.0625F, 0.125F, 0.0F);
            AnimUtils.renderPartOverBone(model.leftArm, bone, stack, armBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
            AnimUtils.renderPartOverBone(model.leftSleeve, bone, stack, sleeveBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
         } else if (name.equals("righthand_pos")) {
            stack.translate(0.0625F, 0.125F, 0.0F);
            AnimUtils.renderPartOverBone(model.rightArm, bone, stack, armBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
            AnimUtils.renderPartOverBone(model.rightSleeve, bone, stack, sleeveBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
         }

         this.currentBuffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(this.animatable)));
         stack.popPose();
      }

      super.renderRecursively(stack, animatable, bone, type, buffer, bufferIn, isReRender, partialTick, packedLightIn, packedOverlayIn, color);
   }

   public ResourceLocation getTextureLocation(ItemSmallShield instance) {
      return super.getTextureLocation(instance);
   }
}
