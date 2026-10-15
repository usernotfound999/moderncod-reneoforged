package com.whitefood.moderncod.client;

import com.whitefood.moderncod.client.util.HotbarScrollLogic;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class HotbarScrollEventListener {
   public HotbarScrollEventListener() {
   }

   @SubscribeEvent
   public static void onMouseScroll(MouseScrollingEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.screen == null) {
         double scrollDelta = event.getScrollDeltaY();
         if (scrollDelta != 0.0) {
            boolean scrolled = HotbarScrollLogic.tryScrollHotbar(mc.player, scrollDelta);
            if (scrolled) {
               event.setCanceled(true);
            }
         }
      }
   }
}
