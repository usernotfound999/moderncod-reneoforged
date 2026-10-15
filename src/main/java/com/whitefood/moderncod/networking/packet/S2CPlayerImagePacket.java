package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.callingcard.CallingCard;
import com.whitefood.moderncod.client.ClientImageDataManager;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> client: another player's currently equipped calling card (metadata only).
 *
 * <p>When {@code hash} is non-empty the client will request the raw bytes through
 * {@code C2SRequestCallingCardPacket} unless it already has that texture cached.</p>
 */
public class S2CPlayerImagePacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CPlayerImagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "player_image")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CPlayerImagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CPlayerImagePacket::new
   );

   private final UUID playerUUID;
   private final String hash;
   private final String builtinPath;

   public S2CPlayerImagePacket(UUID playerUUID, String hash, String builtinPath) {
      this.playerUUID = playerUUID;
      this.hash = hash != null ? hash : CallingCard.NO_HASH;
      this.builtinPath = builtinPath != null ? builtinPath : CallingCard.DEFAULT_IMAGE.getPath();
   }

   public S2CPlayerImagePacket(RegistryFriendlyByteBuf buffer) {
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
   public CustomPacketPayload.Type<S2CPlayerImagePacket> type() {
      return TYPE;
   }

   public static void handle(S2CPlayerImagePacket msg, IPayloadContext ctx) {
      ctx.enqueueWork(() -> apply(msg.playerUUID, msg.hash, msg.builtinPath));
   }

   static void apply(UUID uuid, String hash, String builtinPath) {
      if (CallingCard.isCustom(hash)) {
         ClientImageDataManager.setCustom(uuid, hash);
      } else {
         ClientImageDataManager.setBuiltin(uuid, builtinPath);
      }
   }
}
