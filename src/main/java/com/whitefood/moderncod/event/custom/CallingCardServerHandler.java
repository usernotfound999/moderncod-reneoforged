package com.whitefood.moderncod.event.custom;

import com.whitefood.moderncod.ModernCodMod;
import com.whitefood.moderncod.callingcard.CallingCard;
import com.whitefood.moderncod.callingcard.CallingCardStorage;
import com.whitefood.moderncod.config.ModernCodConfig;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.S2CCallingCardDataPacket;
import com.whitefood.moderncod.networking.packet.S2CImageDataSyncPacket;
import com.whitefood.moderncod.networking.packet.S2CPlayerImagePacket;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Server-side authority for the calling-card system: validates uploads, keeps the on-disk cache
 * inside its budget, and pushes card metadata to clients.
 */
public final class CallingCardServerHandler {
   private CallingCardServerHandler() {
   }

   /** A player picked one of the built-in images. */
   public static void selectBuiltin(ServerPlayer sender, String path) {
      PlayerImageManager.setPlayerImage(sender, path);
      broadcast(sender);
   }

   /** A player uploaded a custom image from the config folder. */
   public static void upload(ServerPlayer sender, String fileName, byte[] data) {
      if (!ModernCodConfig.customCardsEnabled()) {
         warn(sender, "服务器已禁用自定义名片");
         return;
      }
      String error = CallingCard.validate(data, ModernCodConfig.maxUploadBytes());
      if (error != null) {
         ModernCodMod.LOGGER.warn("[moderncod] Rejected calling card upload from {} ({}): {}",
            sender.getGameProfile().getName(), fileName, error);
         warn(sender, error);
         return;
      }
      String hash = CallingCard.hash(data);
      CallingCardStorage.store(hash, data);
      PlayerImageManager.setPlayerImageHash(sender, hash);
      CallingCardStorage.enforceLimit(inUseHashes());
      broadcast(sender);
   }

   /** A client asked for the raw bytes of a custom card it has not cached yet. */
   public static void request(ServerPlayer sender, String hash) {
      byte[] data = CallingCardStorage.load(hash);
      if (data != null) {
         ModMessages.sendToPlayer(new S2CCallingCardDataPacket(hash, data), sender);
      }
   }

   /** Sends the subject's card to every online player (including the subject). */
   public static void broadcast(ServerPlayer subject) {
      MinecraftServer server = subject.getServer();
      if (server == null) {
         return;
      }
      S2CPlayerImagePacket packet = new S2CPlayerImagePacket(
         subject.getUUID(), PlayerImageManager.getPlayerImageHash(subject), PlayerImageManager.getPlayerImagePath(subject));
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         ModMessages.sendToPlayer(packet, player);
      }
   }

   /** Sends the target their own card plus every other online player's card (on join). */
   public static void syncTo(ServerPlayer target) {
      ModMessages.sendToPlayer(new S2CImageDataSyncPacket(
         target.getUUID(), PlayerImageManager.getPlayerImageHash(target), PlayerImageManager.getPlayerImagePath(target)), target);
      MinecraftServer server = target.getServer();
      if (server == null) {
         return;
      }
      for (ServerPlayer other : server.getPlayerList().getPlayers()) {
         if (other == target) {
            continue;
         }
         ModMessages.sendToPlayer(new S2CPlayerImagePacket(
            other.getUUID(), PlayerImageManager.getPlayerImageHash(other), PlayerImageManager.getPlayerImagePath(other)), target);
      }
   }

   private static Set<String> inUseHashes() {
      Set<String> hashes = new HashSet<>();
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String hash = PlayerImageManager.getPlayerImageHash(player);
            if (CallingCard.isCustom(hash)) {
               hashes.add(hash);
            }
         }
      }
      return hashes;
   }

   private static void warn(ServerPlayer player, String message) {
      player.sendSystemMessage(Component.literal("[名片] " + message).withStyle(ChatFormatting.RED));
   }
}
