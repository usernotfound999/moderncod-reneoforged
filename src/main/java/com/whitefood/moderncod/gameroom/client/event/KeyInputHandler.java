package com.whitefood.moderncod.gameroom.client.event;

import com.whitefood.moderncod.gameroom.client.PlayerMainGUI;
import com.whitefood.moderncod.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class KeyInputHandler {
   public KeyInputHandler() {
   }

   @SubscribeEvent
   public static void onKeyInput(Key event) {
      if (KeyBinding.FPS_MENU_KEY.consumeClick()) {
         Minecraft.getInstance().setScreen(new PlayerMainGUI());
      }
   }
}
