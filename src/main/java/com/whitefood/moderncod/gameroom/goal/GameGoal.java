package com.whitefood.moderncod.gameroom.goal;

import com.whitefood.moderncod.gameroom.base.GameModeType;
import com.whitefood.moderncod.gameroom.base.GameRoom;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface GameGoal {
   Component getDescription();

   boolean isCompleted(GameRoom var1);

   boolean isCompatibleWith(GameModeType var1);

   void onPlayerKill(ServerPlayer var1, ServerPlayer var2, GameRoom var3);

   void onTick(GameRoom var1);

   default void onGameStart(GameRoom room) {
   }

   default void onGameEnd(GameRoom room) {
   }

   default void onPlayerJoin(ServerPlayer player, GameRoom room) {
   }

   default void onPlayerLeave(ServerPlayer player, GameRoom room) {
   }

   default String getWinningTeam(GameRoom room) {
      return null;
   }

   default UUID getWinningPlayer(GameRoom room) {
      return null;
   }
}
