package com.whitefood.moderncod.event.custom;

import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.item.IGun;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.data.GenericKillMessageData;
import com.whitefood.moderncod.networking.data.KillMessageData;
import com.whitefood.moderncod.networking.packet.S2CGenericKillMessagePacket;
import com.whitefood.moderncod.networking.packet.S2CKillMessagePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;

@EventBusSubscriber(modid = "moderncod", value = Dist.DEDICATED_SERVER)
public class KillMessageServerHandler {
   public KillMessageServerHandler() {
   }

   @SubscribeEvent
   public static void onPlayerKilledByGun(EntityKillByGunEvent event) {
      boolean isHeadKill = false;
      if (event.getLogicalSide() == LogicalSide.SERVER
         && event.getAttacker() instanceof ServerPlayer killer
         && killer.getMainHandItem().getItem() instanceof IGun gunItem) {
         MinecraftServer server = event.getKilledEntity().getServer();
         if (server != null) {
            if (event.isHeadShot()) {
               isHeadKill = true;
            }

            String killerTeam = killer.getTeam() != null ? killer.getTeam().getName() : "";
            String deadTeam = event.getKilledEntity().getTeam() != null ? event.getKilledEntity().getTeam().getName() : "";
            KillMessageData killMessageData = new KillMessageData(
               killer.getName().getString(),
               event.getKilledEntity().getName().getString(),
               gunItem.getGunId(killer.getMainHandItem()),
               event.isHeadShot(),
               killerTeam,
               deadTeam
            );
            S2CKillMessagePacket packet = new S2CKillMessagePacket(killMessageData);
            server.getPlayerList().getPlayers().forEach(player -> ModMessages.sendToPlayer(packet, player));
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerKilledByMelee(LivingDeathEvent event) {
      if (event.getEntity().level() instanceof ServerLevel level) {
         if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            ItemStack mainHand = killer.getMainHandItem();
            Item item = mainHand.getItem();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId != null) {
               if (item instanceof SwordItem
                  || item instanceof AxeItem
                  || item instanceof BowItem
                  || itemId.equals(ResourceLocation.fromNamespaceAndPath("lrtactical", "melee"))) {
                  Entity deadEntity = event.getEntity();
                  MinecraftServer server = level.getServer();
                  if (server != null) {
                     String killerTeam = killer.getTeam() != null ? killer.getTeam().getName() : "";
                     String deadTeam = deadEntity.getTeam() != null ? deadEntity.getTeam().getName() : "";
                     GenericKillMessageData data = new GenericKillMessageData(
                        killer.getName().getString(),
                        deadEntity.getName().getString(),
                        BuiltInRegistries.ITEM.getKey(item).toString(),
                        false,
                        killerTeam,
                        deadTeam
                     );
                     S2CGenericKillMessagePacket packet = new S2CGenericKillMessagePacket(data);
                     server.getPlayerList().getPlayers().forEach(player -> ModMessages.sendToPlayer(packet, player));
                  }
               }
            }
         }
      }
   }
}
