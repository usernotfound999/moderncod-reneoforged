package com.whitefood.moderncod.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
   public KeyboardHandlerMixin() {
   }

   @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
   private void onKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.options.keyTogglePerspective.matches(key, scancode)) {
         ci.cancel();
      }
   }
}
