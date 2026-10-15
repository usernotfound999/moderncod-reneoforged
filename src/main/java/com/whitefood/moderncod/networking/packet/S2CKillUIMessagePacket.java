package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.ClientKillUIMessageHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CKillUIMessagePacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CKillUIMessagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "kill_ui_message")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CKillUIMessagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CKillUIMessagePacket::new
   );

   private final String killerName;
   private final List<Component> messages;

   public S2CKillUIMessagePacket(String killerName, List<Component> messages) {
      this.killerName = killerName;
      this.messages = messages;
   }

   public S2CKillUIMessagePacket(RegistryFriendlyByteBuf buffer) {
      this.killerName = buffer.readUtf();
      int count = buffer.readVarInt();
      this.messages = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         this.messages.add(ComponentSerialization.STREAM_CODEC.decode(buffer));
      }
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUtf(this.killerName);
      buffer.writeVarInt(this.messages.size());

      for (Component message : this.messages) {
         ComponentSerialization.STREAM_CODEC.encode(buffer, message);
      }
   }

   public String getKillerName() {
      return this.killerName;
   }

   public List<Component> getMessages() {
      return this.messages;
   }

   @Override
   public CustomPacketPayload.Type<S2CKillUIMessagePacket> type() {
      return TYPE;
   }

   public static void handle(S2CKillUIMessagePacket msg, IPayloadContext ctx) {
      ClientKillUIMessageHandler.addMessages(msg.killerName, msg.messages);
   }
}
