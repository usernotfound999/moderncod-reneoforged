package com.whitefood.moderncod.gameroom.goal;

import com.whitefood.moderncod.gameroom.base.GameModeType;
import com.whitefood.moderncod.gameroom.base.GameRoom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class TeamKillGoal implements GameGoal {
   private static final int REQUIRED_KILLS = 100;
   private final Map<String, Integer> teamKills = new HashMap<>();
   private final Map<UUID, Integer> playerKills = new HashMap<>();
   private final Map<UUID, Integer> playerDeaths = new HashMap<>();

   public TeamKillGoal() {
   }

   @Override
   public boolean isCompatibleWith(GameModeType mode) {
      return mode == GameModeType.TEAM;
   }

   @Override
   public Component getDescription() {
      MutableComponent desc = Component.literal("团队竞赛：")
         .append(Component.literal(" 100 ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16733525))))
         .append("击杀获胜\n");
      if (!this.teamKills.isEmpty()) {
         desc.append(Component.literal("\n当前击杀排行榜：\n").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777045))));
         this.teamKills
            .entrySet()
            .stream()
            .sorted(Entry.<String, Integer>comparingByValue().reversed())
            .forEach(
               entry -> desc.append(
                  Component.literal("  " + entry.getKey() + ": " + entry.getValue() + " 杀\n")
                     .withStyle(Style.EMPTY.withColor(this.getColorByRank(entry.getValue())))
               )
            );
      }

      return desc;
   }

   private TextColor getColorByRank(int kills) {
      if (kills >= 100) {
         return TextColor.fromRgb(16733525);
      } else if (kills >= 15) {
         return TextColor.fromRgb(16755200);
      } else {
         return kills >= 10 ? TextColor.fromRgb(16777045) : TextColor.fromRgb(5635925);
      }
   }

   @Override
   public boolean isCompleted(GameRoom room) {
      return this.teamKills.values().stream().anyMatch(kills -> kills >= 100);
   }

   @Override
   public void onPlayerKill(ServerPlayer killer, ServerPlayer victim, GameRoom room) {
      String killerTeam = room.getPlayerTeam(killer.getUUID());
      if (killerTeam != null) {
         UUID killerId = killer.getUUID();
         UUID victimId = victim.getUUID();
         this.playerKills.put(killerId, this.playerKills.getOrDefault(killerId, 0) + 1);
         this.playerDeaths.put(victimId, this.playerDeaths.getOrDefault(victimId, 0) + 1);
         this.teamKills.put(killerTeam, this.teamKills.getOrDefault(killerTeam, 0) + 1);
         int currentKills = this.teamKills.get(killerTeam);
         if (currentKills >= 100) {
            room.stopGame();
         }
      }
   }

   @Override
   public void onTick(GameRoom room) {
   }

   @Override
   public void onGameStart(GameRoom room) {
      this.playerKills.clear();
      this.playerDeaths.clear();
      this.teamKills.clear();

      for (String teamName : room.getTeamNames()) {
         this.teamKills.put(teamName, 0);
      }
   }

   @Override
   public String getWinningTeam(GameRoom room) {
      return this.teamKills.entrySet().stream().filter(entry -> entry.getValue() >= 100).map(Entry::getKey).findFirst().orElse(null);
   }

   private TextColor getTeamColor(String teamName) {
      if (teamName.equalsIgnoreCase("RED")) {
         return TextColor.fromRgb(16733525);
      } else {
         return teamName.equalsIgnoreCase("BLUE") ? TextColor.fromRgb(5592575) : TextColor.fromRgb(16777215);
      }
   }

   public int getPlayerKills(UUID playerId) {
      return this.playerKills.getOrDefault(playerId, 0);
   }

   public int getPlayerDeaths(UUID playerId) {
      return this.playerDeaths.getOrDefault(playerId, 0);
   }

   public void setPlayerKills(UUID playerId, int kills) {
      this.playerKills.put(playerId, kills);
   }

   public void setPlayerDeaths(UUID playerId, int deaths) {
      this.playerDeaths.put(playerId, deaths);
   }

   public int getTeamKills(String teamName) {
      return this.teamKills.getOrDefault(teamName, 0);
   }

   public int getRequiredKills() {
      return 100;
   }

   public void setTeamKills(String teamName, int kills) {
      this.teamKills.put(teamName, kills);
   }
}
