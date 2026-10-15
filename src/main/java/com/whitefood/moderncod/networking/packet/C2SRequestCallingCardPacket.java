package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.event.custom.CallingCardServerHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: ask for the raw bytes of a custom calling card it does not have cached. */
public class C2SRequestCallingCardPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SRequestCallingCardPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "request_calling_card")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SRequestCallingCardPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), C2SRequestCallingCardPacket::new
   );

   private final String hash;

   public C2SRequestCallingCardPacket(String hash) {
      this.hash = hash;
   }

   public C2SRequestCallingCardPacket(RegistryFriendlyByteBuf buffer) {
      this.hash = buffer.readUtf();
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUtf(this.hash);
   }

   public String getHash() {
      return this.hash;
   }

   @Override
   public CustomPacketPayload.Type<C2SRequestCallingCardPacket> type() {
      return TYPE;
   }

   public static void handle(C2SRequestCallingCardPacket msg, IPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer sender) {
         CallingCardServerHandler.request(sender, msg.hash);
      }
   }
}
