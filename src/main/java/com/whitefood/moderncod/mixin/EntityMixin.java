package com.whitefood.moderncod.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class EntityMixin {
   @Shadow
   public int tickCount;

   public EntityMixin() {
   }

   @Shadow
   public boolean isShiftKeyDown() {
      return false;
   }
}
