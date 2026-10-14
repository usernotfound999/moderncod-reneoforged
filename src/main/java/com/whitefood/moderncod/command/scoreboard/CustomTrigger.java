package com.whitefood.moderncod.command.scoreboard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CustomTrigger extends SimpleCriterionTrigger<CustomTrigger.Instance> {
   private final ResourceLocation id;

   public CustomTrigger(String id) {
      this.id = ResourceLocation.parse(id);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   @Override
   public Codec<CustomTrigger.Instance> codec() {
      return CustomTrigger.Instance.CODEC;
   }

   public void trigger(ServerPlayer player, int amount) {
      this.trigger(player, instance -> instance.matches(amount));
   }

   public static class Instance implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<CustomTrigger.Instance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(CustomTrigger.Instance::player),
               MinMaxBounds.Ints.CODEC.optionalFieldOf("amount", MinMaxBounds.Ints.ANY).forGetter(CustomTrigger.Instance::amount)
            )
            .apply(instance, CustomTrigger.Instance::new)
      );

      private final Optional<ContextAwarePredicate> player;
      private final MinMaxBounds.Ints amount;

      public Instance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints amount) {
         this.player = player;
         this.amount = amount;
      }

      @Override
      public Optional<ContextAwarePredicate> player() {
         return this.player;
      }

      public MinMaxBounds.Ints amount() {
         return this.amount;
      }

      public boolean matches(int amount) {
         return this.amount.matches(amount);
      }
   }
}
