package com.whitefood.moderncod.item;

import com.whitefood.moderncod.sound.ModSounds;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BaseItemShield extends Item implements GeoItem {
   private static final RawAnimation USE_ANIM = RawAnimation.begin().thenPlay("use");
   private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
   private static final RawAnimation DRAW_ANIM = RawAnimation.begin().thenPlay("draw");
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private final Map<UUID, BaseItemShield.ShieldUseState> animStates = new WeakHashMap<>();
   public static ItemDisplayContext transformType;

   public void getTransformType(ItemDisplayContext type) {
      transformType = type;
   }

   public BaseItemShield(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   public void ensureIdleAnimation(Player player, ItemStack itemInHand) {
      UUID playerId = player.getUUID();
      if (!this.animStates.containsKey(playerId)) {
         this.animStates.put(playerId, new BaseItemShield.ShieldUseState());
         this.triggerAnim(player, GeoItem.getId(itemInHand), "main_controller", "idle");
      }
   }

   public abstract boolean canStartUsing(Player var1);

   protected abstract void onUseCompleted(Level var1, Player var2, ItemStack var3);

   public abstract String getNBTKey();

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "main_controller", 0, this::predicate)
               .triggerableAnim("use", USE_ANIM)
               .triggerableAnim("idle", IDLE_ANIM)
               .triggerableAnim("draw", DRAW_ANIM)
         }
      );
   }

   private PlayState predicate(AnimationState<?> state) {
      Player player = (Player)state.getData(DataTickets.ENTITY);
      if (player != null && player.level().isClientSide) {
         CompoundTag tag = player.getPersistentData();
         boolean isUsing = tag.getBoolean(this.getNBTKey());
         int usingSlot = tag.getInt("ShieldUsingSlot");
         ItemStack expectedStack = player.getInventory().getItem(usingSlot);
         BaseItemShield.ShieldUseState animState = this.animStates.computeIfAbsent(player.getUUID(), u -> new BaseItemShield.ShieldUseState());
         AnimationController<?> controller = state.getController();
         boolean valid = isUsing && player.getInventory().selected == usingSlot && ItemStack.isSameItemSameComponents(player.getMainHandItem(), expectedStack);
         if (valid && !animState.wasUsing) {
            controller.forceAnimationReset();
            controller.setAnimation(USE_ANIM);
            animState.wasUsing = true;
         } else if (!valid && animState.wasUsing) {
            controller.forceAnimationReset();
            controller.setAnimation(IDLE_ANIM);
            animState.wasUsing = false;
         } else if (!animState.initialized) {
            controller.forceAnimationReset();
            controller.setAnimation(IDLE_ANIM);
            animState.initialized = true;
            animState.wasUsing = valid;
         }

         return PlayState.CONTINUE;
      } else {
         return PlayState.STOP;
      }
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResultHolder.fail(player.getItemInHand(hand));
      }

      ItemStack stack = player.getMainHandItem();
      CompoundTag tag = player.getPersistentData();
      String key = this.getNBTKey();
      boolean isUsing = tag.getBoolean(key);
      if (isUsing) {
         this.stopUsing(world, player, true);
         return InteractionResultHolder.success(stack);
      }

      if (!this.canStartUsing(player)) {
         return InteractionResultHolder.fail(stack);
      }

      tag.putBoolean(key, true);
      tag.putLong("ShieldStartTime", world.getGameTime());
      tag.putInt("ShieldUsingSlot", player.getInventory().selected);
      if (!world.isClientSide) {
         world.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.SHIELD_USING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      } else {
         this.triggerAnim(player, GeoItem.getId(stack), "main_controller", "draw");
         this.triggerAnim(player, GeoItem.getId(stack), "main_controller", "use");
      }

      return InteractionResultHolder.consume(stack);
   }

   protected void stopUsing(Level world, Player player, boolean playDraw) {
      CompoundTag tag = player.getPersistentData();
      tag.remove(this.getNBTKey());
      tag.remove("ShieldStartTime");
      tag.remove("ShieldUsingSlot");
      BaseItemShield.ShieldUseState state = this.animStates.get(player.getUUID());
      if (state != null) {
         state.reset();
      }

      if (world.isClientSide && playDraw) {
         this.triggerAnim(player, GeoItem.getId(player.getMainHandItem()), "main_controller", "draw");
      }

      if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
         serverPlayer.connection.send(new ClientboundStopSoundPacket(((SoundEvent)ModSounds.SHIELD_USING.get()).getLocation(), SoundSource.PLAYERS));
      }
   }

   public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
      if (entity instanceof Player player) {
         CompoundTag tag = player.getPersistentData();
         String key = this.getNBTKey();
         boolean isUsing = tag.getBoolean(key);
         long startTime = tag.getLong("ShieldStartTime");
         int usingSlot = tag.getInt("ShieldUsingSlot");
         boolean isCurrent = slot == usingSlot && stack.getItem() == this;
         if (isUsing && !this.canStartUsing(player)) {
            this.stopUsing(world, player, true);
         } else if (!isUsing || isCurrent) {
            if (isUsing && !player.getMainHandItem().equals(stack)) {
               this.stopUsing(world, player, true);
            } else {
               if (isUsing && world.getGameTime() - startTime >= 44L) {
                  this.onUseCompleted(world, player, stack);
                  this.stopUsing(world, player, true);
               }
            }
         }
      }
   }

   private static class ShieldUseState {
      boolean wasUsing = false;
      boolean initialized = false;
      boolean wasInMainHand = false;

      private ShieldUseState() {
      }

      void reset() {
         this.wasUsing = false;
         this.initialized = false;
         this.wasInMainHand = false;
      }
   }
}
