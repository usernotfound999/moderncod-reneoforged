package com.whitefood.moderncod.gameroom.persistence;

import com.mojang.text2speech.Narrator;
import com.whitefood.moderncod.gameroom.base.GameModeType;
import com.whitefood.moderncod.gameroom.base.GameRoom;
import com.whitefood.moderncod.gameroom.base.JoinMethod;
import com.whitefood.moderncod.gameroom.goal.GameGoal;
import com.whitefood.moderncod.gameroom.goal.SoloKillGoal;
import com.whitefood.moderncod.gameroom.goal.TeamKillGoal;
import com.whitefood.moderncod.gameroom.manager.TeamManager;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class RoomNBTSerializer {
   private static final String DIMENSION_KEY = "Dimension";

   public RoomNBTSerializer() {
   }

   public static CompoundTag serialize(GameRoom room) {
      CompoundTag nbt = new CompoundTag();
      nbt.putString("id", room.getId());
      nbt.putString("mode", room.getMode().toString());
      nbt.putString("joinMethod", room.getJoinMethod().toString());
      nbt.putBoolean("isRunning", room.isRunning());
      nbt.putInt("tickCount", room.getTickCount());
      nbt.putLong("startTime", room.getStartTime());
      ResourceLocation dimLocation = room.getLevel().dimension().location();
      nbt.putString("Dimension", dimLocation.toString());
      GameGoal goal = room.getGoal();
      if (goal != null) {
         CompoundTag goalTag = new CompoundTag();
         if (goal instanceof SoloKillGoal) {
            goalTag.putString("type", "SOLO_KILL");
         } else if (goal instanceof TeamKillGoal) {
            goalTag.putString("type", "TEAM_KILL");
         }

         nbt.put("goal", goalTag);
      }

      if (room.getArea() != null) {
         CompoundTag areaTag = new CompoundTag();
         areaTag.putDouble("minX", room.getArea().minX);
         areaTag.putDouble("minY", room.getArea().minY);
         areaTag.putDouble("minZ", room.getArea().minZ);
         areaTag.putDouble("maxX", room.getArea().maxX);
         areaTag.putDouble("maxY", room.getArea().maxY);
         areaTag.putDouble("maxZ", room.getArea().maxZ);
         nbt.put("area", areaTag);
      }

      CompoundTag spawnPointsTag = new CompoundTag();

      for (Entry<String, BlockPos> entry : room.getTeamSpawnPoints().entrySet()) {
         spawnPointsTag.put(entry.getKey(), NbtUtils.writeBlockPos(entry.getValue()));
      }

      nbt.put("spawnPoints", spawnPointsTag);
      TeamManager teamManager = room.getTeamManager();
      CompoundTag teamsTag = new CompoundTag();

      for (String teamName : teamManager.getTeamNames()) {
         ListTag membersTag = new ListTag();

         for (UUID memberId : teamManager.getTeamMembers(teamName)) {
            membersTag.add(NbtUtils.createUUID(memberId));
         }

         teamsTag.put(teamName, membersTag);
      }

      nbt.put("teams", teamsTag);
      CompoundTag teamColorsTag = new CompoundTag();

      for (String teamName : teamManager.getTeamNames()) {
         teamColorsTag.putString(teamName, teamManager.getTeamColor(teamName));
      }

      nbt.put("teamColors", teamColorsTag);
      return nbt;
   }

   public static GameRoom deserialize(String roomId, CompoundTag nbt, MinecraftServer server) {
      try {
         GameRoom room = new GameRoom(roomId);
         room.setMode(GameModeType.valueOf(nbt.getString("mode")));
         room.setJoinMethod(JoinMethod.valueOf(nbt.getString("joinMethod")));
         room.setRunning(nbt.getBoolean("isRunning"));
         room.setTickCount(nbt.getInt("tickCount"));
         room.setStartTime(nbt.getLong("startTime"));
         if (nbt.contains("goal", 10)) {
            CompoundTag goalTag = nbt.getCompound("goal");
            String goalType = goalTag.getString("type");
            GameGoal goal = null;
            switch (goalType) {
               case "SOLO_KILL":
                  SoloKillGoal soloGoal = new SoloKillGoal();
                  goal = soloGoal;
                  break;
               case "TEAM_KILL":
                  TeamKillGoal teamGoal = new TeamKillGoal();
                  goal = teamGoal;
            }

            room.setGoal(goal);
         }

         if (nbt.contains("area", 10)) {
            CompoundTag areaTag = nbt.getCompound("area");
            double minX = areaTag.getDouble("minX");
            double minY = areaTag.getDouble("minY");
            double minZ = areaTag.getDouble("minZ");
            double maxX = areaTag.getDouble("maxX");
            double maxY = areaTag.getDouble("maxY");
            double maxZ = areaTag.getDouble("maxZ");
            room.setArea(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
         }

         CompoundTag spawnPointsTag = nbt.getCompound("spawnPoints");

         for (String teamName : spawnPointsTag.getAllKeys()) {
            NbtUtils.readBlockPos(spawnPointsTag, teamName).ifPresent(pos -> room.setTeamSpawnPoint(teamName, pos));
         }

         CompoundTag teamsTag = nbt.getCompound("teams");
         TeamManager teamManager = room.getTeamManager();

         for (String teamName : teamsTag.getAllKeys()) {
            teamManager.createTeam(teamName);
         }

         if (nbt.contains("teamColors", 10)) {
            CompoundTag teamColorsTag = nbt.getCompound("teamColors");

            for (String teamName : teamColorsTag.getAllKeys()) {
               teamManager.setTeamColor(teamName, teamColorsTag.getString(teamName));
            }
         }

         if (nbt.contains("Dimension", 8)) {
            String dimLocationStr = nbt.getString("Dimension");
            ResourceLocation dimLocation = ResourceLocation.parse(dimLocationStr);
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimLocation);
            ServerLevel level = server.getLevel(dimensionKey);
            if (level != null) {
               room.setLevel(level);
            } else {
               Narrator.LOGGER.warn("Failed to load dimension {} for room {}", dimLocationStr, roomId);
            }
         }

         return room;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }
}
