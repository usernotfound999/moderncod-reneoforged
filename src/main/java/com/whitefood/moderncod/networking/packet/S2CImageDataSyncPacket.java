package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.ModernCodMod;
import com.whitefood.moderncod.client.ClientImageDataManager;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CImageDataSyncPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CImageDataSyncPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "image_data_sync")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CImageDataSyncPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CImageDataSyncPacket::new
   );

   private final UUID playerUUID;
   private final String imagePath;

   public S2CImageDataSyncPacket(UUID playerUUID, String imagePath) {
      this.playerUUID = playerUUID;
      this.imagePath = imagePath;
   }

   public S2CImageDataSyncPacket(RegistryFriendlyByteBuf buf) {
      this.playerUUID = buf.readUUID();
      this.imagePath = buf.readUtf();
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeUUID(this.playerUUID);
      buf.writeUtf(this.imagePath);
   }

   @Override
   public CustomPacketPayload.Type<S2CImageDataSyncPacket> type() {
      return TYPE;
   }

   public static void handle(S2CImageDataSyncPacket msg, IPayloadContext ctx) {
      ModernCodMod.LOGGER.info("客户端接收到玩家 {} 的图片路径: {}", msg.playerUUID, msg.imagePath);
      ClientImageDataManager.setPlayerImage(msg.playerUUID, msg.imagePath);
   }
}
