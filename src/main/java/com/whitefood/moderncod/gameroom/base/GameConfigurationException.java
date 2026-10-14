package com.whitefood.moderncod.gameroom.base;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class GameConfigurationException extends Exception {
   private final Component errorMessage;

   public GameConfigurationException(String message) {
      this.errorMessage = Component.literal(message).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16733525)));
   }

   public GameConfigurationException(Component message) {
      this.errorMessage = message;
   }

   public Component getErrorMessage() {
      return this.errorMessage;
   }
}
