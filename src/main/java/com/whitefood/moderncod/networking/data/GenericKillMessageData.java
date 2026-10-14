package com.whitefood.moderncod.networking.data;

public class GenericKillMessageData {
   private final String killerName;
   private final String deadName;
   private final String weaponId;
   private final boolean isHeadKill;
   private final String killerTeam;
   private final String deadTeam;

   public GenericKillMessageData(String killerName, String deadName, String weaponId, boolean isHeadKill, String killerTeam, String deadTeam) {
      this.killerName = killerName;
      this.deadName = deadName;
      this.weaponId = weaponId;
      this.isHeadKill = isHeadKill;
      this.killerTeam = killerTeam;
      this.deadTeam = deadTeam;
   }

   public String getKillerName() {
      return this.killerName;
   }

   public String getDeadName() {
      return this.deadName;
   }

   public String getWeaponId() {
      return this.weaponId;
   }

   public boolean isHeadKill() {
      return this.isHeadKill;
   }

   public String getKillerTeam() {
      return this.killerTeam;
   }

   public String getDeadTeam() {
      return this.deadTeam;
   }
}
