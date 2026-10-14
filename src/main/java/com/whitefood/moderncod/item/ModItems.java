package com.whitefood.moderncod.item;

import com.whitefood.moderncod.item.custom.ItemShield;
import com.whitefood.moderncod.item.custom.ItemSmallShield;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "moderncod");
   public static final DeferredHolder<Item, Item> SHIELD = ITEMS.register("shield", () -> new ItemShield(new Properties().stacksTo(64)));
   public static final DeferredHolder<Item, Item> SMALL_SHIELD = ITEMS.register("small_shield", () -> new ItemSmallShield(new Properties().stacksTo(64)));

   public ModItems() {
   }

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }
}
