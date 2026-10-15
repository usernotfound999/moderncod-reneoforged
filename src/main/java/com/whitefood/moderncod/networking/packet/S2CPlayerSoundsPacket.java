package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.client.ClientPacketHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CPlayerSoundsPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<S2CPlayerSoundsPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "player_sounds")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, S2CPlayerSoundsPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), S2CPlayerSoundsPacket::new
   );

   private double x;
   private double y;
   private double z;
   private SoundEvent sound;

   public S2CPlayerSoundsPacket() {
   }

   public S2CPlayerSoundsPacket(double x, double y, double z, SoundEvent sound) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.sound = sound;
   }

   public S2CPlayerSoundsPacket(RegistryFriendlyByteBuf buf) {
      this.x = buf.readDouble();
      this.y = buf.readDouble();
      this.z = buf.readDouble();
      ResourceLocation soundLocation = buf.readResourceLocation();
      this.sound = BuiltInRegistries.SOUND_EVENT.get(soundLocation);
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      ResourceLocation soundLocation = BuiltInRegistries.SOUND_EVENT.getKey(this.sound);
      buf.writeResourceLocation(soundLocation);
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public SoundEvent getSound() {
      return this.sound;
   }

   @Override
   public CustomPacketPayload.Type<S2CPlayerSoundsPacket> type() {
      return TYPE;
   }

   public static void handle(S2CPlayerSoundsPacket msg, IPayloadContext ctx) {
      ClientPacketHandler.handlePlayerSounds(msg.x, msg.y, msg.z, msg.sound);
   }
}
