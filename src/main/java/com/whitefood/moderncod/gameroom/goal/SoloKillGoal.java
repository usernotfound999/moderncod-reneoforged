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
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class SoloKillGoal implements GameGoal {
   private static final int REQUIRED_KILLS = 10;
   private final Map<UUID, Integer> playerKills = new HashMap<>();

   public SoloKillGoal() {
   }

   @Override
   public boolean isCompatibleWith(GameModeType mode) {
      return mode == GameModeType.SOLO;
   }

   @Override
   public Component getDescription() {
      MutableComponent desc = Component.literal("个人竞赛：率先获得")
         .append(Component.literal(" 10 ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16733525))))
         .append("击杀的玩家获胜\n");
      if (!this.playerKills.isEmpty()) {
         desc.append(Component.literal("\n当前击杀排行榜：\n").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777045))));
         this.playerKills
            .entrySet()
            .stream()
            .sorted(Entry.<UUID, Integer>comparingByValue().reversed())
            .forEach(
               entry -> {
                  ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(entry.getKey());
                  if (player != null) {
                     desc.append(
                        Component.literal("  " + player.getName().getString() + ": " + entry.getValue() + " 杀\n")
                           .withStyle(Style.EMPTY.withColor(this.getColorByRank(entry.getValue())))
                     );
                  }
               }
            );
      }

      return desc;
   }

   private TextColor getColorByRank(int kills) {
      if (kills >= 10) {
         return TextColor.fromRgb(16733525);
      } else if (kills >= 8) {
         return TextColor.fromRgb(16755200);
      } else {
         return kills >= 5 ? TextColor.fromRgb(16777045) : TextColor.fromRgb(5635925);
      }
   }

   @Override
   public boolean isCompleted(GameRoom room) {
      return this.playerKills.values().stream().anyMatch(kills -> kills >= 10);
   }

   @Override
   public void onPlayerKill(ServerPlayer killer, ServerPlayer victim, GameRoom room) {
      UUID killerId = killer.getUUID();
      this.playerKills.put(killerId, this.playerKills.getOrDefault(killerId, 0) + 1);
      int currentKills = this.playerKills.get(killerId);
      MutableComponent message = Component.literal(killer.getName().getString())
         .append(" 击杀了 ")
         .append(victim.getName())
         .append(" (")
         .append(Component.literal(currentKills + "杀").withStyle(Style.EMPTY.withColor(this.getColorByRank(currentKills))))
         .append(")");
      if (currentKills >= 10) {
         message.append("\n")
            .append(
               Component.literal(killer.getName().getString() + " 获得了胜利！")
                  .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(16733525)).withBold(true))
            );
      }

      room.broadcastMessage(message);
   }

   @Override
   public void onTick(GameRoom room) {
      if (room.getTickCount() % 600 == 0) {
         room.broadcastMessage(this.getDescription());
      }
   }

   @Override
   public void onGameStart(GameRoom room) {
      this.playerKills.clear();
   }

   @Override
   public UUID getWinningPlayer(GameRoom room) {
      return this.playerKills.entrySet().stream().filter(entry -> entry.getValue() >= 10).map(Entry::getKey).findFirst().orElse(null);
   }
}
