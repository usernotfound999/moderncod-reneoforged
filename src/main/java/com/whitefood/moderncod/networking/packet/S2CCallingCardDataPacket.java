package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.CallingCardTextures;
import com.whitefood.moderncod.client.ClientImageDataManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: raw bytes of a custom calling card the client requested. */
public class S2CCallingCardDataPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CCallingCardDataPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "calling_card_data")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CCallingCardDataPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CCallingCardDataPacket::new
   );

   private final String hash;
   private final byte[] data;

   public S2CCallingCardDataPacket(String hash, byte[] data) {
      this.hash = hash;
      this.data = data;
   }

   public S2CCallingCardDataPacket(RegistryFriendlyByteBuf buffer) {
      this.hash = buffer.readUtf();
      this.data = buffer.readByteArray();
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUtf(this.hash);
      buffer.writeByteArray(this.data);
   }

   public String getHash() {
      return this.hash;
   }

   public byte[] getData() {
      return this.data;
   }

   @Override
   public CustomPacketPayload.Type<S2CCallingCardDataPacket> type() {
      return TYPE;
   }

   public static void handle(S2CCallingCardDataPacket msg, IPayloadContext ctx) {
      ctx.enqueueWork(() -> {
         if (CallingCardTextures.register(msg.hash, msg.data)) {
            ClientImageDataManager.onTextureLoaded(msg.hash);
         }
      });
   }
}
