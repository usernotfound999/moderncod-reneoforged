package com.whitefood.moderncod.networking;

import com.whitefood.moderncod.gameroom.net.C2SJoinRoomPacket;
import com.whitefood.moderncod.gameroom.net.C2SLeaveRoomPacket;
import com.whitefood.moderncod.gameroom.net.C2SRequestRoomListPacket;
import com.whitefood.moderncod.gameroom.net.S2CRoomDataPacket;
import com.whitefood.moderncod.gameroom.net.S2CRoomListPacket;
import com.whitefood.moderncod.networking.packet.C2SMoveItemPacket;
import com.whitefood.moderncod.networking.packet.C2SPlayerImagePacket;
import com.whitefood.moderncod.networking.packet.S2CArmorBrokenIndicatorPacket;
import com.whitefood.moderncod.networking.packet.S2CArmorHitIndicatorPacket;
import com.whitefood.moderncod.networking.packet.S2CGenericKillMessagePacket;
import com.whitefood.moderncod.networking.packet.S2CImageDataSyncPacket;
import com.whitefood.moderncod.networking.packet.S2CKillMessagePacket;
import com.whitefood.moderncod.networking.packet.S2CKillStreakPacket;
import com.whitefood.moderncod.networking.packet.S2CKillUIMessagePacket;
import com.whitefood.moderncod.networking.packet.S2CKillXPMessagePacket;
import com.whitefood.moderncod.networking.packet.S2COpenImageUIScreenPacket;
import com.whitefood.moderncod.networking.packet.S2CPlayerImagePacket;
import com.whitefood.moderncod.networking.packet.S2CPlayerSoundsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all custom payloads on the mod event bus and provides convenience send helpers.
 */
@EventBusSubscriber(modid = "moderncod")
public class ModMessages {
   public ModMessages() {
   }

   @SubscribeEvent
   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar
         .playToClient(S2CRoomListPacket.TYPE, S2CRoomListPacket.STREAM_CODEC, S2CRoomListPacket::handle)
         .playToServer(C2SRequestRoomListPacket.TYPE, C2SRequestRoomListPacket.STREAM_CODEC, C2SRequestRoomListPacket::handle)
         .playToServer(C2SLeaveRoomPacket.TYPE, C2SLeaveRoomPacket.STREAM_CODEC, C2SLeaveRoomPacket::handle)
         .playToServer(C2SJoinRoomPacket.TYPE, C2SJoinRoomPacket.STREAM_CODEC, C2SJoinRoomPacket::handle)
         .playToClient(S2CRoomDataPacket.TYPE, S2CRoomDataPacket.STREAM_CODEC, S2CRoomDataPacket::handle)
         .playToClient(S2CGenericKillMessagePacket.TYPE, S2CGenericKillMessagePacket.STREAM_CODEC, S2CGenericKillMessagePacket::handle)
         .playToClient(S2CImageDataSyncPacket.TYPE, S2CImageDataSyncPacket.STREAM_CODEC, S2CImageDataSyncPacket::handle)
         .playToServer(C2SPlayerImagePacket.TYPE, C2SPlayerImagePacket.STREAM_CODEC, C2SPlayerImagePacket::handle)
         .playToServer(C2SMoveItemPacket.TYPE, C2SMoveItemPacket.STREAM_CODEC, C2SMoveItemPacket::handle)
         .playToClient(S2COpenImageUIScreenPacket.TYPE, S2COpenImageUIScreenPacket.STREAM_CODEC, S2COpenImageUIScreenPacket::handle)
         .playToClient(S2CPlayerImagePacket.TYPE, S2CPlayerImagePacket.STREAM_CODEC, S2CPlayerImagePacket::handle)
         .playToClient(S2CKillUIMessagePacket.TYPE, S2CKillUIMessagePacket.STREAM_CODEC, S2CKillUIMessagePacket::handle)
         .playToClient(S2CKillStreakPacket.TYPE, S2CKillStreakPacket.STREAM_CODEC, S2CKillStreakPacket::handle)
         .playToClient(S2CArmorHitIndicatorPacket.TYPE, S2CArmorHitIndicatorPacket.STREAM_CODEC, S2CArmorHitIndicatorPacket::handle)
         .playToClient(S2CArmorBrokenIndicatorPacket.TYPE, S2CArmorBrokenIndicatorPacket.STREAM_CODEC, S2CArmorBrokenIndicatorPacket::handle)
         .playToClient(S2CPlayerSoundsPacket.TYPE, S2CPlayerSoundsPacket.STREAM_CODEC, S2CPlayerSoundsPacket::handle)
         .playToClient(S2CKillMessagePacket.TYPE, S2CKillMessagePacket.STREAM_CODEC, S2CKillMessagePacket::handle)
         .playToClient(S2CKillXPMessagePacket.TYPE, S2CKillXPMessagePacket.STREAM_CODEC, S2CKillXPMessagePacket::handle)
         .playToClient(DamageIndicatorMessage.DamageIndicator.TYPE, DamageIndicatorMessage.DamageIndicator.STREAM_CODEC, DamageIndicatorMessage.DamageIndicator::handle)
         .playToServer(C2SSlide.TYPE, C2SSlide.STREAM_CODEC, C2SSlide::handle)
         .playToClient(S2CSlide.TYPE, S2CSlide.STREAM_CODEC, S2CSlide::handle);
   }

   public static void sendToServer(CustomPacketPayload message) {
      PacketDistributor.sendToServer(message);
   }

   public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, message);
   }

   public static void sendToAllPlayers(CustomPacketPayload message) {
      PacketDistributor.sendToAllPlayers(message);
   }
}
