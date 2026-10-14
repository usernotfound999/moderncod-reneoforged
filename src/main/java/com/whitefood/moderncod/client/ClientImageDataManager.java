package com.whitefood.moderncod.client;

import com.whitefood.moderncod.event.custom.PlayerImageManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientImageDataManager {
   private static final Map<UUID, String> playerImageMap = new HashMap<>();

   public ClientImageDataManager() {
   }

   public static void setPlayerImage(UUID uuid, String path) {
      playerImageMap.put(uuid, path);
   }

   public static String getPlayerImage(UUID uuid) {
      return playerImageMap.getOrDefault(uuid, PlayerImageManager.DEFAULT_IMAGE.getPath());
   }
}
