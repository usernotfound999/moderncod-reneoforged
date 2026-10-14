package com.whitefood.moderncod.networking.packet;

import com.whitefood.moderncod.networking.CommonClasses;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class C2SMoveItemPacket implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<C2SMoveItemPacket> TYPE = new CustomPacketPayload.Type<>(
      ResourceLocation.fromNamespaceAndPath("moderncod", "move_item")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, C2SMoveItemPacket> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> msg.toBytes(buf), C2SMoveItemPacket::new
   );

   private final int selectedIndex;

   public C2SMoveItemPacket(int selectedIndex) {
      this.selectedIndex = selectedIndex;
   }

   public C2SMoveItemPacket(RegistryFriendlyByteBuf buf) {
      this.selectedIndex = buf.readInt();
   }

   public void toBytes(RegistryFriendlyByteBuf buf) {
      buf.writeInt(this.selectedIndex);
   }

   @Override
   public CustomPacketPayload.Type<C2SMoveItemPacket> type() {
      return TYPE;
   }

   public static void handle(C2SMoveItemPacket packet, IPayloadContext context) {
      if (!(context.player() instanceof ServerPlayer player)) {
         return;
      }
      Inventory inventory = player.getInventory();
      int targetSlot = 3;
      if (packet.selectedIndex >= 0 && packet.selectedIndex < CommonClasses.ITEMS.size()) {
         ResourceLocation itemId = CommonClasses.ITEMS.get(packet.selectedIndex);
         Item item = BuiltInRegistries.ITEM.get(itemId);
         if (item != null) {
            boolean hasSwapped = false;

            for (int i = 0; i < inventory.items.size(); i++) {
               ItemStack stack = inventory.getItem(i);
               if (stack.getItem() == item && i != targetSlot) {
                  ItemStack currentTargetStack = inventory.getItem(targetSlot);
                  inventory.setItem(targetSlot, stack);
                  inventory.setItem(i, currentTargetStack);
                  hasSwapped = true;
                  break;
               }
            }

            if (hasSwapped) {
               inventory.selected = targetSlot;
               player.inventoryMenu.broadcastChanges();
               player.connection.send(new ClientboundSetCarriedItemPacket(targetSlot));
            }
         }
      }
   }
}
