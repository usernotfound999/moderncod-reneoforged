package com.whitefood.moderncod.gameroom.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.whitefood.moderncod.gameroom.base.GameConfigurationException;
import com.whitefood.moderncod.gameroom.base.GameModeType;
import com.whitefood.moderncod.gameroom.base.GameRoom;
import com.whitefood.moderncod.gameroom.goal.SoloKillGoal;
import com.whitefood.moderncod.gameroom.goal.TeamKillGoal;
import com.whitefood.moderncod.gameroom.manager.GameRoomManager;
import com.whitefood.moderncod.gameroom.manager.SelectionManager;
import com.whitefood.moderncod.gameroom.manager.TeamManager;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class FpsCommand {
   public FpsCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                          "fps"
                                       )
                                       .requires(src -> src.hasPermission(2)))
                                    .then(
                                       Commands.literal("create")
                                          .then(
                                             Commands.argument("roomId", StringArgumentType.word())
                                                .then(
                                                   Commands.argument("mode", StringArgumentType.word())
                                                      .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"TEAM", "SOLO"}, builder))
                                                      .executes(
                                                         ctx -> {
                                                            String roomId = StringArgumentType.getString(ctx, "roomId");
                                                            String modeStr = StringArgumentType.getString(ctx, "mode");
                                                            ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();

                                                            GameModeType gameMode;
                                                            try {
                                                               gameMode = GameModeType.valueOf(modeStr.toUpperCase());
                                                            } catch (IllegalArgumentException e) {
                                                               ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("无效的游戏模式: " + modeStr));
                                                               return 0;
                                                            }

                                                            GameRoomManager manager = GameRoomManager.getForLevel(level);
                                                            GameRoom room = new GameRoom(roomId, level);
                                                            room.setMode(gameMode);
                                                            manager.addRoom(room);
                                                            ((CommandSourceStack)ctx.getSource())
                                                               .sendSuccess(
                                                                  () -> Component.literal(
                                                                     "在维度 " + level.dimension().location() + " 创建房间 " + roomId + "，模式：" + gameMode
                                                                  ),
                                                                  false
                                                               );
                                                            return 1;
                                                         }
                                                      )
                                                )
                                          )
                                    ))
                                 .then(Commands.literal("join").then(Commands.argument("roomId", StringArgumentType.word()).suggests((context, builder) -> {
                                    ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
                                    return SharedSuggestionProvider.suggest(
                                       GameRoomManager.getForLevel(level).getAllRooms().stream().map(GameRoom::getId), builder
                                    );
                                 }).executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "roomId");
                                    ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                                    ServerLevel level = player.serverLevel();
                                    GameRoom room = GameRoomManager.getForLevel(level).getRoom(id);
                                    if (room == null || !room.canJoinByCommand() && !((CommandSourceStack)ctx.getSource()).hasPermission(2)) {
                                       ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("无法加入该房间"));
                                       return 0;
                                    } else {
                                       room.addPlayer(player);
                                       return 1;
                                    }
                                 }))))
                              .then(
                                 ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("select").then(Commands.literal("pos1").executes(ctx -> {
                                    ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                                    BlockPos pos = BlockPos.containing(player.position());
                                    SelectionManager.setPos1(player, pos);
                                    ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal("已设置 Pos1: " + pos), false);
                                    ServerLevel level = player.serverLevel();
                                    level.sendParticles(
                                       ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1, 1.0, 0.0, 0.0, 1.0
                                    );
                                    return 1;
                                 }))).then(Commands.literal("pos2").executes(ctx -> {
                                    ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                                    BlockPos pos = BlockPos.containing(player.position());
                                    SelectionManager.setPos2(player, pos);
                                    ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal("已设置 Pos2: " + pos), false);
                                    ServerLevel level = player.serverLevel();
                                    level.sendParticles(
                                       ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1, 1.0, 0.0, 0.0, 1.0
                                    );
                                    return 1;
                                 }))).then(
                                    Commands.literal("setArea").then(Commands.argument("roomId", StringArgumentType.word()).suggests((context, builder) -> {
                                       ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
                                       return SharedSuggestionProvider.suggest(
                                          GameRoomManager.getForLevel(level).getAllRooms().stream().map(GameRoom::getId), builder
                                       );
                                    }).executes(ctx -> {
                                       String id = StringArgumentType.getString(ctx, "roomId");
                                       ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                                       ServerLevel level = player.serverLevel();
                                       AABB area = SelectionManager.getSelectedArea(player);
                                       if (area == null) {
                                          ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("请先设置 pos1 和 pos2"));
                                          return 0;
                                       } else {
                                          GameRoom room = GameRoomManager.getForLevel(level).getRoom(id);
                                          if (room == null) {
                                             ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                                             return 0;
                                          } else {
                                             room.setArea(area);
                                             SelectionManager.clear(player);
                                             ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal("已为房间 " + id + " 设置区域: " + area), false);
                                             displayAreaParticles(room, player);
                                             return 1;
                                          }
                                       }
                                    }))
                                 )
                              ))
                           .then(Commands.literal("start").then(Commands.argument("roomId", StringArgumentType.word()).executes(ctx -> {
                              String roomId = StringArgumentType.getString(ctx, "roomId");
                              ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                              GameRoom room = GameRoomManager.getForLevel(level).getRoom(roomId);
                              if (room == null) {
                                 ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                                 return 0;
                              }

                              try {
                                 room.startGame();
                                 ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal("游戏已启动！"), false);
                                 return 1;
                              } catch (GameConfigurationException e) {
                                 ((CommandSourceStack)ctx.getSource()).sendFailure(e.getErrorMessage());
                                 return 0;
                              } catch (Exception e) {
                                 ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("启动游戏时发生未知错误: " + e.getMessage()));
                                 e.printStackTrace();
                                 return 0;
                              }
                           }))))
                        .then(Commands.literal("stop").then(Commands.argument("roomId", StringArgumentType.word()).executes(ctx -> {
                           String roomId = StringArgumentType.getString(ctx, "roomId");
                           ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                           GameRoom room = GameRoomManager.getForLevel(level).getRoom(roomId);
                           if (room == null) {
                              ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                              return 0;
                           }

                           room.stopGame();
                           ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal("游戏已结束！"), false);
                           if (room.getArea() != null) {
                              AABB area = room.getArea();
                              ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                              ServerLevel playerLevel = player.serverLevel();

                              for (int i = 0; i < 50; i++) {
                                 double x = area.minX + Math.random() * (area.maxX - area.minX);
                                 double y = area.minY + Math.random() * (area.maxY - area.minY);
                                 double z = area.minZ + Math.random() * (area.maxZ - area.minZ);
                                 playerLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 1, 0.0, 0.1, 0.0, 0.1);
                              }
                           }

                           return 1;
                        }))))
                     .then(
                        Commands.literal("setSpawn")
                           .then(
                              Commands.argument("roomId", StringArgumentType.word())
                                 .suggests(
                                    (ctx, builder) -> {
                                       ServerLevel playerLevel = ((CommandSourceStack)ctx.getSource()).getLevel();
                                       return SharedSuggestionProvider.suggest(
                                          GameRoomManager.getForLevel(playerLevel)
                                             .getAllRooms()
                                             .stream()
                                             .filter(room -> room.getArea() != null)
                                             .map(GameRoom::getId),
                                          builder
                                       );
                                    }
                                 )
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("teamName", StringArgumentType.word())
                                          .suggests((ctx, builder) -> {
                                             String roomId = (String)ctx.getArgument("roomId", String.class);
                                             ServerLevel playerLevel = ((CommandSourceStack)ctx.getSource()).getLevel();
                                             GameRoom room = GameRoomManager.getForLevel(playerLevel).getRoom(roomId);
                                             return room != null && room.getArea() != null
                                                ? SharedSuggestionProvider.suggest(room.getTeamNames(), builder)
                                                : builder.buildFuture();
                                          })
                                          .then(
                                             Commands.literal("here")
                                                .executes(
                                                   ctx -> {
                                                      String roomId = StringArgumentType.getString(ctx, "roomId");
                                                      String teamName = StringArgumentType.getString(ctx, "teamName");
                                                      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                                                      BlockPos spawnPoint = player.blockPosition();
                                                      ServerLevel playerLevel = player.serverLevel();
                                                      GameRoom room = GameRoomManager.getForLevel(playerLevel).getRoom(roomId);
                                                      if (room != null) {
                                                         AABB area = room.getArea();
                                                         if (area == null) {
                                                            ((CommandSourceStack)ctx.getSource())
                                                               .sendFailure(Component.literal("请先设置房间区域 (/fps select setArea " + roomId + ")"));
                                                            return 0;
                                                         } else if (!area.contains(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ())) {
                                                            ((CommandSourceStack)ctx.getSource())
                                                               .sendFailure(
                                                                  Component.literal(
                                                                     "你的当前位置 ("
                                                                        + spawnPoint.getX()
                                                                        + ","
                                                                        + spawnPoint.getY()
                                                                        + ","
                                                                        + spawnPoint.getZ()
                                                                        + ") 不在房间区域内"
                                                                  )
                                                               );
                                                            return 0;
                                                         } else if (!room.getTeamNames().contains(teamName)) {
                                                            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("队伍 " + teamName + " 不存在"));
                                                            return 0;
                                                         } else {
                                                            room.setTeamSpawnPoint(teamName, spawnPoint);
                                                            ((CommandSourceStack)ctx.getSource())
                                                               .sendSuccess(() -> Component.literal("为 " + teamName + " 队设置重生点: " + spawnPoint), false);
                                                            displaySpawnPointParticles(spawnPoint, player);
                                                            return 1;
                                                         }
                                                      } else {
                                                         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                                                         return 0;
                                                      }
                                                   }
                                                )
                                          ))
                                       .then(
                                          Commands.argument("x", IntegerArgumentType.integer())
                                             .then(
                                                Commands.argument("y", IntegerArgumentType.integer())
                                                   .then(Commands.argument("z", IntegerArgumentType.integer()).executes(ctx -> {
                                                      String roomId = StringArgumentType.getString(ctx, "roomId");
                                                      String teamName = StringArgumentType.getString(ctx, "teamName");
                                                      int x = IntegerArgumentType.getInteger(ctx, "x");
                                                      int y = IntegerArgumentType.getInteger(ctx, "y");
                                                      int z = IntegerArgumentType.getInteger(ctx, "z");
                                                      BlockPos spawnPoint = new BlockPos(x, y, z);
                                                      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                                                      ServerLevel playerLevel = player.serverLevel();
                                                      GameRoom room = GameRoomManager.getForLevel(playerLevel).getRoom(roomId);
                                                      if (room != null) {
                                                         AABB area = room.getArea();
                                                         if (area == null) {
                                                            ((CommandSourceStack)ctx.getSource())
                                                               .sendFailure(Component.literal("请先设置房间区域 (/fps select setArea " + roomId + ")"));
                                                            return 0;
                                                         } else if (!area.contains(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ())) {
                                                            ((CommandSourceStack)ctx.getSource())
                                                               .sendFailure(Component.literal("重生点坐标 (" + x + "," + y + "," + z + ") 不在房间区域内"));
                                                            return 0;
                                                         } else if (!room.getTeamNames().contains(teamName)) {
                                                            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("队伍 " + teamName + " 不存在"));
                                                            return 0;
                                                         } else {
                                                            room.setTeamSpawnPoint(teamName, spawnPoint);
                                                            ((CommandSourceStack)ctx.getSource())
                                                               .sendSuccess(() -> Component.literal("为 " + teamName + " 队设置重生点: " + spawnPoint), false);
                                                            displaySpawnPointParticles(spawnPoint, player);
                                                            return 1;
                                                         }
                                                      } else {
                                                         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                                                         return 0;
                                                      }
                                                   }))
                                             )
                                       )
                                 )
                           )
                     ))
                  .then(
                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("team")
                              .then(
                                 Commands.literal("create")
                                    .then(
                                       Commands.argument("roomId", StringArgumentType.word())
                                          .suggests((context, builder) -> {
                                             ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
                                             return SharedSuggestionProvider.suggest(
                                                GameRoomManager.getForLevel(level).getAllRooms().stream().map(GameRoom::getId), builder
                                             );
                                          })
                                          .executes(
                                             ctx -> {
                                                String roomId = StringArgumentType.getString(ctx, "roomId");
                                                ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                                                GameRoom room = GameRoomManager.getForLevel(level).getRoom(roomId);
                                                if (room == null) {
                                                   ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                                                   return 0;
                                                }

                                                TeamManager teamManager = room.teamManager;
                                                teamManager.createTeamsByRoomType(roomId, room.getMode());
                                                ((CommandSourceStack)ctx.getSource())
                                                   .sendSuccess(
                                                      () -> Component.literal(
                                                         "已为房间 " + roomId + " 创建 " + (room.getMode() == GameModeType.TEAM ? "2个" : "1个") + " 队伍"
                                                      ),
                                                      false
                                                   );
                                                if (room.getArea() != null) {
                                                   ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                                                   ServerLevel playerLevel = player.serverLevel();
                                                   AABB area = room.getArea();
                                                   playerLevel.sendParticles(
                                                      ParticleTypes.HAPPY_VILLAGER,
                                                      area.getCenter().x,
                                                      area.maxY + 1.0,
                                                      area.getCenter().z,
                                                      1,
                                                      0.5,
                                                      0.5,
                                                      0.5,
                                                      1.0
                                                   );
                                                }

                                                return 1;
                                             }
                                          )
                                    )
                              ))
                           .then(Commands.literal("playerlist").then(Commands.argument("roomId", StringArgumentType.word()).suggests((context, builder) -> {
                              ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
                              return SharedSuggestionProvider.suggest(GameRoomManager.getForLevel(level).getAllRooms().stream().map(GameRoom::getId), builder);
                           }).executes(ctx -> {
                              String roomId = StringArgumentType.getString(ctx, "roomId");
                              ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                              GameRoom room = GameRoomManager.getForLevel(level).getRoom(roomId);
                              if (room == null) {
                                 ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                                 return 0;
                              }

                              TeamManager teamManager = room.teamManager;
                              MutableComponent message = Component.literal("房间 " + roomId + " 的队伍玩家列表:\n");

                              for (String teamName : teamManager.getTeamNames()) {
                                 Set<UUID> members = teamManager.getTeamMembers(teamName);
                                 MutableComponent teamInfo = Component.literal("  - " + teamName + ": ");
                                 if (members.isEmpty()) {
                                    teamInfo.append(Component.literal("无玩家").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16733525))));
                                 } else {
                                    StringBuilder playerNames = new StringBuilder();

                                    for (UUID uuid : members) {
                                       ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
                                       if (p != null) {
                                          playerNames.append(p.getName().getString()).append(", ");
                                       }
                                    }

                                    if (playerNames.length() > 0) {
                                       playerNames.setLength(playerNames.length() - 2);
                                    }

                                    teamInfo.append(
                                       Component.literal(playerNames.toString()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(5635925)))
                                    );
                                 }

                                 message.append(teamInfo).append("\n");
                              }

                              ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> message, false);
                              return 1;
                           }))))
                        .then(Commands.literal("add").then(Commands.argument("roomId", StringArgumentType.word()).suggests((context, builder) -> {
                           ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
                           return SharedSuggestionProvider.suggest(GameRoomManager.getForLevel(level).getAllRooms().stream().map(GameRoom::getId), builder);
                        }).then(Commands.argument("teamName", StringArgumentType.word()).suggests((ctx, builder) -> {
                           String roomId = (String)ctx.getArgument("roomId", String.class);
                           ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                           GameRoom room = GameRoomManager.getForLevel(level).getRoom(roomId);
                           return room != null ? SharedSuggestionProvider.suggest(room.getTeamNames(), builder) : builder.buildFuture();
                        }).then(Commands.argument("playerName", StringArgumentType.word()).suggests((ctx, builder) -> {
                           Collection<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
                           return SharedSuggestionProvider.suggest(players.stream().map(p -> p.getName().getString()), builder);
                        }).executes(ctx -> {
                           String roomId = StringArgumentType.getString(ctx, "roomId");
                           String teamName = StringArgumentType.getString(ctx, "teamName");
                           String playerName = StringArgumentType.getString(ctx, "playerName");
                           ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                           GameRoom room = GameRoomManager.getForLevel(level).getRoom(roomId);
                           if (room == null) {
                              ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                              return 0;
                           }

                           if (!room.getTeamNames().contains(teamName)) {
                              ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("队伍不存在"));
                              return 0;
                           }

                           ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(playerName);
                           if (player == null) {
                              ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("玩家不在线"));
                              return 0;
                           }

                           TeamManager teamManager = room.teamManager;
                           teamManager.addPlayerToTeam(teamName, player.getUUID());
                           if (!room.hasPlayer(player.getUUID())) {
                              room.addPlayer(player);
                           }

                           ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal("已将玩家 " + playerName + " 添加到队伍 " + teamName), false);
                           level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 1, 0.2, 0.2, 0.2, 1.0);
                           return 1;
                        })))))
                  ))
               .then(
                  Commands.literal("setGoal")
                     .then(
                        Commands.argument("roomId", StringArgumentType.word())
                           .suggests((context, builder) -> {
                              ServerLevel level = ((CommandSourceStack)context.getSource()).getLevel();
                              return SharedSuggestionProvider.suggest(GameRoomManager.getForLevel(level).getAllRooms().stream().map(GameRoom::getId), builder);
                           })
                           .then(
                              Commands.argument("goalType", StringArgumentType.word())
                                 .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"SOLO_KILL", "TEAM_KILL"}, builder))
                                 .executes(ctx -> {
                                    String roomId = StringArgumentType.getString(ctx, "roomId");
                                    String goalType = StringArgumentType.getString(ctx, "goalType");
                                    ServerLevel level = ((CommandSourceStack)ctx.getSource()).getLevel();
                                    GameRoom room = GameRoomManager.getForLevel(level).getRoom(roomId);
                                    if (room == null) {
                                       ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("房间不存在"));
                                       return 0;
                                    }

                                    try {
                                       switch (goalType.toUpperCase()) {
                                          case "SOLO_KILL":
                                             room.setGoal(new SoloKillGoal());
                                             ((CommandSourceStack)ctx.getSource())
                                                .sendSuccess(() -> Component.literal("已为房间 " + roomId + " 设置目标: 个人竞赛(10杀胜利)"), false);
                                             break;
                                          case "TEAM_KILL":
                                             room.setGoal(new TeamKillGoal());
                                             ((CommandSourceStack)ctx.getSource())
                                                .sendSuccess(() -> Component.literal("已为房间 " + roomId + " 设置目标: 团队竞赛(100杀胜利)"), false);
                                             break;
                                          default:
                                             ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("未知的目标类型: " + goalType));
                                             return 0;
                                       }

                                       return 1;
                                    } catch (Exception e) {
                                       ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("设置目标时出错: " + e.getMessage()));
                                       e.printStackTrace();
                                       return 0;
                                    }
                                 })
                           )
                     )
               ))
            .then(
               Commands.literal("list")
                  .executes(
                     ctx -> {
                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                        MutableComponent message = Component.literal("当前房间列表:\n");

                        for (ServerLevel level : server.getAllLevels()) {
                           for (GameRoom room : GameRoomManager.getForLevel(level).getAllRooms()) {
                              AABB area = room.getArea();
                              String goalDesc = room.getGoal() != null ? room.getGoal().getDescription().getString() : "未设置";
                              if (goalDesc.length() > 30) {
                                 goalDesc = goalDesc.substring(0, 30) + "...";
                              }

                              String status = room.isRunning() ? "运行中" : "未开始";
                              message.append(
                                 Component.literal(
                                       "房间: "
                                          + room.getId()
                                          + "，世界: "
                                          + level.dimension().location()
                                          + "，模式: "
                                          + room.getMode()
                                          + "，状态: "
                                          + status
                                          + "，区域: "
                                          + (area != null ? "已设置" : "未设置")
                                          + "，目标: "
                                          + goalDesc
                                          + "\n"
                                    )
                                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16776960)))
                              );
                           }
                        }

                        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> message, false);
                        return 1;
                     }
                  )
            )
      );
   }

   private static void displayAreaParticles(GameRoom room, ServerPlayer player) {
      AABB area = room.getArea();
      if (area != null && player != null) {
         ServerLevel level = player.serverLevel();
         double step = 1.0;

         for (double x = area.minX; x <= area.maxX; x += step) {
            for (double z = area.minZ; z <= area.maxZ; z += step) {
               spawnParticle(level, ParticleTypes.END_ROD, x, area.minY, z, 0.0, 0.0, 0.0);
               spawnParticle(level, ParticleTypes.END_ROD, x, area.maxY, z, 0.0, 0.0, 0.0);
            }
         }

         for (double y = area.minY; y <= area.maxY; y += step) {
            for (double z = area.minZ; z <= area.maxZ; z += step) {
               spawnParticle(level, ParticleTypes.END_ROD, area.minX, y, z, 0.0, 0.0, 0.0);
               spawnParticle(level, ParticleTypes.END_ROD, area.maxX, y, z, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   private static void displaySpawnPointParticles(BlockPos pos, ServerPlayer player) {
      if (pos != null && player != null) {
         ServerLevel level = player.serverLevel();

         for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 2.0;
            double offsetZ = (Math.random() - 0.5) * 2.0;
            spawnParticle(
               level,
               ParticleTypes.HAPPY_VILLAGER,
               pos.getX() + 0.5 + offsetX,
               pos.getY() + 1.0 + Math.random(),
               pos.getZ() + 0.5 + offsetZ,
               0.0,
               0.0,
               0.0
            );
         }
      }
   }

   private static void spawnParticle(ServerLevel level, ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      level.sendParticles(particle, x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.0);
   }
}
