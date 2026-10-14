package com.whitefood.moderncod.gameroom.client;

import com.whitefood.moderncod.gameroom.base.GameModeType;
import com.whitefood.moderncod.gameroom.net.C2SJoinRoomPacket;
import com.whitefood.moderncod.gameroom.net.C2SLeaveRoomPacket;
import com.whitefood.moderncod.gameroom.net.C2SRequestRoomListPacket;
import com.whitefood.moderncod.gameroom.net.S2CRoomListPacket;
import com.whitefood.moderncod.networking.ModMessages;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PlayerGameRoomGUI extends Screen {
   private static final int WIDTH = 400;
   private static final int HEIGHT = 300;
   private List<PlayerGameRoomGUI.GameRoomEntry> roomEntries = new CopyOnWriteArrayList<>();
   private Button refreshButton;
   private Button joinButton;
   private Button leaveButton;
   private Button closeButton;
   private int selectedRoomIndex = -1;
   private UUID currentPlayerId;
   private boolean isInRoom = false;
   private String currentRoomId = null;
   private static PlayerGameRoomGUI INSTANCE;

   public PlayerGameRoomGUI() {
      super(Component.literal("游戏房间"));
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         this.currentPlayerId = mc.player.getUUID();
      }

      INSTANCE = this;
   }

   protected void init() {
      int x = (this.width - 400) / 2;
      int y = (this.height - 300) / 2;
      this.refreshButton = Button.builder(Component.literal("刷新房间列表"), button -> this.refreshRoomList())
         .bounds(x + 10, y + 300 - 40, 80, 20)
         .build();
      this.addRenderableWidget(this.refreshButton);
      this.joinButton = Button.builder(Component.literal("加入房间"), button -> this.joinSelectedRoom()).bounds(x + 100, y + 300 - 40, 80, 20).build();
      this.joinButton.active = false;
      this.addRenderableWidget(this.joinButton);
      this.leaveButton = Button.builder(Component.literal("离开房间"), button -> {
         if (this.currentRoomId != null) {
            this.leaveCurrentRoom(this.currentRoomId);
         }
      }).bounds(x + 190, y + 300 - 40, 80, 20).build();
      this.leaveButton.active = false;
      this.addRenderableWidget(this.leaveButton);
      this.closeButton = Button.builder(Component.literal("关闭"), button -> this.onClose()).bounds(x + 280, y + 300 - 40, 80, 20).build();
      this.addRenderableWidget(this.closeButton);
      this.refreshRoomList();
   }

   public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(gui, mouseX, mouseY, partialTick);
      int x = (this.width - 400) / 2;
      int y = (this.height - 300) / 2;
      gui.drawCenteredString(this.font, "游戏房间列表", x + 200, y + 5, Color.WHITE.getRGB());
      gui.drawString(this.font, "房间ID", x + 20, y + 30, Color.LIGHT_GRAY.getRGB());
      gui.drawString(this.font, "模式", x + 150, y + 30, Color.LIGHT_GRAY.getRGB());
      gui.drawString(this.font, "玩家", x + 250, y + 30, Color.LIGHT_GRAY.getRGB());
      gui.drawString(this.font, "状态", x + 320, y + 30, Color.LIGHT_GRAY.getRGB());
      gui.fill(x + 10, y + 50, x + 400 - 10, y + 51, Color.GRAY.getRGB());
      int entryY = y + 60;

      for (int i = 0; i < this.roomEntries.size(); i++) {
         PlayerGameRoomGUI.GameRoomEntry entry = this.roomEntries.get(i);
         if (i == this.selectedRoomIndex) {
            gui.fill(x + 10, entryY - 10, x + 400 - 10, entryY + 10, Color.white.getRGB() & 285212671);
         }

         gui.drawString(this.font, entry.roomId, x + 20, entryY, Color.WHITE.getRGB());
         gui.drawString(this.font, entry.mode.toString(), x + 150, entryY, Color.WHITE.getRGB());
         gui.drawString(this.font, entry.playerCount + "/16", x + 250, entryY, Color.WHITE.getRGB());
         gui.drawString(this.font, entry.status, x + 320, entryY, entry.isRunning ? Color.RED.getRGB() : Color.GREEN.getRGB());
         entryY += 25;
      }

      super.render(gui, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = (this.width - 400) / 2;
      int y = (this.height - 300) / 2;
      if (mouseX >= x + 10 && mouseX <= x + 400 - 10 && mouseY >= y + 60 && mouseY <= y + 300 - 50) {
         int entryIndex = (int)((mouseY - (y + 60)) / 25.0);
         if (entryIndex >= 0 && entryIndex < this.roomEntries.size()) {
            this.selectedRoomIndex = entryIndex;
            this.updateButtonStates();
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public void onClose() {
      INSTANCE = null;
      Minecraft.getInstance().setScreen(new PlayerMainGUI());
   }

   private void refreshRoomList() {
      this.roomEntries.clear();
      ModMessages.sendToServer(new C2SRequestRoomListPacket());
      this.joinButton.active = false;
   }

   public void handleRoomListData(List<S2CRoomListPacket.RoomData> roomDataList) {
      Minecraft.getInstance().execute(() -> {
         this.roomEntries.clear();
         this.isInRoom = false;
         this.currentRoomId = null;

         for (S2CRoomListPacket.RoomData data : roomDataList) {
            this.roomEntries.add(new PlayerGameRoomGUI.GameRoomEntry(data.roomId(), data.mode(), data.playerCount(), data.isRunning()));
            if (data.playerUuids().contains(this.currentPlayerId)) {
               this.isInRoom = true;
               this.currentRoomId = data.roomId();
            }
         }

         Collections.sort(this.roomEntries, (a, b) -> {
            if (a.isRunning == b.isRunning) {
               return 0;
            } else {
               return a.isRunning ? 1 : -1;
            }
         });
         this.updateButtonStates();
      });
   }

   private void joinSelectedRoom() {
      if (this.selectedRoomIndex >= 0 && this.selectedRoomIndex < this.roomEntries.size()) {
         String roomId = this.roomEntries.get(this.selectedRoomIndex).roomId;
         C2SJoinRoomPacket packet = new C2SJoinRoomPacket(roomId);
         ModMessages.sendToServer(packet);
         new Thread(() -> {
            try {
               Thread.sleep(500L);
               Minecraft.getInstance().execute(this::refreshRoomList);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         }).start();
      }
   }

   private void leaveCurrentRoom(String roomId) {
      C2SLeaveRoomPacket packet = new C2SLeaveRoomPacket(roomId);
      ModMessages.sendToServer(packet);
      new Thread(() -> {
         try {
            Thread.sleep(500L);
            Minecraft.getInstance().execute(() -> {
               this.isInRoom = false;
               this.currentRoomId = null;
               this.refreshRoomList();
            });
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }).start();
   }

   private void updateButtonStates() {
      this.joinButton.active = !this.isInRoom && this.selectedRoomIndex >= 0 && this.selectedRoomIndex < this.roomEntries.size();
      this.leaveButton.active = this.isInRoom;
   }

   private static class GameRoomEntry {
      String roomId;
      GameModeType mode;
      int playerCount;
      boolean isRunning;
      String status;

      public GameRoomEntry(String roomId, GameModeType mode, int playerCount, boolean isRunning) {
         this.roomId = roomId;
         this.mode = mode;
         this.playerCount = playerCount;
         this.isRunning = isRunning;
         this.status = isRunning ? "游戏中" : "等待中";
      }
   }

   public static class PacketHandler {
      public PacketHandler() {
      }

      public static void handle(S2CRoomListPacket packet, IPayloadContext ctx) {
         if (PlayerGameRoomGUI.INSTANCE != null) {
            PlayerGameRoomGUI.INSTANCE.handleRoomListData(packet.getRoomDataList());
         }
      }
   }
}
