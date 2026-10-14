package com.whitefood.moderncod.mixin;

import com.tacz.guns.client.gui.overlay.KillAmountOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

@OnlyIn(Dist.CLIENT)
@Mixin(KillAmountOverlay.class)
public class KillAmountOverlayMixin implements LayeredDraw.Layer {
   public KillAmountOverlayMixin() {
   }

   @Override
   public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
   }
}
