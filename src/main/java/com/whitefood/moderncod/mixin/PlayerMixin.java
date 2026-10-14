package com.whitefood.moderncod.mixin;

import com.whitefood.moderncod.animation.Slide;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Player.class)
public class PlayerMixin extends EntityMixin {
   public PlayerMixin() {
   }

   @Overwrite
   protected boolean isStayingOnGroundSurface() {
      return this.isShiftKeyDown() && !Slide.sliding;
   }
}
