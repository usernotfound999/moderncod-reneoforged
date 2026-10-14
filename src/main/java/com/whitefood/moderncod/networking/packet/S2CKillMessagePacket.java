package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.KillMessageHandler;
import com.whitefood.moderncod.networking.data.KillMessageData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CKillMessagePacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CKillMessagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "kill_message")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CKillMessagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CKillMessagePacket::new
   );

   private final KillMessageData killMessageData;

   public S2CKillMessagePacket(KillMessageData killMessageData) {
      this.killMessageData = killMessageData;
   }

   public S2CKillMessagePacket(RegistryFriendlyByteBuf buf) {
      String killerName = buf.readUtf(32767);
      String deadName = buf.readUtf(32767);
      ResourceLocation gunId = ResourceLocation.parse(buf.readUtf(32767));
      boolean isHeadKill = buf.readBoolean();
      String killerTeam = buf.readUtf(32767);
      String deadTeam = buf.readUtf(32767);
      this.killMessageData = new KillMessageData(killerName, deadName, gunId, isHeadKill, killerTeam, deadTeam);
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeUtf(this.killMessageData.getKillerName());
      buf.writeUtf(this.killMessageData.getDeadName());
      buf.writeUtf(this.killMessageData.getGunId().toString());
      buf.writeBoolean(this.killMessageData.isHeadKill());
      buf.writeUtf(this.killMessageData.getKillerTeam());
      buf.writeUtf(this.killMessageData.getDeadTeam());
   }

   public KillMessageData getKillMessageData() {
      return this.killMessageData;
   }

   @Override
   public CustomPacketPayload.Type<S2CKillMessagePacket> type() {
      return TYPE;
   }

   public static void handle(S2CKillMessagePacket msg, IPayloadContext ctx) {
      try {
         KillMessageHandler.onKillMessageReceived(msg);
      } catch (Exception e) {
         System.err.println("Error handling kill message: " + e.getMessage());
         e.printStackTrace();
      }
   }
}
