package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.event.custom.C2SPlayerImagePacketHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SPlayerImagePacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SPlayerImagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "c2s_player_image")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SPlayerImagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), C2SPlayerImagePacket::new
   );

   private final String playerName;
   private final String imagePath;

   public C2SPlayerImagePacket(String playerName, String imagePath) {
      this.playerName = playerName;
      this.imagePath = imagePath;
   }

   public C2SPlayerImagePacket(RegistryFriendlyByteBuf buffer) {
      this.playerName = buffer.readUtf();
      this.imagePath = buffer.readUtf();
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUtf(this.playerName);
      buffer.writeUtf(this.imagePath);
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public String getImagePath() {
      return this.imagePath;
   }

   @Override
   public CustomPacketPayload.Type<C2SPlayerImagePacket> type() {
      return TYPE;
   }

   public static void handle(C2SPlayerImagePacket msg, IPayloadContext ctx) {
      C2SPlayerImagePacketHandler.handle(msg, ctx);
   }
}
