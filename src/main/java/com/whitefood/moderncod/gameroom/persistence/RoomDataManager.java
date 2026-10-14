package com.whitefood.moderncod.gameroom.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class RoomDataManager extends SavedData {
   public static final SavedData.Factory<RoomDataManager> FACTORY = new SavedData.Factory<>(RoomDataManager::new, RoomDataManager::load, null);

   private int totalRooms = 0;

   public RoomDataManager() {
   }

   public static RoomDataManager load(CompoundTag nbt, HolderLookup.Provider provider) {
      RoomDataManager manager = new RoomDataManager();
      manager.totalRooms = nbt.getInt("TotalRooms");
      return manager;
   }

   @Override
   public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
      tag.putInt("TotalRooms", this.totalRooms);
      return tag;
   }

   public void setTotalRooms(int total) {
      this.totalRooms = total;
      this.setDirty();
   }

   public int getTotalRooms() {
      return this.totalRooms;
   }
}
