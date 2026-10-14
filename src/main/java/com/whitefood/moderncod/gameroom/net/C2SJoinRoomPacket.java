package com.whitefood.moderncod.gameroom.net;

import com.whitefood.moderncod.gameroom.base.GameRoom;
import com.whitefood.moderncod.gameroom.manager.GameRoomManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SJoinRoomPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SJoinRoomPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "join_room")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SJoinRoomPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.encode(buf), C2SJoinRoomPacket::decode
   );

   private final String roomId;

   public C2SJoinRoomPacket(String roomId) {
      this.roomId = roomId;
   }

   public void encode(RegistryFriendlyByteBuf buf) {
      buf.writeUtf(this.roomId);
   }

   public static C2SJoinRoomPacket decode(RegistryFriendlyByteBuf buf) {
      return new C2SJoinRoomPacket(buf.readUtf());
   }

   @Override
   public CustomPacketPayload.Type<C2SJoinRoomPacket> type() {
      return TYPE;
   }

   public static void handle(C2SJoinRoomPacket packet, IPayloadContext context) {
      if (!(context.player() instanceof ServerPlayer player)) {
         return;
      }
      GameRoomManager manager = GameRoomManager.getForLevel(player.serverLevel().getLevel());
      if (manager != null) {
         GameRoom room = manager.getRoom(packet.roomId);
         if (room != null) {
            room.addPlayer(player);
         }
      }
   }
}
