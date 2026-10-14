package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.ClientImageDataManager;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CPlayerImagePacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CPlayerImagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "player_image")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CPlayerImagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CPlayerImagePacket::new
   );

   private final UUID playerUUID;
   private final String imagePath;

   public S2CPlayerImagePacket(UUID playerUUID, String imagePath) {
      this.playerUUID = playerUUID;
      this.imagePath = imagePath;
   }

   public S2CPlayerImagePacket(RegistryFriendlyByteBuf buffer) {
      this.playerUUID = buffer.readUUID();
      this.imagePath = buffer.readUtf();
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUUID(this.playerUUID);
      buffer.writeUtf(this.imagePath);
   }

   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   public String getImagePath() {
      return this.imagePath;
   }

   @Override
   public CustomPacketPayload.Type<S2CPlayerImagePacket> type() {
      return TYPE;
   }

   public static void handle(S2CPlayerImagePacket msg, IPayloadContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         ClientImageDataManager.setPlayerImage(msg.playerUUID, msg.imagePath);
      }
   }
}
