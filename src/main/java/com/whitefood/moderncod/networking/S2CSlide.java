package com.whitefood.moderncod.networking;

import com.whitefood.moderncod.animation.Slide;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CSlide implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CSlide> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "s2c_slide")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CSlide> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> {
         buf.writeUtf(msg.name);
         buf.writeInt(msg.action);
      },
      buf -> new S2CSlide(buf.readUtf(), buf.readInt())
   );

   private final String name;
   private final int action;

   public S2CSlide(String name, int action) {
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
   public CustomPacketPayload.Type<S2CSlide> type() {
      return TYPE;
   }

   public static void handle(S2CSlide content, IPayloadContext ctx) {
      if (content.action == 1) {
         Slide.slidingPlayers.add(content.name);
      } else {
         Slide.slidingPlayers.remove(content.name);
      }
   }
}
