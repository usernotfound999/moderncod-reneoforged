package com.whitefood.moderncod.networking;

import com.whitefood.moderncod.client.RenderGuiHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DamageIndicatorMessage {
   public DamageIndicatorMessage() {
   }

   public static class DamageIndicator implements CustomPacketPayload {
      public static final CustomPacketPayload.Type<DamageIndicator> TYPE = new CustomPacketPayload.Type<>(
         ResourceLocation.fromNamespaceAndPath("moderncod", "damage_indicator")
      );
      public static final StreamCodec<RegistryFriendlyByteBuf, DamageIndicator> STREAM_CODEC = StreamCodec.of(
         (buf, msg) -> {
            buf.writeFloat(msg.x);
            buf.writeFloat(msg.z);
         },
         buf -> {
            DamageIndicator msg = new DamageIndicator();
            msg.x = buf.readFloat();
            msg.z = buf.readFloat();
            return msg;
         }
      );

      public float startTick = -1.0F;
      public float x;
      public float z;

      public DamageIndicator() {
      }

      @Override
      public CustomPacketPayload.Type<DamageIndicator> type() {
         return TYPE;
      }

      public static void handle(DamageIndicator msg, IPayloadContext ctx) {
         RenderGuiHandler.lastIndicator.startTick = -1.0F;
         RenderGuiHandler.lastIndicator.x = msg.x;
         RenderGuiHandler.lastIndicator.z = msg.z;
      }
   }
}
