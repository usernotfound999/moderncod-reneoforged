package com.whitefood.moderncod.animation;

import com.whitefood.moderncod.networking.C2SSlide;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.util.KeyBinding;
import com.whitefood.moderncod.util.SlideInfo;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class Slide {
   public static Set<String> slidingPlayers = new HashSet<>();
   public static boolean sliding = false;
   public static boolean release = true;
   public static Vec3 slidevec;
   public static Vec3 slowdelta;
   public static double tickCount = 0.0;

   public Slide() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Pre event) {
      if (event instanceof PlayerTickEvent.Pre) {
         if (event.getEntity() instanceof LocalPlayer player) {
            Minecraft minecraft = Minecraft.getInstance();
            Options options = minecraft.options;
            float speed = player.getSpeed();
            tickCount++;
            if (tickCount > 60.0) {
               tickCount = 60.0;
            }

            if (sliding) {
               slidevec = slidevec.add(slowdelta);
               if (!KeyBinding.SLIDE_KEY.isDown() || slidevec.multiply(1.0, 0.0, 1.0).length() < 0.1) {
                  end(player);
               }

               if (player.onGround() && options.keyJump.isDown()) {
                  jump(player);
               }

               player.setDeltaMovement(slidevec.x, player.getDeltaMovement().y, slidevec.z);
               player.input.forwardImpulse = 0.0F;
               player.input.leftImpulse = 0.0F;
            }

            if (!sliding
               && speed > 0.11
               && player.onGround()
               && KeyBinding.SLIDE_KEY.isDown()
               && release
               && (player.input.forwardImpulse != 0.0F || player.input.leftImpulse != 0.0F)) {
               start(player, speed);
            }

            if (!KeyBinding.SLIDE_KEY.isDown()) {
               release = true;
            }
         }
      }
   }

   public static void start(LocalPlayer player, float speed) {
      sliding = true;
      release = false;
      slidevec = player.getForward().multiply(1.0, 0.0, 1.0).normalize().scale(speed * tickCount / 15.0);
      tickCount = 0.0;
      slowdelta = slidevec.scale(-0.025);
      ModMessages.sendToServer(new C2SSlide(player.getScoreboardName(), 1));
      player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.6F);
   }

   public static void end(LocalPlayer player) {
      sliding = false;
      player.setDeltaMovement(0.0, player.getDeltaMovement().y, 0.0);
      ModMessages.sendToServer(new C2SSlide(player.getScoreboardName(), -1));
   }

   public static void jump(LocalPlayer player) {
      player.setOnGround(false);
      double dy = 0.5;
      if (player.hasEffect(MobEffects.JUMP)) {
         dy *= 1.2 + 0.2 * player.getEffect(MobEffects.JUMP).getAmplifier();
      }

      player.setDeltaMovement(player.getDeltaMovement().x, dy, player.getDeltaMovement().z);
   }

   public static void animationPre(PlayerModel model, Player player) {
      if (slidingPlayers.contains(player.getScoreboardName())) {
         if (player.onGround()) {
            model.riding = true;
         }
      }
   }

   public static void animationPost(PlayerModel model, Player player) {
      if (slidingPlayers.contains(player.getScoreboardName())) {
         if (player.onGround()) {
            float rot = -0.8F;
            model.leftLeg.xRot = rot;
            model.rightLeg.xRot = rot;
            model.leftPants.xRot = rot;
            model.rightPants.xRot = rot;
         }
      }
   }
}
