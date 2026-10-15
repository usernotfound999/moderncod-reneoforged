package com.whitefood.moderncod.event.custom;

import com.whitefood.moderncod.networking.packet.C2SPlayerImagePacket;
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
         CallingCardServerHandler.selectBuiltin(sender, msg.getImagePath());
      }
   }
}
