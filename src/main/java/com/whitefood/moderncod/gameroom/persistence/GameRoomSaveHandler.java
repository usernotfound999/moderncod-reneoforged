package com.whitefood.moderncod.gameroom.persistence;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.whitefood.moderncod.gameroom.base.GameRoom;
import com.whitefood.moderncod.gameroom.manager.GameRoomManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.level.LevelEvent.Load;
import net.neoforged.neoforge.event.level.LevelEvent.Save;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;

@EventBusSubscriber(modid = "moderncod")
public class GameRoomSaveHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final LevelResource DATA_FOLDER = new LevelResource("gameroom");
   private static final Lock saveLock = new ReentrantLock();
   private static final Map<ServerLevel, RoomDataManager> dataManagers = Maps.newConcurrentMap();

   public GameRoomSaveHandler() {
   }

   private static File getLevelDataFolder(ServerLevel level) {
      Path path = level.getServer().getWorldPath(DATA_FOLDER).resolve(level.dimension().location().getNamespace()).resolve(level.dimension().location().getPath());
      return path.toFile();
   }

   private static File getRoomDataFile(ServerLevel level, String roomId) {
      return new File(getLevelDataFolder(level), roomId + ".dat");
   }

   @SubscribeEvent
   public static void onLevelLoad(Load event) {
      if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
         initDataManager(serverLevel);
         loadAllRooms(serverLevel);
      }
   }

   private static void initDataManager(ServerLevel level) {
      dataManagers.computeIfAbsent(level, l -> {
         String storageId = String.format("moderncod_rooms_%s_%s", level.dimension().location().getNamespace(), level.dimension().location().getPath());
         return level.getDataStorage().computeIfAbsent(RoomDataManager.FACTORY, storageId);
      });
   }

   private static void loadAllRooms(ServerLevel level) {
      File dataFolder = getLevelDataFolder(level);
      if (!dataFolder.exists() && !dataFolder.mkdirs()) {
         LOGGER.error("无法创建数据目录: {}", level.dimension().location());
      } else {
         File[] roomFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".dat"));
         if (roomFiles != null) {
            GameRoomManager manager = GameRoomManager.getForLevel(level);

            for (File file : roomFiles) {
               try {
                  String roomId = file.getName().replace(".dat", "");
                  CompoundTag nbt = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
                  GameRoom room = RoomNBTSerializer.deserialize(roomId, nbt, level.getServer());
                  if (room != null) {
                     manager.addRoom(room);
                     LOGGER.debug("已加载房间 {} 维度 {}", roomId, file.getAbsolutePath());
                  }
               } catch (IOException e) {
                  LOGGER.error("加载房间数据失败:{}", file, e);
               }
            }
         }
      }
   }

   public static void saveAllRooms(ServerLevel level) {
      saveLock.lock();

      try {
         File dataFolder = getLevelDataFolder(level);
         if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            LOGGER.error("Failed to create data folder for {}", level.dimension().location());
            return;
         }

         GameRoomManager manager = GameRoomManager.getForLevel(level);

         for (GameRoom room : manager.getAllRooms()) {
            try {
               File file = getRoomDataFile(level, room.getId());
               CompoundTag nbt = RoomNBTSerializer.serialize(room);
               NbtIo.writeCompressed(nbt, file.toPath());
               LOGGER.debug("Saved room {} to {}", room.getId(), file.getAbsolutePath());
            } catch (IOException e) {
               LOGGER.error("Failed to save room {}: {}", room.getId(), e.getMessage());
            }
         }
      } finally {
         saveLock.unlock();
      }
   }

   @SubscribeEvent
   public static void onLevelUnload(Unload event) {
      if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
         saveAllRooms(serverLevel);
         dataManagers.remove(serverLevel);
         GameRoomManager.cleanupForLevel(serverLevel);
         LOGGER.info("Cleaned up game room data for {}", serverLevel.dimension().location());
      }
   }

   @SubscribeEvent
   public static void onWorldSave(Save event) {
      if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
         saveAllRooms(serverLevel);
      }
   }

   public static RoomDataManager getDataManager(ServerLevel level) {
      return dataManagers.get(level);
   }
}
