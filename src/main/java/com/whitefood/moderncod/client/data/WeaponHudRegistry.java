package com.whitefood.moderncod.client.data;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class WeaponHudRegistry {
   public static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/melee_icon.png");
   private static final ResourceLocation SWORD_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/sword_icon.png");
   private static final ResourceLocation AXE_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/axe_icon.png");
   private static final ResourceLocation BOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("moderncod", "textures/gui/bow_icon.png");
   private static final Map<String, ResourceLocation> weaponHudMap = new HashMap<>();

   public WeaponHudRegistry() {
   }

   public static void register(String weaponId, ResourceLocation texture) {
      weaponHudMap.put(weaponId, texture);
   }

   public static ResourceLocation get(String weaponId) {
      if (weaponId != null && !weaponId.isEmpty()) {
         ResourceLocation texture = weaponHudMap.get(weaponId);
         if (texture != null) {
            return texture;
         } else {
            String lower = weaponId.toLowerCase();
            if (lower.contains("sword")) {
               return SWORD_TEXTURE;
            } else if (lower.contains("axe")) {
               return AXE_TEXTURE;
            } else {
               return lower.contains("bow") ? BOW_TEXTURE : DEFAULT_TEXTURE;
            }
         }
      } else {
         return DEFAULT_TEXTURE;
      }
   }

   public static void initDefaults() {
      register("minecraft:diamond_sword", SWORD_TEXTURE);
      register("minecraft:iron_axe", AXE_TEXTURE);
      register("lrtactical:melee", DEFAULT_TEXTURE);
   }
}
