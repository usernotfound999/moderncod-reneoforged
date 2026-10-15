package com.whitefood.moderncod.gameroom.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CRoomDataPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CRoomDataPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "room_data")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CRoomDataPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CRoomDataPacket::new
   );

   private final String roomId;
   private final S2CRoomDataPacket.DataType dataType;
   private final Map<String, String> stringData;
   private final Map<String, Component> componentData;
   public final Map<String, Number> numberData;

   public S2CRoomDataPacket(String roomId, S2CRoomDataPacket.DataType dataType) {
      this.roomId = roomId;
      this.dataType = dataType;
      this.stringData = new HashMap<>();
      this.componentData = new HashMap<>();
      this.numberData = new HashMap<>();
   }

   public S2CRoomDataPacket(RegistryFriendlyByteBuf buffer) {
      this.roomId = buffer.readUtf();
      this.dataType = buffer.readEnum(S2CRoomDataPacket.DataType.class);
      int stringCount = buffer.readVarInt();
      this.stringData = new HashMap<>(stringCount);

      for (int i = 0; i < stringCount; i++) {
         String key = buffer.readUtf();
         String value = buffer.readUtf();
         this.stringData.put(key, value);
      }

      int componentCount = buffer.readVarInt();
      this.componentData = new HashMap<>(componentCount);

      for (int i = 0; i < componentCount; i++) {
         String key = buffer.readUtf();
         Component value = ComponentSerialization.STREAM_CODEC.decode(buffer);
         this.componentData.put(key, value);
      }

      int numberCount = buffer.readVarInt();
      this.numberData = new HashMap<>(numberCount);

      for (int i = 0; i < numberCount; i++) {
         String key = buffer.readUtf();
         byte type = buffer.readByte();

         this.numberData.put(key, switch (type) {
            case 0 -> buffer.readInt();
            case 1 -> buffer.readLong();
            case 2 -> buffer.readFloat();
            case 3 -> buffer.readDouble();
            default -> 0;
         });
      }
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUtf(this.roomId);
      buffer.writeEnum(this.dataType);
      buffer.writeVarInt(this.stringData.size());

      for (Entry<String, String> entry : this.stringData.entrySet()) {
         buffer.writeUtf(entry.getKey());
         buffer.writeUtf(entry.getValue());
      }

      buffer.writeVarInt(this.componentData.size());

      for (Entry<String, Component> entry : this.componentData.entrySet()) {
         buffer.writeUtf(entry.getKey());
         ComponentSerialization.STREAM_CODEC.encode(buffer, entry.getValue());
      }

      buffer.writeVarInt(this.numberData.size());

      for (Entry<String, Number> entry : this.numberData.entrySet()) {
         buffer.writeUtf(entry.getKey());
         Number value = entry.getValue();
         if (value instanceof Integer) {
            buffer.writeByte(0);
            buffer.writeInt(value.intValue());
         } else if (value instanceof Long) {
            buffer.writeByte(1);
            buffer.writeLong(value.longValue());
         } else if (value instanceof Float) {
            buffer.writeByte(2);
            buffer.writeFloat(value.floatValue());
         } else if (value instanceof Double) {
            buffer.writeByte(3);
            buffer.writeDouble(value.doubleValue());
         }
      }
   }

   public void addString(String key, String value) {
      this.stringData.put(key, value);
   }

   public void addComponent(String key, Component value) {
      this.componentData.put(key, value);
   }

   public void addNumber(String key, Number value) {
      this.numberData.put(key, value);
   }

   public String getRoomId() {
      return this.roomId;
   }

   public S2CRoomDataPacket.DataType getDataType() {
      return this.dataType;
   }

   public Optional<String> getString(String key) {
      return Optional.ofNullable(this.stringData.get(key));
   }

   public Optional<Component> getComponent(String key) {
      return Optional.ofNullable(this.componentData.get(key));
   }

   public Optional<Number> getNumber(String key) {
      return Optional.ofNullable(this.numberData.get(key));
   }

   @Override
   public CustomPacketPayload.Type<S2CRoomDataPacket> type() {
      return TYPE;
   }

   public static void handle(S2CRoomDataPacket msg, IPayloadContext ctx) {
      S2CRoomDataPacket.ClientRoomDataManager.handleRoomData(msg);
   }

   public static class Builder {
      private final S2CRoomDataPacket packet;

      public Builder(String roomId, S2CRoomDataPacket.DataType dataType) {
         this.packet = new S2CRoomDataPacket(roomId, dataType);
      }

      public S2CRoomDataPacket.Builder addString(String key, String value) {
         this.packet.addString(key, value);
         return this;
      }

      public S2CRoomDataPacket.Builder addComponent(String key, Component value) {
         this.packet.addComponent(key, value);
         return this;
      }

      public S2CRoomDataPacket.Builder addNumber(String key, Number value) {
         this.packet.addNumber(key, value);
         return this;
      }

      public S2CRoomDataPacket build() {
         return this.packet;
      }
   }

   public static class ClientRoomDataManager {
      private static final Map<String, S2CRoomDataPacket> roomDataCache = new HashMap<>();
      private static final Map<S2CRoomDataPacket.DataType, S2CRoomDataPacket.ClientRoomDataManager.RoomDataListener> listeners = new HashMap<>();

      public ClientRoomDataManager() {
      }

      public static void handleRoomData(S2CRoomDataPacket packet) {
         roomDataCache.put(packet.getRoomId(), packet);
         S2CRoomDataPacket.ClientRoomDataManager.RoomDataListener listener = listeners.get(packet.getDataType());
         if (listener != null) {
            listener.onDataUpdated(packet);
         }
      }

      public static Optional<S2CRoomDataPacket> getRoomData(String roomId) {
         return Optional.ofNullable(roomDataCache.get(roomId));
      }

      public static void registerListener(S2CRoomDataPacket.DataType type, S2CRoomDataPacket.ClientRoomDataManager.RoomDataListener listener) {
         listeners.put(type, listener);
      }

      @FunctionalInterface
      public interface RoomDataListener {
         void onDataUpdated(S2CRoomDataPacket var1);
      }
   }

   public enum DataType {
      TIME_DATA,
      SCORE_DATA,
      PLAYER_LEAVE_ROOM,
      PLAYER_STATUS,
      GAME_STATE;

      DataType() {
      }
   }
}
