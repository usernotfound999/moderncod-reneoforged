package com.whitefood.moderncod.codxp;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.S2CKillUIMessagePacket;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface KillCondition {
   default boolean matches(EntityKillByGunEvent event) {
      return false;
   }

   default boolean matches(ServerPlayer killer, LivingEntity victim, DamageSource source) {
      return false;
   }

   int getXPBonus();

   Optional<Component> getSpecialText();

   default boolean shouldDisplayInUI() {
      return false;
   }

   default void sendKillUIMessage(EntityKillByGunEvent event) {
      if (this.matches(event) && this.shouldDisplayInUI() && event.getAttacker() instanceof ServerPlayer killer) {
         this.getSpecialText().ifPresent(text -> {
            MinecraftServer server = event.getKilledEntity().getServer();
            if (server != null) {
               S2CKillUIMessagePacket packet = new S2CKillUIMessagePacket(killer.getScoreboardName(), List.of(text));
               server.getPlayerList().getPlayers().forEach(player -> ModMessages.sendToPlayer(packet, player));
            }
         });
      }
   }

   default void sendKillUIMessage(ServerPlayer killer, LivingEntity victim, DamageSource source) {
      if (this.shouldDisplayInUI()) {
         this.getSpecialText().ifPresent(text -> {
            MinecraftServer server = killer.getServer();
            if (server != null) {
               S2CKillUIMessagePacket packet = new S2CKillUIMessagePacket(killer.getScoreboardName(), List.of(text));
               server.getPlayerList().getPlayers().forEach(player -> ModMessages.sendToPlayer(packet, player));
            }
         });
      }
   }
}
