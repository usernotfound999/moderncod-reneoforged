package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.KillXPHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CKillXPMessagePacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CKillXPMessagePacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "kill_xp_message")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CKillXPMessagePacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CKillXPMessagePacket::new
   );

   private final int xpAmount;
   private final List<Component> specialTexts;

   public S2CKillXPMessagePacket(int xpAmount, List<Component> specialTexts) {
      this.xpAmount = xpAmount;
      this.specialTexts = specialTexts != null ? specialTexts : Collections.emptyList();
   }

   public S2CKillXPMessagePacket(RegistryFriendlyByteBuf buf) {
      this.xpAmount = buf.readInt();
      int textCount = buf.readVarInt();
      this.specialTexts = new ArrayList<>(textCount);

      for (int i = 0; i < textCount; i++) {
         this.specialTexts.add(ComponentSerialization.STREAM_CODEC.decode(buf));
      }
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeInt(this.xpAmount);
      buf.writeVarInt(this.specialTexts.size());

      for (Component text : this.specialTexts) {
         ComponentSerialization.STREAM_CODEC.encode(buf, text);
      }
   }

   public int getXpAmount() {
      return this.xpAmount;
   }

   public List<Component> getSpecialTexts() {
      return this.specialTexts;
   }

   @Override
   public CustomPacketPayload.Type<S2CKillXPMessagePacket> type() {
      return TYPE;
   }

   public static void handle(S2CKillXPMessagePacket msg, IPayloadContext ctx) {
      KillXPHandler.onKillXPReceived(msg);
   }
}
