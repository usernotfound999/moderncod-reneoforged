package com.whitefood.moderncod.gameroom.net;

import com.whitefood.moderncod.gameroom.base.GameModeType;
import com.whitefood.moderncod.gameroom.client.PlayerGameRoomGUI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CRoomListPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CRoomListPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "room_list")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CRoomListPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.encode(buf), S2CRoomListPacket::decode
   );

   private final List<S2CRoomListPacket.RoomData> roomDataList;

   public S2CRoomListPacket(List<S2CRoomListPacket.RoomData> roomDataList) {
      this.roomDataList = roomDataList;
   }

   public void encode(RegistryFriendlyByteBuf buf) {
      buf.writeInt(this.roomDataList.size());

      for (S2CRoomListPacket.RoomData data : this.roomDataList) {
         buf.writeUtf(data.roomId());
         buf.writeEnum(data.mode());
         buf.writeInt(data.playerCount());
         buf.writeBoolean(data.isRunning());
         buf.writeInt(data.playerUuids().size());

         for (UUID uuid : data.playerUuids()) {
            buf.writeUUID(uuid);
         }
      }
   }

   public static S2CRoomListPacket decode(RegistryFriendlyByteBuf buf) {
      List<S2CRoomListPacket.RoomData> roomDataList = new ArrayList<>();
      int roomCount = buf.readInt();

      for (int i = 0; i < roomCount; i++) {
         String roomId = buf.readUtf();
         GameModeType mode = buf.readEnum(GameModeType.class);
         int playerCount = buf.readInt();
         boolean isRunning = buf.readBoolean();
         List<UUID> playerUuids = new ArrayList<>();
         int uuidCount = buf.readInt();

         for (int j = 0; j < uuidCount; j++) {
            playerUuids.add(buf.readUUID());
         }

         roomDataList.add(new S2CRoomListPacket.RoomData(roomId, mode, playerUuids, playerCount, isRunning));
      }

      return new S2CRoomListPacket(roomDataList);
   }

   public List<S2CRoomListPacket.RoomData> getRoomDataList() {
      return this.roomDataList;
   }

   @Override
   public CustomPacketPayload.Type<S2CRoomListPacket> type() {
      return TYPE;
   }

   public static void handle(S2CRoomListPacket packet, IPayloadContext ctx) {
      PlayerGameRoomGUI.PacketHandler.handle(packet, ctx);
   }

   public record RoomData(String roomId, GameModeType mode, List<UUID> playerUuids, int playerCount, boolean isRunning) {
      public RoomData {
      }
   }
}
