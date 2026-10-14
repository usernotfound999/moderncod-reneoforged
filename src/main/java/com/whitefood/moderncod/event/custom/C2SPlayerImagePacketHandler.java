package com.whitefood.moderncod.event.custom;

import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.C2SPlayerImagePacket;
import com.whitefood.moderncod.networking.packet.S2CImageDataSyncPacket;
import com.whitefood.moderncod.networking.packet.S2CPlayerImagePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SPlayerImagePacketHandler {
   public C2SPlayerImagePacketHandler() {
   }

   public static void handle(C2SPlayerImagePacket msg, IPayloadContext ctx) {
      if (!(ctx.player() instanceof ServerPlayer sender)) {
         return;
      }
      if (sender.getGameProfile().getName().equals(msg.getPlayerName())) {
         PlayerImageManager.setPlayerImage(sender, msg.getImagePath());
         ModMessages.sendToPlayer(new S2CImageDataSyncPacket(sender.getUUID(), msg.getImagePath()), sender);
         S2CPlayerImagePacket broadcastPacket = new S2CPlayerImagePacket(sender.getUUID(), msg.getImagePath());
         MinecraftServer server = sender.getServer();
         if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               ModMessages.sendToPlayer(broadcastPacket, player);
            }
         }
      }
   }
}
