package com.whitefood.moderncod.gameroom.manager;

import com.whitefood.moderncod.gameroom.base.GameModeType;
import com.whitefood.moderncod.gameroom.base.GameRoom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public class GameRoomManager {
   private static final Map<ServerLevel, GameRoomManager> INSTANCES = new ConcurrentHashMap<>();
   private final Map<String, GameRoom> rooms = new HashMap<>();
   private final ServerLevel level;

   private GameRoomManager(ServerLevel level) {
      this.level = level;
   }

   public static GameRoomManager getForLevel(ServerLevel level) {
      return INSTANCES.computeIfAbsent(level, GameRoomManager::new);
   }

   public void createRoom(String id, GameModeType mode) {
      if (this.rooms.containsKey(id)) {
         throw new IllegalArgumentException("房间已存在: " + id);
      }

      GameRoom room = new GameRoom(id, this.level);
      room.setMode(mode);
      this.rooms.put(id, room);
   }

   public void addRoom(GameRoom room) {
      this.rooms.put(room.getId(), room);
   }

   @Nullable
   public GameRoom getRoom(String id) {
      return this.rooms.get(id);
   }

   public Collection<GameRoom> getAllRooms() {
      return this.rooms.values();
   }

   public void removeRoom(String id) {
      GameRoom room = this.rooms.get(id);
      if (room != null && room.isRunning()) {
         room.stopGame();
      }

      this.rooms.remove(id);
   }

   public void removeRoom(GameRoom room) {
      if (room != null) {
         this.removeRoom(room.getId());
      }
   }

   public static void cleanupForLevel(ServerLevel level) {
      GameRoomManager manager = INSTANCES.get(level);
      if (manager != null) {
         for (GameRoom room : manager.rooms.values()) {
            if (room.isRunning()) {
               room.stopGame();
            }
         }
      }

      INSTANCES.remove(level);
   }

   public boolean hasRoom(String id) {
      return this.rooms.containsKey(id);
   }

   public int getRoomCount() {
      return this.rooms.size();
   }
}
