package com.whitefood.moderncod.command.rule;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.Key;

public class ModGameRules {
   public static final Key<BooleanValue> RULE_USE_BREATH_HEALTH = GameRules.register("useBreathHealth", Category.PLAYER, BooleanValue.create(true));

   public ModGameRules() {
   }

   public static void register() {
   }
}
