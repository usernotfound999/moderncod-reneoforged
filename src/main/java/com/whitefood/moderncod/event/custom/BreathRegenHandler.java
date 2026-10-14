package com.whitefood.moderncod.event.custom;

import com.whitefood.moderncod.command.rule.ModGameRules;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.S2CPlayerSoundsPacket;
import com.whitefood.moderncod.sound.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.fml.LogicalSide;

public class BreathRegenHandler {
   private static final String TAG_LAST_HURT = "breathregen_lastHurt";
   private static final String TAG_REGEN_TICKS = "breathregen_regenTicks";
   private static final int WAIT_TICKS = 120;
   private static final int REGEN_DURATION = 40;

   public BreathRegenHandler() {
   }

   public static void register() {
      NeoForge.EVENT_BUS.addListener(BreathRegenHandler::onPlayerTick);
      NeoForge.EVENT_BUS.addListener(BreathRegenHandler::onLivingHurt);
   }

   private static void onLivingHurt(LivingIncomingDamageEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CompoundTag var5 = player.getPersistentData();
         long gameTime = player.level().getGameTime();
         var5.putLong("breathregen_lastHurt", gameTime);
         var5.remove("breathregen_regenTicks");
      }
   }

   private static void onPlayerTick(PlayerTickEvent.Post event) {
      if (event instanceof PlayerTickEvent.Post && !event.getEntity().level().isClientSide) {
         if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().getGameRules().getBoolean(ModGameRules.RULE_USE_BREATH_HEALTH)) {
               if (player.getTags().contains("CanBreathHealth")) {
                  CompoundTag data = player.getPersistentData();
                  long gameTime = player.level().getGameTime();
                  float health = player.getHealth();
                  float maxHealth = player.getMaxHealth();
                  if (!data.contains("breathregen_lastHurt")) {
                     data.putLong("breathregen_lastHurt", 0L);
                  }

                  if (data.contains("breathregen_regenTicks")) {
                     int ticksLeft = data.getInt("breathregen_regenTicks");
                     if (gameTime - data.getLong("breathregen_lastHurt") < 120L) {
                        data.remove("breathregen_regenTicks");
                     } else {
                        if (ticksLeft > 0 && health < maxHealth) {
                           float amountPerTick = (maxHealth - health) / ticksLeft;
                           player.setHealth(Math.min(maxHealth, health + amountPerTick));
                           if (!(Math.abs(player.getHealth() - maxHealth) < 0.05F) && ticksLeft > 1) {
                              data.putInt("breathregen_regenTicks", ticksLeft - 1);
                           } else {
                              data.remove("breathregen_regenTicks");
                           }
                        } else {
                           data.remove("breathregen_regenTicks");
                        }
                     }
                  } else {
                     if (health < maxHealth && gameTime - data.getLong("breathregen_lastHurt") >= 120L && !data.contains("breathregen_regenTicks")) {
                        data.putInt("breathregen_regenTicks", 40);
                        ModMessages.sendToPlayer(
                           new S2CPlayerSoundsPacket(player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.BREATHING.get()), player
                        );
                     }
                  }
               }
            }
         }
      }
   }
}
