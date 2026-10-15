package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.ClientArmorHitHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CArmorHitIndicatorPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CArmorHitIndicatorPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "armor_hit_indicator")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CArmorHitIndicatorPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {}, buf -> new S2CArmorHitIndicatorPacket()
   );

   public S2CArmorHitIndicatorPacket() {
   }

   @Override
   public CustomPacketPayload.Type<S2CArmorHitIndicatorPacket> type() {
      return TYPE;
   }

   public static void handle(S2CArmorHitIndicatorPacket msg, IPayloadContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         ClientArmorHitHandler.showArmorHitIndicator();
      }
   }
}
