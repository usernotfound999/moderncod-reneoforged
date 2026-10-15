package com.whitefood.moderncod.command.scoreboard;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCriteriaTriggers {
   public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = DeferredRegister.create(Registries.TRIGGER_TYPE, "moderncod");
   public static final DeferredHolder<CriterionTrigger<?>, CustomTrigger> COD_XP = TRIGGER_TYPES.register(
      "codxp", () -> new CustomTrigger("moderncod:codxp")
   );

   public ModCriteriaTriggers() {
   }

   public static void register(IEventBus modEventBus) {
      TRIGGER_TYPES.register(modEventBus);
   }
}
