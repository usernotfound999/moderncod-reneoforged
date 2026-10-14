package com.whitefood.moderncod.util;

public class DamageInfo {
   public float amount = 0.0F;
   public float startTick = 0.0F;
   public int color;

   public DamageInfo() {
      this.getArmorColor(0);
   }

   public void getArmorColor(int level) {
      switch (level) {
         case 1:
            this.color = 16777215;
            return;
         case 2:
            this.color = 255;
            return;
         case 3:
            this.color = 16711935;
            return;
         case 4:
            this.color = 16776960;
            return;
         default:
            this.color = 16711680;
      }
   }
}
