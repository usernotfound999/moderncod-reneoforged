package com.whitefood.moderncod.gameroom.client;

import com.whitefood.moderncod.gameroom.manager.TeamManager;
import com.whitefood.moderncod.gameroom.net.S2CRoomDataPacket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class TeamRoomTabScoreboard {
   private static final Minecraft mc = Minecraft.getInstance();
   private static boolean isTabOpen = false;
   private static long lastUpdateTime = 0L;
   private static final long UPDATE_INTERVAL = 1000L;
   public static TeamManager teamManager;
   private static String currentRoomId = "";
   private static String playerTeam = "";
   private static final Map<String, Integer> teamScores = new LinkedHashMap<>();
   private static int requiredKills = 0;
   private static final Map<String, List<TeamRoomTabScoreboard.PlayerStats>> teamPlayers = new LinkedHashMap<>();

   public TeamRoomTabScoreboard() {
   }

   public static void setTeamManager(TeamManager manager) {
      teamManager = manager;
   }

   public static void handleScoreDataPacket(S2CRoomDataPacket packet) {
      currentRoomId = packet.getString("roomId").orElse("");
      playerTeam = packet.getString("playerTeam").orElse("");
      teamScores.clear();
      teamPlayers.clear();
      int teamCount = packet.getNumber("teamCount").orElse(0).intValue();

      for (int i = 0; i < teamCount; i++) {
         String teamName = packet.getString("team" + i).orElse("未知队伍");
         int score = packet.getNumber("score" + i).orElse(0).intValue();
         teamScores.put(teamName, score);
         teamPlayers.put(teamName, new ArrayList<>());
      }

      requiredKills = packet.getNumber("requiredKills").orElse(0).intValue();
      Minecraft mc = Minecraft.getInstance();

      for (String key : packet.numberData.keySet()) {
         if (key.startsWith("playerKills_")) {
            String uuidStr = key.substring("playerKills_".length());

            try {
               UUID uuid = UUID.fromString(uuidStr);
               int kills = packet.getNumber(key).orElse(0).intValue();
               int deaths = packet.getNumber("playerDeaths_" + uuidStr).orElse(0).intValue();
               String playerName = getPlayerName(uuid);
               addPlayerToTeam(uuid, playerName, kills, deaths);
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      for (List<TeamRoomTabScoreboard.PlayerStats> players : teamPlayers.values()) {
         sortPlayersByScore(players);
      }

      lastUpdateTime = System.currentTimeMillis();
   }

   private static void addPlayerToTeam(UUID uuid, String playerName, int kills, int deaths) {
      LocalPlayer localPlayer = mc.player;
      if (localPlayer != null && localPlayer.getUUID().equals(uuid)) {
         if (playerTeam != null && !playerTeam.isEmpty() && teamPlayers.containsKey(playerTeam)) {
            teamPlayers.get(playerTeam).add(new TeamRoomTabScoreboard.PlayerStats(playerName, uuid, kills, deaths));
         }
      } else {
         String targetTeam = null;

         for (String teamName : teamPlayers.keySet()) {
            Set<UUID> members = teamManager.getTeamMembers(teamName);
            if (members != null && members.contains(uuid)) {
               targetTeam = teamName;
               break;
            }
         }

         if (targetTeam != null && teamPlayers.containsKey(targetTeam)) {
            teamPlayers.get(targetTeam).add(new TeamRoomTabScoreboard.PlayerStats(playerName, uuid, kills, deaths));
         } else {
            Iterator var9 = teamPlayers.entrySet().iterator();
            if (var9.hasNext()) {
               Entry<String, List<TeamRoomTabScoreboard.PlayerStats>> entry = (Entry<String, List<TeamRoomTabScoreboard.PlayerStats>>)var9.next();
               entry.getValue().add(new TeamRoomTabScoreboard.PlayerStats(playerName, uuid, kills, deaths));
            }
         }
      }
   }

   private static String getPlayerName(UUID uuid) {
      LocalPlayer localPlayer = mc.player;
      if (localPlayer != null && localPlayer.getUUID().equals(uuid)) {
         return localPlayer.getName().getString();
      } else if (mc.level != null) {
         Player player = mc.level.getPlayerByUUID(uuid);
         return player != null ? player.getDisplayName().getString() : "玩家" + uuid.toString().substring(0, 8);
      } else {
         return "玩家" + uuid.toString().substring(0, 8);
      }
   }

   @SubscribeEvent
   public static void onRenderTabOverlay(Pre event) {
   }

   private static void renderCustomTab(GuiGraphics graphics) {
      if (mc.player != null) {
         int screenWidth = mc.getWindow().getGuiScaledWidth();
         int screenHeight = mc.getWindow().getGuiScaledHeight();
         int padding = 8;
         int tableWidth = (int)(screenWidth * 0.5);
         int lineHeight = 9;
         int headerHeight = lineHeight + 6;
         int rowHeight = lineHeight + 3;
         int maxRows = teamPlayers.values().stream().mapToInt(List::size).max().orElse(0);
         int teamBlockHeight = headerHeight + rowHeight * maxRows + padding * 2;
         int tableHeight = teamBlockHeight * teamPlayers.size() + padding * 2;
         if (tableHeight > screenHeight - 40) {
            tableHeight = screenHeight - 40;
         }

         int x = (screenWidth - tableWidth) / 2;
         int y = (screenHeight - tableHeight) / 2;
         drawBackground(graphics, x, y, tableWidth, tableHeight);
         int infoY = y + padding;
         graphics.drawString(mc.font, "房间: " + currentRoomId, x + padding, infoY, -5592406, false);
         graphics.drawString(
            mc.font,
            "目标: 达到" + requiredKills + "击杀",
            x + tableWidth - mc.font.width("目标: 达到" + requiredKills + "击杀") - padding,
            infoY,
            -5592406,
            false
         );
         int separatorY = infoY + lineHeight + padding;
         graphics.fill(x + padding, separatorY, x + tableWidth - padding, separatorY + 1, -2130706433);
         int currentY = separatorY + padding;

         for (Entry<String, List<TeamRoomTabScoreboard.PlayerStats>> entry : teamPlayers.entrySet()) {
            String teamName = entry.getKey();
            List<TeamRoomTabScoreboard.PlayerStats> players = entry.getValue();
            int teamScore = teamScores.getOrDefault(teamName, 0);
            int teamColor = -5592406;
            if (teamName.equals(playerTeam)) {
               teamColor = -5636096;
            } else if (!teamPlayers.isEmpty()) {
               teamColor = -16777046;
            }

            int blockHeight = renderTeamBlock(
               graphics, x, currentY, tableWidth, headerHeight, rowHeight, teamName + " (总分: " + teamScore + ")", teamColor, players
            );
            currentY += blockHeight + padding;
         }
      }
   }

   private static void drawBackground(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x, y, x + width, y + height, -872415232);
      graphics.fill(x, y, x + width, y + 1, -1);
      graphics.fill(x, y + height - 1, x + width, y + height, -1);
      graphics.fill(x, y, x + 1, y + height, -1);
      graphics.fill(x + width - 1, y, x + width, y + height, -1);
   }

   private static int renderTeamBlock(
      GuiGraphics graphics,
      int x,
      int y,
      int width,
      int headerHeight,
      int rowHeight,
      String teamName,
      int teamColor,
      List<TeamRoomTabScoreboard.PlayerStats> players
   ) {
      int nameColumnWidth = (int)(width * 0.6);
      int statColumnWidth = (width - nameColumnWidth) / 3;
      graphics.drawString(mc.font, teamName, x + 10, y + 5, teamColor, false);
      int separatorY = y + headerHeight - 4;
      graphics.fill(x + 5, separatorY, x + width - 5, separatorY + 1, -2130706433);
      int headerY = y + headerHeight;
      graphics.drawString(mc.font, "玩家", x + 10, headerY, -5592406, false);
      graphics.drawString(mc.font, "击杀", x + nameColumnWidth + 5, headerY, -5592406, false);
      graphics.drawString(mc.font, "死亡", x + nameColumnWidth + statColumnWidth + 5, headerY, -5592406, false);
      graphics.drawString(mc.font, "得分", x + nameColumnWidth + statColumnWidth * 2 + 5, headerY, -5592406, false);
      drawVerticalSeparators(graphics, x, y, width, nameColumnWidth, statColumnWidth, headerHeight, players.size() * rowHeight);

      for (int i = 0; i < players.size(); i++) {
         TeamRoomTabScoreboard.PlayerStats stats = players.get(i);
         int rowY = headerY + rowHeight + i * rowHeight;
         int textColor = isCurrentPlayer(stats.uuid) ? -256 : -1;
         graphics.drawString(mc.font, stats.name, x + 10, rowY, textColor, false);
         graphics.drawString(mc.font, String.valueOf(stats.kills), x + nameColumnWidth + 5, rowY, -3355444, false);
         graphics.drawString(mc.font, String.valueOf(stats.deaths), x + nameColumnWidth + statColumnWidth + 5, rowY, -3355444, false);
         graphics.drawString(mc.font, String.valueOf(stats.score), x + nameColumnWidth + statColumnWidth * 2 + 5, rowY, getScoreColor(stats.score), false);
      }

      return headerHeight + rowHeight * players.size() + 10;
   }

   private static void drawVerticalSeparators(
      GuiGraphics graphics, int x, int y, int width, int nameColumnWidth, int statColumnWidth, int headerHeight, int contentHeight
   ) {
      int startY = y + headerHeight - 4;
      int endY = startY + contentHeight + 16;
      int separatorX1 = x + nameColumnWidth;
      graphics.fill(separatorX1, startY, separatorX1 + 1, endY, 1090519039);
      int separatorX2 = separatorX1 + statColumnWidth;
      graphics.fill(separatorX2, startY, separatorX2 + 1, endY, 1090519039);
      int separatorX3 = separatorX2 + statColumnWidth;
      graphics.fill(separatorX3, startY, separatorX3 + 1, endY, 1090519039);
   }

   private static int getScoreColor(int score) {
      if (score > 50) {
         return -16711936;
      } else if (score > 20) {
         return -256;
      } else {
         return score >= 0 ? -3355444 : -65536;
      }
   }

   private static boolean isCurrentPlayer(UUID uuid) {
      LocalPlayer player = mc.player;
      return player != null && player.getUUID().equals(uuid);
   }

   private static void sortPlayersByScore(List<TeamRoomTabScoreboard.PlayerStats> players) {
      players.sort((a, b) -> Integer.compare(b.score, a.score));
   }

   static {
      S2CRoomDataPacket.ClientRoomDataManager.registerListener(S2CRoomDataPacket.DataType.PLAYER_STATUS, TeamRoomTabScoreboard::handleScoreDataPacket);
   }

   private static class PlayerStats {
      String name;
      UUID uuid;
      int kills;
      int deaths;
      int score;

      PlayerStats(String name, UUID uuid) {
         this.name = name;
         this.uuid = uuid;
         this.kills = 0;
         this.deaths = 0;
         this.score = 0;
      }

      PlayerStats(String name, UUID uuid, int kills, int deaths) {
         this.name = name;
         this.uuid = uuid;
         this.kills = kills;
         this.deaths = deaths;
         this.score = kills * 10 - deaths * 5;
      }
   }
}
