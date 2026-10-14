package com.whitefood.moderncod.mixin;

import com.whitefood.moderncod.animation.Slide;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> {
   public PlayerModelMixin() {
   }

   @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"), cancellable = true)
   public void setupAnimPre(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
      if (pEntity instanceof Player player) {
         PlayerModel model = (PlayerModel)(Object)this;
         Slide.animationPre(model, player);
      }
   }

   @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), cancellable = true)
   public void setupAnimPost(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
      if (pEntity instanceof Player player) {
         PlayerModel model = (PlayerModel)(Object)this;
         Slide.animationPost(model, player);
      }
   }
}
