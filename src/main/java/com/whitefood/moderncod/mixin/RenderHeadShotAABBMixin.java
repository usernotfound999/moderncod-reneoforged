package com.whitefood.moderncod.mixin;

import com.tacz.guns.client.event.RenderHeadShotAABB;
import net.neoforged.neoforge.client.event.RenderLivingEvent.Post;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderHeadShotAABB.class)
public abstract class RenderHeadShotAABBMixin {
   public RenderHeadShotAABBMixin() {
   }

   @Inject(method = "onRenderEntity", at = @At("HEAD"), cancellable = true, remap = false)
   private static void onRenderHeadShotAABB(Post<?, ?> event, CallbackInfo ci) {
      ci.cancel();
   }
}
