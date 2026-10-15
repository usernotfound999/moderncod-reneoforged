package com.whitefood.moderncod.gameroom.net;

import com.whitefood.moderncod.gameroom.base.GameRoom;
import com.whitefood.moderncod.gameroom.manager.GameRoomManager;
import com.whitefood.moderncod.networking.ModMessages;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SRequestRoomListPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SRequestRoomListPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "request_room_list")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SRequestRoomListPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {}, buf -> new C2SRequestRoomListPacket()
   );

   public C2SRequestRoomListPacket() {
   }

   @Override
   public CustomPacketPayload.Type<C2SRequestRoomListPacket> type() {
      return TYPE;
   }

   public static void handle(C2SRequestRoomListPacket packet, IPayloadContext context) {
      if (!(context.player() instanceof ServerPlayer player)) {
         return;
      }
      ServerLevel level = player.serverLevel().getLevel();
      GameRoomManager manager = GameRoomManager.getForLevel(level);
      List<S2CRoomListPacket.RoomData> roomDataList = new ArrayList<>();

      for (GameRoom room : manager.getAllRooms()) {
         List<UUID> playerList = new ArrayList<>(room.getPlayerUUIDs());
         roomDataList.add(new S2CRoomListPacket.RoomData(room.getId(), room.getMode(), playerList, room.getPlayerUUIDs().size(), room.isRunning()));
      }

      S2CRoomListPacket response = new S2CRoomListPacket(roomDataList);
      ModMessages.sendToPlayer(response, player);
   }
}
