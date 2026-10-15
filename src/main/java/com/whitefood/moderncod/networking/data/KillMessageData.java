package com.whitefood.moderncod.networking.data;

import net.minecraft.resources.ResourceLocation;

public class KillMessageData {
   private final String killerName;
   private final String deadName;
   private final ResourceLocation gunId;
   private final boolean isHeadKill;
   private final String killerTeam;
   private final String deadTeam;

   public KillMessageData(String killerName, String deadName, ResourceLocation gunId, boolean isHeadKill, String killerTeam, String deadTeam) {
      this.killerName = killerName;
      this.deadName = deadName;
      this.gunId = gunId;
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

   public ResourceLocation getGunId() {
      return this.gunId;
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
