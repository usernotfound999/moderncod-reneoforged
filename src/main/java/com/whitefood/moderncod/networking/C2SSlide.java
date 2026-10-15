package com.whitefood.moderncod.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SSlide implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SSlide> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "c2s_slide")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SSlide> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {
         buf.writeUtf(msg.name);
         buf.writeInt(msg.action);
      },
      buf -> new C2SSlide(buf.readUtf(), buf.readInt())
   );

   private final String name;
   private final int action;

   public C2SSlide(String name, int action) {
      this.name = name;
      this.action = action;
   }

   public String getName() {
      return this.name;
   }

   public int getAction() {
      return this.action;
   }

   @Override
   public CustomPacketPayload.Type<C2SSlide> type() {
      return TYPE;
   }

   public static void handle(C2SSlide content, IPayloadContext ctx) {
      PacketDistributor.sendToAllPlayers(new S2CSlide(content.name, content.action));
   }
}
