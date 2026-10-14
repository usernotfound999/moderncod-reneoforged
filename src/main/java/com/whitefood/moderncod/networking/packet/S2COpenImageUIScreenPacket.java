package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.screen.ImageSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2COpenImageUIScreenPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2COpenImageUIScreenPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "open_image_ui")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2COpenImageUIScreenPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {}, buf -> new S2COpenImageUIScreenPacket()
   );

   public S2COpenImageUIScreenPacket() {
   }

   @Override
   public CustomPacketPayload.Type<S2COpenImageUIScreenPacket> type() {
      return TYPE;
   }

   public static void handle(S2COpenImageUIScreenPacket msg, IPayloadContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         ImageSelectionScreen.open();
      }
   }
}
