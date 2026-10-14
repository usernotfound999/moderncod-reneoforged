package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.KillMessageHandler;
import com.whitefood.moderncod.networking.data.GenericKillMessageData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CGenericKillMessagePacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CGenericKillMessagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "generic_kill_message")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CGenericKillMessagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CGenericKillMessagePacket::new
   );

   private final GenericKillMessageData data;

   public S2CGenericKillMessagePacket(GenericKillMessageData data) {
      this.data = data;
   }

   public S2CGenericKillMessagePacket(RegistryFriendlyByteBuf buf) {
      String killerName = buf.readUtf(32767);
      String deadName = buf.readUtf(32767);
      String weaponId = buf.readUtf(32767);
      boolean isHeadKill = buf.readBoolean();
      String killerTeam = buf.readUtf(32767);
      String deadTeam = buf.readUtf(32767);
      this.data = new GenericKillMessageData(killerName, deadName, weaponId, isHeadKill, killerTeam, deadTeam);
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeUtf(this.data.getKillerName());
      buf.writeUtf(this.data.getDeadName());
      buf.writeUtf(this.data.getWeaponId());
      buf.writeBoolean(this.data.isHeadKill());
      buf.writeUtf(this.data.getKillerTeam());
      buf.writeUtf(this.data.getDeadTeam());
   }

   public GenericKillMessageData getData() {
      return this.data;
   }

   @Override
   public CustomPacketPayload.Type<S2CGenericKillMessagePacket> type() {
      return TYPE;
   }

   public static void handle(S2CGenericKillMessagePacket msg, IPayloadContext ctx) {
      try {
         KillMessageHandler.onGenericKillMessageReceived(msg);
      } catch (Exception e) {
         System.err.println("Error handling generic kill message: " + e.getMessage());
         e.printStackTrace();
      }
   }
}
