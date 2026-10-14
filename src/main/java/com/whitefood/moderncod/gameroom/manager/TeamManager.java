package com.whitefood.moderncod.gameroom.manager;

import com.whitefood.moderncod.gameroom.base.GameModeType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

public class TeamManager {
   private final Map<String, Set<UUID>> teams = new HashMap<>();
   private final Map<String, String> teamColors = new HashMap<>();

   public TeamManager() {
   }

   public void createTeam(String teamName) {
      if (!this.teams.containsKey(teamName)) {
         this.teams.put(teamName, new HashSet<>());
      }
   }

   public void createTeamsByRoomType(String roomName, GameModeType mode) {
      this.clearTeams();
      switch (mode) {
         case TEAM:
            String teamA = roomName + "A";
            String teamB = roomName + "B";
            this.createTeam(teamA);
            this.createTeam(teamB);
            this.setTeamColor(teamA, "RED");
            this.setTeamColor(teamB, "BLUE");
            break;
         case SOLO:
            String soloTeam = roomName + "A";
            this.createTeam(soloTeam);
            this.setTeamColor(soloTeam, "YELLOW");
      }
   }

   public void setTeamColor(String teamName, String color) {
      this.teamColors.put(teamName, color);
   }

   public String getTeamColor(String teamName) {
      return this.teamColors.getOrDefault(teamName, "DEFAULT");
   }

   public boolean addPlayerToTeam(String teamName, UUID playerId) {
      this.removePlayerFromAllTeams(playerId);
      return this.teams.containsKey(teamName) ? this.teams.get(teamName).add(playerId) : false;
   }

   public void removePlayerFromAllTeams(UUID playerId) {
      for (Set<UUID> teamMembers : this.teams.values()) {
         teamMembers.remove(playerId);
      }
   }

   public String findTeamWithFewestPlayers() {
      String targetTeam = null;
      int minPlayers = Integer.MAX_VALUE;

      for (String teamName : this.teams.keySet()) {
         int playerCount = this.teams.get(teamName).size();
         if (playerCount < minPlayers) {
            minPlayers = playerCount;
            targetTeam = teamName;
         }
      }

      return targetTeam;
   }

   public String getPlayerTeam(UUID playerId) {
      for (Entry<String, Set<UUID>> entry : this.teams.entrySet()) {
         if (entry.getValue().contains(playerId)) {
            return entry.getKey();
         }
      }

      return null;
   }

   public void clearTeams() {
      this.teams.clear();
      this.teamColors.clear();
   }

   public void removeTeam(String teamName) {
      this.teams.remove(teamName);
      this.teamColors.remove(teamName);
   }

   public Set<UUID> getTeamMembers(String teamName) {
      return this.teams.getOrDefault(teamName, Collections.emptySet());
   }

   public Set<String> getTeamNames() {
      return this.teams.keySet();
   }

   public Set<UUID> getPlayerTeamMembers(UUID playerId) {
      String teamName = this.getPlayerTeam(playerId);
      return teamName != null ? this.getTeamMembers(teamName) : Collections.emptySet();
   }

   public int getTeamSize(String teamName) {
      return this.teams.getOrDefault(teamName, Collections.emptySet()).size();
   }
}
