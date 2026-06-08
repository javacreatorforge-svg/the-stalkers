package com.redstonedev.thestalkers.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.redstonedev.thestalkers.event.StalkerSpawns;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class StalkersCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stalkers")
                .requires(src -> src.hasPermission(0))
                .then(Commands.literal("run")
                        .then(Commands.literal("stalkerstalk").executes(c -> run(c, "stalkerstalk")))
                        .then(Commands.literal("stalkermad").executes(c -> run(c, "stalkermad")))
                        .then(Commands.literal("stalkerwindow").executes(c -> run(c, "stalkerwindow")))
                        .then(Commands.literal("stalkerbehind").executes(c -> run(c, "stalkerbehind")))
                        .then(Commands.literal("goatstalk").executes(c -> run(c, "goatstalk")))
                        .then(Commands.literal("goatwindow").executes(c -> run(c, "goatwindow")))
                        .then(Commands.literal("goatbehind").executes(c -> run(c, "goatbehind")))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, String which) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) { src.sendFailure(Component.literal("Must be run by a player.")); return 0; }
        ServerLevel level = src.getLevel();

        if (which.equals("stalkerstalk"))       StalkerSpawns.stalkerStalk(level, player);
        else if (which.equals("stalkermad"))     StalkerSpawns.stalkerMad(level, player);
        else if (which.equals("stalkerwindow"))  { if (!StalkerSpawns.stalkerWindow(level, player)) { src.sendFailure(Component.literal("No window (glass) nearby.")); return 0; } }
        else if (which.equals("stalkerbehind"))  StalkerSpawns.stalkerBehind(level, player);
        else if (which.equals("goatstalk"))      StalkerSpawns.goatStalk(level, player);
        else if (which.equals("goatwindow"))     { if (!StalkerSpawns.goatWindow(level, player)) { src.sendFailure(Component.literal("No window (glass) nearby.")); return 0; } }
        else if (which.equals("goatbehind"))     StalkerSpawns.goatBehind(level, player);

        src.sendSuccess(Component.literal("Ran: " + which), false);
        return 1;
    }
}
