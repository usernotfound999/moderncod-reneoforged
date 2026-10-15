package com.whitefood.moderncod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeModTabs {
   public static final String MAMT_TAB_STRING = "creativetab.moderncod_tab";
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "moderncod");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAMT_TAB = CREATIVE_MODE_TABS.register(
      "moderncod_tab",
      () -> CreativeModeTab.builder()
         .icon(() -> new ItemStack(Items.APPLE))
         .title(Component.translatable("creativetab.moderncod_tab"))
         .displayItems((pParameters, pOutput) -> {
            pOutput.accept((ItemLike)ModItems.SHIELD.get());
            pOutput.accept((ItemLike)ModItems.SMALL_SHIELD.get());
         })
         .build()
   );

   public ModCreativeModTabs() {
   }

   public static void register(IEventBus eventBus) {
      CREATIVE_MODE_TABS.register(eventBus);
   }
}
