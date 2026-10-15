package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.event.custom.CallingCardServerHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: upload a custom calling card picked from the config folder. */
public class C2SUploadCallingCardPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SUploadCallingCardPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "upload_calling_card")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SUploadCallingCardPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), C2SUploadCallingCardPacket::new
   );

   private final String playerName;
   private final String fileName;
   private final byte[] data;

   public C2SUploadCallingCardPacket(String playerName, String fileName, byte[] data) {
      this.playerName = playerName;
      this.fileName = fileName;
      this.data = data;
   }

   public C2SUploadCallingCardPacket(RegistryFriendlyByteBuf buffer) {
      this.playerName = buffer.readUtf();
      this.fileName = buffer.readUtf();
      this.data = buffer.readByteArray();
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUtf(this.playerName);
      buffer.writeUtf(this.fileName);
      buffer.writeByteArray(this.data);
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public String getFileName() {
      return this.fileName;
   }

   public byte[] getData() {
      return this.data;
   }

   @Override
   public CustomPacketPayload.Type<C2SUploadCallingCardPacket> type() {
      return TYPE;
   }

   public static void handle(C2SUploadCallingCardPacket msg, IPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer sender && sender.getGameProfile().getName().equals(msg.playerName)) {
         CallingCardServerHandler.upload(sender, msg.fileName, msg.data);
      }
   }
}
