package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.ClientKillUIMessageHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CKillStreakPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CKillStreakPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "kill_streak")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CKillStreakPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CKillStreakPacket::new
   );

   private final int killStreak;
   private final String entityName;

   public S2CKillStreakPacket(int killStreak, String entityName) {
      this.killStreak = killStreak;
      this.entityName = entityName;
   }

   public S2CKillStreakPacket(RegistryFriendlyByteBuf buffer) {
      this.killStreak = buffer.readInt();
      this.entityName = buffer.readUtf();
   }

   public void toBytes(RegistryFriendlyByteBuf buffer) {
      buffer.writeInt(this.killStreak);
      buffer.writeUtf(this.entityName != null ? this.entityName : "");
   }

   public int getKillStreak() {
      return this.killStreak;
   }

   public String getEntityName() {
      return this.entityName;
   }

   @Override
   public CustomPacketPayload.Type<S2CKillStreakPacket> type() {
      return TYPE;
   }

   public static void handle(S2CKillStreakPacket msg, IPayloadContext ctx) {
      ClientKillUIMessageHandler.displayKillStreakMessage(msg.killStreak, msg.entityName);
   }
}
