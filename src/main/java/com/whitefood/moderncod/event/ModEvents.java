package com.whitefood.moderncod.event;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent.Pre;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.item.ModernKineticGunItem;
import com.whitefood.moderncod.command.OpenImageUICommands;
import com.whitefood.moderncod.event.custom.KillStreakTrackerHandler;
import com.whitefood.moderncod.event.custom.PlayerImageManager;
import com.whitefood.moderncod.gameroom.command.FpsCommand;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.S2CArmorBrokenIndicatorPacket;
import com.whitefood.moderncod.networking.packet.S2CArmorHitIndicatorPacket;
import com.whitefood.moderncod.networking.packet.S2CImageDataSyncPacket;
import com.whitefood.moderncod.networking.packet.S2CPlayerImagePacket;
import com.whitefood.moderncod.networking.packet.S2CPlayerSoundsPacket;
import com.whitefood.moderncod.sound.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.command.ConfigCommand;

@EventBusSubscriber(modid = "moderncod")
public class ModEvents {
   private static final String TAG_LAST_HURT = "breathregen_lastHurt";
   private static final String TAG_REGEN_TICKS = "breathregen_regenTicks";
   private static final String TAG_REGEN_SOUND_PLAYING = "breathregen_soundPlaying";

   public ModEvents() {
   }

   @SubscribeEvent
   public static void onPlayerHurtByGun(Pre event) {
      if (event.getHurtEntity() instanceof LivingEntity hurtEntity) {
         if (event.getAttacker() instanceof ServerPlayer attackerPlayer) {
            if (attackerPlayer.getMainHandItem().getItem() instanceof IGun) {
               float absorptionBeforeHit = hurtEntity.getAbsorptionAmount();
               float damage;
               if (event.isHeadShot()) {
                  damage = event.getAmount();
               } else {
                  damage = event.getBaseAmount();
               }

               if (absorptionBeforeHit > 0.0F) {
                  ModMessages.sendToPlayer(
                     new S2CPlayerSoundsPacket(
                        attackerPlayer.getX(), attackerPlayer.getY(), attackerPlayer.getZ(), (SoundEvent)ModSounds.SHIELD_HIT.get()
                     ),
                     attackerPlayer
                  );
                  ModMessages.sendToPlayer(new S2CArmorHitIndicatorPacket(), attackerPlayer);
                  if (absorptionBeforeHit <= damage) {
                     ModMessages.sendToPlayer(new S2CArmorBrokenIndicatorPacket(), attackerPlayer);
                     ModMessages.sendToPlayer(
                        new S2CPlayerSoundsPacket(
                           attackerPlayer.getX(), attackerPlayer.getY(), attackerPlayer.getZ(), (SoundEvent)ModSounds.SHIELD_BROKEN.get()
                        ),
                        attackerPlayer
                     );
                  }
               } else if (event.isHeadShot()) {
                  ModMessages.sendToPlayer(
                     new S2CPlayerSoundsPacket(
                        attackerPlayer.getX(), attackerPlayer.getY(), attackerPlayer.getZ(), (SoundEvent)ModSounds.HEAD_HIT.get()
                     ),
                     attackerPlayer
                  );
               } else {
                  ModMessages.sendToPlayer(
                     new S2CPlayerSoundsPacket(
                        attackerPlayer.getX(), attackerPlayer.getY(), attackerPlayer.getZ(), (SoundEvent)ModSounds.FLESH_HIT.get()
                     ),
                     attackerPlayer
                  );
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerKilled(LivingDeathEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
         ItemStack mainHand = serverPlayer.getMainHandItem();
         Item item = mainHand.getItem();
         if (item instanceof ModernKineticGunItem) {
            return;
         }

         ModMessages.sendToPlayer(
            new S2CPlayerSoundsPacket(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), (SoundEvent)ModSounds.KILL.get()),
            serverPlayer
         );
      }
   }

   @SubscribeEvent
   public static void onPlayerKillByGun(EntityKillByGunEvent event) {
      if (event.getAttacker() instanceof ServerPlayer attackerPlayer) {
         Level world = attackerPlayer.level();
         if (attackerPlayer.getMainHandItem().getItem() instanceof IGun gunItem) {
            if (event.isHeadShot()) {
               ModMessages.sendToPlayer(
                  new S2CPlayerSoundsPacket(
                     attackerPlayer.getX(), attackerPlayer.getY(), attackerPlayer.getZ(), (SoundEvent)ModSounds.HEAD_KILL.get()
                  ),
                  attackerPlayer
               );
            } else {
               ModMessages.sendToPlayer(
                  new S2CPlayerSoundsPacket(attackerPlayer.getX(), attackerPlayer.getY(), attackerPlayer.getZ(), (SoundEvent)ModSounds.KILL.get()),
                  attackerPlayer
               );
            }
         }
      }
   }

   @SubscribeEvent
   public static void onCommandsRegister(RegisterCommandsEvent event) {
      new OpenImageUICommands(event.getDispatcher());
      FpsCommand.register(event.getDispatcher());
      ConfigCommand.register(event.getDispatcher());
   }

   @SubscribeEvent
   public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof Player player) {
         CompoundTag data = player.getPersistentData();
         data.putLong("breathregen_lastHurt", 0L);
         data.remove("breathregen_regenTicks");
         data.putBoolean("breathregen_soundPlaying", false);
         if (event.getEntity() instanceof Player) {
            KillStreakTrackerHandler.resetKillStreak(event.getEntity());
         }

         if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer newPlayer) {
            ModMessages.sendToPlayer(new S2CImageDataSyncPacket(newPlayer.getUUID(), PlayerImageManager.getPlayerImage(newPlayer)), newPlayer);
            MinecraftServer server = newPlayer.getServer();
            if (server != null) {
               for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
                  if (onlinePlayer != newPlayer) {
                     ModMessages.sendToPlayer(new S2CPlayerImagePacket(onlinePlayer.getUUID(), PlayerImageManager.getPlayerImage(onlinePlayer)), newPlayer);
                  }
               }
            }
         }
      }
   }
}
