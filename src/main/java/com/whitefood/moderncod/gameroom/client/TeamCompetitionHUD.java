package com.whitefood.moderncod.gameroom.client;

import com.whitefood.moderncod.gameroom.net.S2CRoomDataPacket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "moderncod", value = Dist.CLIENT)
public class TeamCompetitionHUD {
   private static String currentRoomId = null;
   private static String playerTeam = null;
   private static final Map<String, Integer> teamScores = new LinkedHashMap<>();
   private static int requiredKills = 100;
   private static boolean isPlayerInRoom = false;
   private static long remainingTimeMillis = 0L;

   public TeamCompetitionHUD() {
   }

   private static void registerDataListeners() {
      try {
         S2CRoomDataPacket.ClientRoomDataManager.registerListener(S2CRoomDataPacket.DataType.TIME_DATA, packet -> {
            isPlayerInRoom = true;
            currentRoomId = packet.getRoomId();
            remainingTimeMillis = packet.getNumber("remainingTimeMillis").map(Number::longValue).orElse(0L);
            triggerHUDUpdate();
         });
         S2CRoomDataPacket.ClientRoomDataManager.registerListener(S2CRoomDataPacket.DataType.SCORE_DATA, TeamCompetitionHUD::handleScoreDataUpdate);
         S2CRoomDataPacket.ClientRoomDataManager.registerListener(S2CRoomDataPacket.DataType.PLAYER_LEAVE_ROOM, packet -> {
            if (packet.getRoomId().equals(currentRoomId)) {
               resetRoomData();
            }
         });
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @SubscribeEvent
   public static void onRenderGui(Post event) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null) {
         if (isPlayerInRoom && currentRoomId != null && playerTeam != null && !teamScores.isEmpty()) {
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            renderHUD(event.getGuiGraphics(), screenWidth, screenHeight);
            renderRemainingTime(event.getGuiGraphics(), screenWidth);
         }
      }
   }

   private static void renderHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
      int backgroundHeight = calculateBackgroundHeight();
      renderBackground(guiGraphics, screenWidth, backgroundHeight);
      renderTitle(guiGraphics, screenWidth);
      renderScores(guiGraphics, screenWidth);
      renderGoalInfo(guiGraphics, screenWidth, backgroundHeight);
   }

   private static int calculateBackgroundHeight() {
      int teamCount = teamScores.size();
      int rows = (teamCount + 1) / 2;
      return 25 + rows * 15 + 15 + 10;
   }

   private static void renderBackground(GuiGraphics guiGraphics, int screenWidth, int backgroundHeight) {
      int centerX = screenWidth / 2;
      int boxWidth = 300;
      int boxX = centerX - boxWidth / 2;
      int boxY = 10;
      guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + backgroundHeight, -872415232);
      guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, -1);
      guiGraphics.fill(boxX, boxY + backgroundHeight - 1, boxX + boxWidth, boxY + backgroundHeight, -1);
   }

   private static void renderScores(GuiGraphics guiGraphics, int screenWidth) {
      Minecraft mc = Minecraft.getInstance();
      int centerX = screenWidth / 2;
      int startY = 30;
      int index = 0;

      for (Entry<String, Integer> entry : teamScores.entrySet()) {
         String teamName = entry.getKey();
         int score = entry.getValue();
         String scoreText = String.format("%s: %d (%d%%)", teamName, score, calculateProgressPercentage(score));
         int color = teamName.equals(playerTeam) ? 65535 : 16777215;
         int row = index / 2;
         int y = startY + row * 15;
         int textWidth = mc.font.width(scoreText);
         int x = index % 2 == 0 ? centerX - 10 - textWidth : centerX + 10;
         guiGraphics.drawString(mc.font, scoreText, x, y, color, false);
         index++;
      }
   }

   private static void renderTitle(GuiGraphics guiGraphics, int screenWidth) {
      Minecraft mc = Minecraft.getInstance();
      Component title = Component.literal("团队竞赛 - " + currentRoomId);
      int centerX = screenWidth / 2;
      int titleX = centerX - mc.font.width(title) / 2;
      guiGraphics.drawString(mc.font, title, titleX, 15, 16776960, false);
   }

   private static void renderGoalInfo(GuiGraphics guiGraphics, int screenWidth, int backgroundHeight) {
      Minecraft mc = Minecraft.getInstance();
      Component goalInfo = Component.literal("目标: 率先达到 " + requiredKills + " 击杀");
      int centerX = screenWidth / 2;
      int goalX = centerX - mc.font.width(goalInfo) / 2;
      int goalY = backgroundHeight - 20;
      guiGraphics.drawString(mc.font, goalInfo, goalX, goalY, 65280, false);
   }

   private static void renderRemainingTime(GuiGraphics guiGraphics, int screenWidth) {
      if (remainingTimeMillis > 0L) {
         Minecraft mc = Minecraft.getInstance();
         Font font = mc.font;
         long totalSeconds = remainingTimeMillis / 1000L;
         long minutes = totalSeconds / 60L;
         long seconds = totalSeconds % 60L;
         String timeText = String.format("剩余时间：%02d:%02d", minutes, seconds);
         int x = screenWidth - font.width(timeText) - 10;
         int y = 10;
         int paddingX = 5;
         int paddingY = 2;
         int bgColor = 100663296;
         guiGraphics.fill(x - paddingX, y - paddingY, x + font.width(timeText) + paddingX, y + 9 + paddingY, bgColor);
         guiGraphics.drawString(font, timeText, x, y, 16777215, false);
      }
   }

   private static void handleScoreDataUpdate(S2CRoomDataPacket packet) {
      isPlayerInRoom = true;
      currentRoomId = packet.getRoomId();
      playerTeam = packet.getString("playerTeam").orElse(null);
      if (playerTeam != null && !playerTeam.isEmpty()) {
         teamScores.clear();
         int teamCount = packet.getNumber("teamCount").orElse(0).intValue();

         for (int i = 0; i < teamCount; i++) {
            String teamName = packet.getString("team" + i).orElse("未知队伍 " + i);
            int score = packet.getNumber("score" + i).orElse(0).intValue();
            teamScores.put(teamName, score);
         }

         requiredKills = packet.getNumber("requiredKills").orElse(100).intValue();
      }
   }

   private static int calculateProgressPercentage(int currentKills) {
      if (requiredKills <= 0) {
         return 0;
      }

      int percentage = currentKills * 100 / requiredKills;
      return Math.min(percentage, 100);
   }

   public static void resetRoomData() {
      currentRoomId = null;
      playerTeam = null;
      teamScores.clear();
      requiredKills = 100;
      remainingTimeMillis = 0L;
      isPlayerInRoom = false;
      triggerHUDUpdate();
   }

   private static void triggerHUDUpdate() {
      Minecraft.getInstance().getProfiler().push("HUDUpdate");
      Minecraft.getInstance().getProfiler().pop();
   }

   static {
      registerDataListeners();
   }
}
