package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.callingcard.CallingCard;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: the receiving player's own calling card (sent on join / after a change). */
public class S2CImageDataSyncPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CImageDataSyncPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "image_data_sync")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CImageDataSyncPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CImageDataSyncPacket::new
   );

   private final UUID playerUUID;
   private final String hash;
   private final String builtinPath;

   public S2CImageDataSyncPacket(UUID playerUUID, String hash, String builtinPath) {
      this.playerUUID = playerUUID;
      this.hash = hash != null ? hash : CallingCard.NO_HASH;
      this.builtinPath = builtinPath != null ? builtinPath : CallingCard.DEFAULT_IMAGE.getPath();
   }

   public S2CImageDataSyncPacket(RegistryFriendlyByteBuf buffer) {
      this.playerUUID = buffer.readUUID();
      this.hash = buffer.readUtf();
      this.builtinPath = buffer.readUtf();
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeUUID(this.playerUUID);
      buffer.writeUtf(this.hash);
      buffer.writeUtf(this.builtinPath);
   }

   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   public String getHash() {
      return this.hash;
   }

   public String getBuiltinPath() {
      return this.builtinPath;
   }

   @Override
   public CustomPacketPayload.Type<S2CImageDataSyncPacket> type() {
      return TYPE;
   }

   public static void handle(S2CImageDataSyncPacket msg, IPayloadContext ctx) {
      ctx.enqueueWork(() -> S2CPlayerImagePacket.apply(msg.playerUUID, msg.hash, msg.builtinPath));
   }
}
