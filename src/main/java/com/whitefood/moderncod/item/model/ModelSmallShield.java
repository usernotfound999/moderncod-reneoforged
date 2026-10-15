package com.whitefood.moderncod.item.model;

import com.whitefood.moderncod.item.custom.ItemSmallShield;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelSmallShield extends GeoModel<ItemSmallShield> {
   public ModelSmallShield() {
   }

   public ResourceLocation getModelResource(ItemSmallShield animatable) {
      return ResourceLocation.fromNamespaceAndPath("moderncod", "geo/small_shield.geo.json");
   }

   public ResourceLocation getTextureResource(ItemSmallShield animatable) {
      return ResourceLocation.fromNamespaceAndPath("moderncod", "textures/item/small_shield_in_hand.png");
   }

   public ResourceLocation getAnimationResource(ItemSmallShield animatable) {
      return ResourceLocation.fromNamespaceAndPath("moderncod", "animations/small_shield.animation.json");
   }
}
