package com.whitefood.moderncod.util;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class KeyBinding {
   public static final KeyMapping WHEEL_MENU_KEY = new KeyMapping("key.moderncod.wheel_menu", 52, "key.categories.moderncod");
   public static final KeyMapping SLIDE_KEY = new KeyMapping("key.moderncod.slide", 67, "key.categories.moderncod");
   public static final KeyMapping FPS_MENU_KEY = new KeyMapping("key.moderncod.fps_menu", 66, "key.categories.moderncod");

   public KeyBinding() {
   }

   @SubscribeEvent
   public static void registerKeys(RegisterKeyMappingsEvent event) {
      event.register(WHEEL_MENU_KEY);
      event.register(SLIDE_KEY);
      event.register(FPS_MENU_KEY);
   }
}
