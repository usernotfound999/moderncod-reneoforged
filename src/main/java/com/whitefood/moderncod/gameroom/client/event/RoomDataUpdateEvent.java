package com.whitefood.moderncod.gameroom.client.event;

import java.util.Map;
import net.neoforged.bus.api.Event;

public class RoomDataUpdateEvent extends Event {
   private final String roomId;
   private final Map<String, Object> data;

   public RoomDataUpdateEvent(String roomId, Map<String, Object> data) {
      this.roomId = roomId;
      this.data = data;
   }

   public String getRoomId() {
      return this.roomId;
   }

   public Map<String, Object> getData() {
      return this.data;
   }
}
