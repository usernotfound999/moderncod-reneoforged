package com.whitefood.moderncod.item.model;

import com.whitefood.moderncod.item.custom.ItemShield;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelShield extends GeoModel<ItemShield> {
   public ModelShield() {
   }

   public ResourceLocation getModelResource(ItemShield animatable) {
      return ResourceLocation.fromNamespaceAndPath("moderncod", "geo/shield.geo.json");
   }

   public ResourceLocation getTextureResource(ItemShield animatable) {
      return ResourceLocation.fromNamespaceAndPath("moderncod", "textures/item/shield_in_hand.png");
   }

   public ResourceLocation getAnimationResource(ItemShield animatable) {
      return ResourceLocation.fromNamespaceAndPath("moderncod", "animations/shield.animation.json");
   }
}
