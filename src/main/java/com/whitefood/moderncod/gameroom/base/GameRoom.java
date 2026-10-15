package com.whitefood.moderncod.gameroom.base;

import com.whitefood.moderncod.gameroom.client.TeamPlayerNameRenderer;
import com.whitefood.moderncod.gameroom.client.TeamRoomTabScoreboard;
import com.whitefood.moderncod.gameroom.goal.GameGoal;
import com.whitefood.moderncod.gameroom.goal.TeamKillGoal;
import com.whitefood.moderncod.gameroom.handler.FriendlyFireHandler;
import com.whitefood.moderncod.gameroom.manager.GameRoomManager;
import com.whitefood.moderncod.gameroom.manager.TeamManager;
import com.whitefood.moderncod.gameroom.net.S2CRoomDataPacket;
import com.whitefood.moderncod.networking.ModMessages;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class GameRoom {
   private final String id;
   private GameModeType mode = GameModeType.TEAM;
   private JoinMethod joinMethod = JoinMethod.GUI;
   private AABB area;
   private GameGoal goal;
   private final Set<UUID> players = ConcurrentHashMap.newKeySet();
   public final TeamManager teamManager = new TeamManager();
   private boolean isRunning = false;
   private int tickCount = 0;
   private long startTime = -1L;
   private static final int MAX_TICK_COUNT = 12000;
   private final Map<String, BlockPos> teamSpawnPoints = new ConcurrentHashMap<>();
   private final Set<UUID> offlinePlayers = ConcurrentHashMap.newKeySet();
   private ServerLevel level;

   public GameRoom(String id, ServerLevel level) {
      this.id = id;
      this.level = level;
   }

   @Deprecated
   public GameRoom(String id) {
      this(id, null);
   }

   public boolean hasPlayer(UUID playerId) {
      return this.players.contains(playerId) || this.offlinePlayers.contains(playerId);
   }

   public Set<UUID> getPlayerUUIDs() {
      return Collections.unmodifiableSet(this.players);
   }

   public void setMode(GameModeType mode) {
      this.mode = mode;
   }

   public GameModeType getMode() {
      return this.mode;
   }

   public String getId() {
      return this.id;
   }

   public void setArea(AABB area) {
      if (area == null) {
         throw new IllegalArgumentException("区域不能为null");
      }

      this.area = area;
   }

   public AABB getArea() {
      return this.area;
   }

   public Set<String> getTeamNames() {
      return this.teamManager.getTeamNames();
   }

   public void setTeamSpawnPoint(String teamName, BlockPos spawnPoint) {
      if (!this.teamManager.getTeamNames().contains(teamName)) {
         throw new IllegalArgumentException("队伍不存在: " + teamName);
      }

      if (this.area != null && !this.area.contains(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ())) {
         throw new IllegalArgumentException("重生点不在房间区域内: " + spawnPoint);
      }

      this.teamSpawnPoints.put(teamName, spawnPoint);
   }

   public BlockPos getTeamSpawnPoint(String teamName) {
      return this.teamSpawnPoints.get(teamName);
   }

   public void startGame() throws GameConfigurationException {
      if (this.isRunning) {
         throw new GameConfigurationException("游戏已在运行中！");
      }

      if (this.level == null) {
         throw new GameConfigurationException("未关联服务器世界！");
      }

      if (this.area == null) {
         throw new GameConfigurationException("房间区域未设置！请使用 /fps select setArea " + this.id);
      }

      if (this.goal == null) {
         throw new GameConfigurationException("游戏目标未设置！");
      }

      if (this.players.size() < 2) {
         throw new GameConfigurationException("玩家数量不足（至少需要2人）！");
      }

      if (this.mode == GameModeType.TEAM) {
         if (this.teamManager.getTeamNames().isEmpty()) {
            throw new GameConfigurationException("没有可用的队伍！请创建队伍");
         }

         for (String teamName : this.teamManager.getTeamNames()) {
            if (!this.teamSpawnPoints.containsKey(teamName)) {
               throw new GameConfigurationException("队伍 " + teamName + " 的重生点未设置！");
            }

            if (this.teamManager.getTeamMembers(teamName).isEmpty()) {
               throw new GameConfigurationException("队伍 " + teamName + " 没有玩家！");
            }
         }
      }

      this.isRunning = true;
      this.startTime = System.currentTimeMillis();
      this.tickCount = 0;
      this.goal.onGameStart(this);
      this.broadcastMessage(Component.literal("游戏开始！").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(5635925))));
      this.spawnPlayersToSpawnPoints();
   }

   private void spawnPlayersToSpawnPoints() {
      for (UUID playerId : this.players) {
         ServerPlayer player = this.getPlayer(playerId);
         if (player != null) {
            FriendlyFireHandler.setTeamManager(this.teamManager);
            TeamPlayerNameRenderer.setTeamManager(this.teamManager);
            TeamRoomTabScoreboard.setTeamManager(this.teamManager);
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.clearFire();
            if (this.mode == GameModeType.TEAM) {
               String teamName = this.teamManager.getPlayerTeam(playerId);
               if (teamName != null) {
                  BlockPos spawnPos = this.teamSpawnPoints.get(teamName);
                  if (spawnPos != null) {
                     ResourceKey<Level> dimensionKey = this.level.dimension();
                     player.setRespawnPosition(dimensionKey, spawnPos, 0.0F, true, false);
                     player.teleportTo(this.level, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                  }
               }
            }
         }
      }
   }

   public void broadcastMessage(Component message) {
      for (UUID playerId : this.players) {
         ServerPlayer player = this.getPlayer(playerId);
         if (player != null) {
            player.sendSystemMessage(message);
         }
      }
   }

   public void stopGame() {
      if (this.isRunning) {
         this.isRunning = false;
         this.endGame();
      }
   }

   public GameGoal getGoal() {
      return this.goal;
   }

   public void setGoal(GameGoal goal) {
      if (goal == null) {
         throw new IllegalArgumentException("游戏目标不能为null");
      }

      if (!goal.isCompatibleWith(this.mode)) {
         throw new IllegalArgumentException("目标与游戏模式不兼容: " + this.mode);
      }

      this.goal = goal;
   }

   private void tick() {
      if (this.isRunning) {
         this.tickCount++;
         if (this.tickCount % 5 == 0) {
            this.checkPlayerStatus();
            this.goal.onTick(this);
            this.closeRoomIfEmpty();
            this.sendScoreDataToClients();
            this.sendRemainingTimeToClients();
            this.sendPlayerScoreDataToClients();
            if (this.goal.isCompleted(this)) {
               this.handleGameCompletion(true);
               return;
            }

            if (this.tickCount >= 12000) {
               this.handleGameCompletion(false);
            }
         }
      }
   }

   private void handleGameCompletion(boolean isGoalCompleted) {
      this.stopGame();
      String message = isGoalCompleted ? "游戏结束！目标已完成！" : "游戏结束！时间已到！";
      this.broadcastMessage(Component.literal(message).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16733525))));
   }

   private void checkPlayerStatus() {
      Iterator<UUID> iterator = this.players.iterator();

      while (iterator.hasNext()) {
         UUID playerId = iterator.next();
         ServerPlayer player = this.getPlayer(playerId);
         if (player == null) {
            iterator.remove();
            this.offlinePlayers.add(playerId);
            this.broadcastMessage(Component.literal("玩家已离线: " + this.getPlayerName(playerId)));
         } else if (this.area != null && !this.area.contains(player.getX(), player.getY(), player.getZ())) {
            iterator.remove();
            this.handlePlayerRespawn(player);
            this.removePlayer(playerId);
            player.sendSystemMessage(Component.literal("你已离开游戏区域，被移出房间"));
            this.broadcastMessage(Component.literal("玩家离开区域: " + player.getName().getString()));
         }
      }
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   private void endGame() {
      for (UUID playerId : this.players) {
         ServerPlayer player = this.getPlayer(playerId);
         if (player != null) {
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            this.handlePlayerRespawn(player);
         }
      }

      String winnerMessage = this.calculateWinnerMessage();
      this.broadcastMessage(Component.literal(winnerMessage).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777045))));
   }

   public void handlePlayerRespawn(ServerPlayer player) {
      if (player != null) {
         Level world = player.serverLevel().getLevel();
         BlockPos worldSpawnPos = world.getSharedSpawnPos();
         float worldSpawnAngle = world.getSharedSpawnAngle();
         this.teleportToWorldSpawn(player, worldSpawnPos, worldSpawnAngle);
         this.setPlayerRespawnToWorldSpawn(player, world, worldSpawnPos, worldSpawnAngle);
      }
   }

   private void teleportToWorldSpawn(ServerPlayer player, BlockPos spawnPos, float spawnAngle) {
      double x = spawnPos.getX() + 0.5;
      double y = spawnPos.getY() + 1;
      double z = spawnPos.getZ() + 0.5;
      player.teleportTo(this.level, x, y, z, 0.0F, 0.0F);
   }

   private void setPlayerRespawnToWorldSpawn(ServerPlayer player, Level world, BlockPos spawnPos, float spawnAngle) {
      player.setRespawnPosition(world.dimension(), spawnPos, spawnAngle, true, false);
   }

   private String calculateWinnerMessage() {
      if (this.mode == GameModeType.TEAM) {
         String winningTeam = this.goal.getWinningTeam(this);
         return winningTeam != null ? "团队 " + winningTeam + " 获胜！" : "游戏结束，无胜利者";
      } else {
         UUID winnerId = this.goal.getWinningPlayer(this);
         return winnerId != null ? this.getPlayerName(winnerId) + " 获胜！" : "游戏结束，无胜利者";
      }
   }

   private String getPlayerName(UUID playerId) {
      ServerPlayer player = this.getPlayer(playerId);
      return player != null ? player.getName().getString() : "离线玩家";
   }

   private ServerPlayer getPlayer(UUID playerId) {
      return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId);
   }

   public boolean canJoinByCommand() {
      return !this.isRunning && this.players.size() < 16;
   }

   public void addPlayer(ServerPlayer player) {
      if (!this.hasPlayer(player.getUUID())) {
         if (this.players.size() >= 16) {
            player.sendSystemMessage(Component.literal("房间已满（16人上限）"));
         } else {
            this.players.add(player.getUUID());
            if (this.mode == GameModeType.TEAM) {
               String targetTeam = this.teamManager.findTeamWithFewestPlayers();
               if (targetTeam != null) {
                  this.teamManager.addPlayerToTeam(targetTeam, player.getUUID());
                  player.sendSystemMessage(Component.literal("你已被分配到队伍 " + targetTeam));
                  if (this.isRunning) {
                     this.broadcastTeamAssignment(player, targetTeam);
                  }
               }

               this.spawnPlayersToSpawnPoints();
               this.tryStartTeamRoomWhenReady();
            }
         }
      }
   }

   private void broadcastTeamAssignment(ServerPlayer player, String teamName) {
      Component message = Component.literal(player.getName().getString() + " 已加入队伍 " + teamName)
         .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777045)));
      this.broadcastMessage(message);
   }

   public void removePlayer(UUID playerId) {
      this.players.remove(playerId);
      this.offlinePlayers.remove(playerId);
      this.teamManager.removePlayerFromAllTeams(playerId);
      this.sendPlayerLeavePacket(playerId);
   }

   private void sendPlayerLeavePacket(UUID playerId) {
      if (this.id != null && !this.id.isEmpty()) {
         S2CRoomDataPacket.Builder builder = new S2CRoomDataPacket.Builder(this.id, S2CRoomDataPacket.DataType.PLAYER_LEAVE_ROOM);
         builder.addString("roomId", this.id);
         builder.addString("playerUUID", playerId.toString());
         S2CRoomDataPacket packet = builder.build();
         ServerPlayer player = this.getPlayer(playerId);
         if (player != null && player.connection != null) {
            ModMessages.sendToPlayer(packet, player);
         }      }
   }

   public void tryStartTeamRoomWhenReady() {
      if (this.mode == GameModeType.TEAM && !this.isRunning) {
         if (this.players.size() >= 2) {
            try {
               this.startGame();
               this.broadcastMessage(Component.literal("玩家数量满足条件，游戏已自动启动！").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(5635925))));
            } catch (GameConfigurationException e) {
               this.broadcastMessage(Component.literal("启动失败: " + e.getMessage()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16733525))));
            }
         }
      }
   }

   public void closeRoomIfEmpty() {
      if (this.players.isEmpty() && this.offlinePlayers.isEmpty() && this.isRunning) {
         this.stopGame();
      }
   }

   public boolean hasAllSpawnPoints() {
      if (this.mode != GameModeType.TEAM) {
         return true;
      }

      for (String teamName : this.teamManager.getTeamNames()) {
         if (!this.teamSpawnPoints.containsKey(teamName)) {
            return false;
         }
      }

      return true;
   }

   public boolean isSpawnPointInArea(String teamName) {
      BlockPos spawnPoint = this.getTeamSpawnPoint(teamName);
      return spawnPoint != null && this.area != null && this.area.contains(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
   }

   public void removeTeam(String teamName) {
      this.teamManager.removeTeam(teamName);
      this.teamSpawnPoints.remove(teamName);
      this.broadcastMessage(Component.literal("队伍 " + teamName + " 已解散"));
   }

   public String getPlayerTeam(UUID playerId) {
      return this.teamManager.getPlayerTeam(playerId);
   }

   public JoinMethod getJoinMethod() {
      return this.joinMethod;
   }

   public int getTickCount() {
      return this.tickCount;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public void setRunning(boolean running) {
      this.isRunning = running;
   }

   public void setTickCount(int tickCount) {
      this.tickCount = tickCount;
   }

   public void setStartTime(long startTime) {
      this.startTime = startTime;
   }

   public Map<String, BlockPos> getTeamSpawnPoints() {
      return Collections.unmodifiableMap(this.teamSpawnPoints);
   }

   public TeamManager getTeamManager() {
      return this.teamManager;
   }

   public void setJoinMethod(JoinMethod joinMethod) {
      this.joinMethod = joinMethod;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public void setLevel(ServerLevel level) {
      this.level = level;
   }

   public boolean isTimeExpired() {
      return this.isRunning && this.getRemainingTime() <= 0L;
   }

   public void sendRemainingTimeToClients() {
      if (this.isRunning) {
         S2CRoomDataPacket.Builder builder = new S2CRoomDataPacket.Builder(this.id, S2CRoomDataPacket.DataType.TIME_DATA);
         builder.addString("roomId", this.id);
         builder.addNumber("remainingTimeMillis", this.getRemainingTime());
         S2CRoomDataPacket packet = builder.build();

         for (UUID playerId : this.players) {
            ServerPlayer player = this.getPlayer(playerId);
            if (player != null && player.connection != null) {
               ModMessages.sendToPlayer(packet, player);
            }
         }
      }
   }

   public void sendPlayerScoreDataToClients() {
      if (this.isRunning) {
         if (this.goal instanceof TeamKillGoal tkGoal) {
            TeamKillGoal goalImpl = tkGoal;
            Map<String, Integer> var15 = new LinkedHashMap<>();

            for (String teamName : this.teamManager.getTeamNames()) {
               int kills = goalImpl.getTeamKills(teamName);
               var15.put(teamName, kills);
            }

            int requiredKills = goalImpl.getRequiredKills();

            for (UUID playerId : this.players) {
               ServerPlayer player = this.getPlayer(playerId);
               if (player != null && player.connection != null) {
                  String playerTeam = this.getPlayerTeam(playerId);
                  if (playerTeam != null && !playerTeam.trim().isEmpty()) {
                     S2CRoomDataPacket.Builder builder = new S2CRoomDataPacket.Builder(this.id, S2CRoomDataPacket.DataType.PLAYER_STATUS);
                     builder.addString("roomId", this.id);
                     builder.addString("playerTeam", playerTeam);
                     int idx = 0;

                     for (Entry<String, Integer> entry : var15.entrySet()) {
                        builder.addString("team" + idx, entry.getKey());
                        builder.addNumber("score" + idx, entry.getValue());
                        idx++;
                     }

                     builder.addNumber("teamCount", idx);
                     builder.addNumber("requiredKills", requiredKills);

                     for (UUID uuid : this.players) {
                        int kills = goalImpl.getPlayerKills(uuid);
                        int deaths = goalImpl.getPlayerDeaths(uuid);
                        builder.addNumber("playerKills_" + uuid, kills);
                        builder.addNumber("playerDeaths_" + uuid, deaths);
                     }

                     ModMessages.sendToPlayer(builder.build(), player);
                  }
               }
            }
         }
      }
   }

   public void sendScoreDataToClients() {
      if (this.isRunning) {
         TeamKillGoal goalImpl = null;
         if (this.goal instanceof TeamKillGoal var15) {
            S2CRoomDataPacket.Builder baseBuilder = new S2CRoomDataPacket.Builder(this.id, S2CRoomDataPacket.DataType.SCORE_DATA);
            baseBuilder.addString("roomId", this.id);
            int idx = 0;

            for (String teamName : this.teamManager.getTeamNames()) {
               int kills = var15.getTeamKills(teamName);
               baseBuilder.addString("team" + idx, teamName);
               baseBuilder.addNumber("score" + idx, kills);
               idx++;
            }

            baseBuilder.addNumber("teamCount", idx);
            baseBuilder.addNumber("requiredKills", var15.getRequiredKills());
            S2CRoomDataPacket basePacket = baseBuilder.build();

            for (UUID playerId : this.players) {
               ServerPlayer player = this.getPlayer(playerId);
               if (player != null && player.connection != null) {
                  String currentPlayerTeam = this.getPlayerTeam(playerId);
                  if (currentPlayerTeam != null && !currentPlayerTeam.isEmpty()) {
                     S2CRoomDataPacket.Builder perPlayerBuilder = new S2CRoomDataPacket.Builder(this.id, S2CRoomDataPacket.DataType.SCORE_DATA);
                     perPlayerBuilder.addString("roomId", this.id);
                     perPlayerBuilder.addString("playerTeam", currentPlayerTeam);

                     for (int i = 0; i < idx; i++) {
                        String teamKey = "team" + i;
                        String scoreKey = "score" + i;
                        String tName = basePacket.getString(teamKey).orElse("");
                        Number tScore = basePacket.getNumber(scoreKey).orElse(0);
                        perPlayerBuilder.addString(teamKey, tName);
                        perPlayerBuilder.addNumber(scoreKey, tScore);
                     }

                     perPlayerBuilder.addNumber("teamCount", idx);
                     perPlayerBuilder.addNumber("requiredKills", var15.getRequiredKills());
                     ModMessages.sendToPlayer(perPlayerBuilder.build(), player);
                  }
               }
            }
         }
      }
   }

   public long getRemainingTime() {
      if (!this.isRunning) {
         return 0L;
      }

      long remainingTicks = Math.max(0, 12000 - this.tickCount);
      return remainingTicks * 50L;
   }

   public long getTotalGameDuration() {
      return 600000L;
   }

   public static void register() {
      NeoForge.EVENT_BUS.addListener(GameRoom::onServerTick);
   }

   public static void onServerTick(ServerTickEvent.Post event) {
      if (event instanceof ServerTickEvent.Post) {
         MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
         if (server != null) {
            for (ServerLevel level : server.getAllLevels()) {
               GameRoomManager manager = GameRoomManager.getForLevel(level);

               for (GameRoom room : manager.getAllRooms()) {
                  if (room.isRunning) {
                     room.tick();
                  }
               }
            }
         }
      }
   }
}
