package com.whitefood.moderncod;

import com.mojang.logging.LogUtils;
import com.whitefood.moderncod.client.data.WeaponHudRegistry;
import com.whitefood.moderncod.command.rule.ModGameRules;
import com.whitefood.moderncod.command.scoreboard.ModCriteriaTriggers;
import com.whitefood.moderncod.config.ModernCodConfig;
import com.whitefood.moderncod.event.custom.BreathRegenHandler;
import com.whitefood.moderncod.event.custom.KillMessageServerHandler;
import com.whitefood.moderncod.event.custom.KillXPServerHandler;
import com.whitefood.moderncod.gameroom.base.GameRoom;
import com.whitefood.moderncod.item.ModCreativeModTabs;
import com.whitefood.moderncod.item.ModItems;
import com.whitefood.moderncod.menu.WheelMenu;
import com.whitefood.moderncod.sound.ModSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ModernCodMod.MODID)
public class ModernCodMod {
   public static final String MODID = "moderncod";
   public static final Logger LOGGER = LogUtils.getLogger();

   public ModernCodMod(IEventBus modEventBus, ModContainer modContainer) {
      modEventBus.addListener(this::commonSetup);
      modContainer.registerConfig(ModConfig.Type.COMMON, ModernCodConfig.SPEC);
      ModItems.register(modEventBus);
      ModGameRules.register();
      ModCriteriaTriggers.register(modEventBus);
      ModCreativeModTabs.register(modEventBus);
      ModSounds.SOUND_EVENTS.register(modEventBus);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      NeoForge.EVENT_BUS.register(KillMessageServerHandler.class);
      NeoForge.EVENT_BUS.register(KillXPServerHandler.class);
      BreathRegenHandler.register();
      GameRoom.register();
   }

   @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
   public static class ClientModEvents {
      public ClientModEvents() {
      }

      @SubscribeEvent
      public static void onClientSetup(FMLClientSetupEvent event) {
         WheelMenu.setupClient(event);
         WeaponHudRegistry.initDefaults();
      }
   }
}
