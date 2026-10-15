package com.whitefood.moderncod.gameroom.net;

import com.whitefood.moderncod.gameroom.base.GameRoom;
import com.whitefood.moderncod.gameroom.manager.GameRoomManager;
import com.whitefood.moderncod.gameroom.util.RespawnUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SLeaveRoomPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SLeaveRoomPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "leave_room")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SLeaveRoomPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.encode(buf), C2SLeaveRoomPacket::decode
   );

   private final String roomId;

   public C2SLeaveRoomPacket(String roomId) {
      this.roomId = roomId;
   }

   public void encode(RegistryFriendlyByteBuf buf) {
      buf.writeUtf(this.roomId);
   }

   public static C2SLeaveRoomPacket decode(RegistryFriendlyByteBuf buf) {
      return new C2SLeaveRoomPacket(buf.readUtf());
   }

   @Override
   public CustomPacketPayload.Type<C2SLeaveRoomPacket> type() {
      return TYPE;
   }

   public static void handle(C2SLeaveRoomPacket packet, IPayloadContext context) {
      if (!(context.player() instanceof ServerPlayer player)) {
         return;
      }
      GameRoomManager manager = GameRoomManager.getForLevel(player.serverLevel().getLevel());
      if (manager != null) {
         GameRoom room = manager.getRoom(packet.roomId);
         if (room != null) {
            room.removePlayer(player.getUUID());
            RespawnUtil.teleportToWorldSpawnAndSetRespawn(player);
         }
      }
   }
}
