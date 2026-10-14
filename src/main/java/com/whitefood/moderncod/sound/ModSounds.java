package com.whitefood.moderncod.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "moderncod");
   public static final DeferredHolder<SoundEvent, SoundEvent> FLESH_HIT = registerSoundEvents("flesh_hit");
   public static final DeferredHolder<SoundEvent, SoundEvent> KILL = registerSoundEvents("kill");
   public static final DeferredHolder<SoundEvent, SoundEvent> HEAD_HIT = registerSoundEvents("head_hit");
   public static final DeferredHolder<SoundEvent, SoundEvent> HEAD_KILL = registerSoundEvents("head_kill");
   public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_HIT = registerSoundEvents("shield_hited");
   public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BROKEN = registerSoundEvents("shield_broken");
   public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_USING = registerSoundEvents("shield_using");
   public static final DeferredHolder<SoundEvent, SoundEvent> BREATHING = registerSoundEvents("breathing");

   public ModSounds() {
   }

   private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvents(String name) {
      return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("moderncod", name)));
   }

   public static void playCustomSound(Level world, double x, double y, double z, SoundEvent sound) {
      if (world != null && sound != null) {
         world.playSound(null, x, y, z, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
      }
   }

   public static void playCustomSoundToPlayer(Player player, Level world, double x, double y, double z, SoundEvent soundEvent) {
      if (world != null && soundEvent != null) {
         world.playSound(player, x, y, z, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
      }
   }

   public static void register(IEventBus eventBus) {
      SOUND_EVENTS.register(eventBus);
   }
}
