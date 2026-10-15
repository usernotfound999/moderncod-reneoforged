package com.whitefood.moderncod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.whitefood.moderncod.networking.ModMessages;
import com.whitefood.moderncod.networking.packet.S2COpenImageUIScreenPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class OpenImageUICommands {
   public OpenImageUICommands(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("codui").requires(source -> source.hasPermission(0)))
            .then(Commands.literal("open").then(Commands.literal("image").executes(OpenImageUICommands::openSelectionScreen)))
      );
   }

   private static int openSelectionScreen(CommandContext<CommandSourceStack> context) {
      if (((CommandSourceStack)context.getSource()).getEntity() instanceof ServerPlayer serverPlayer) {
         ModMessages.sendToPlayer(new S2COpenImageUIScreenPacket(), serverPlayer);
      }

      return 1;
   }
}
