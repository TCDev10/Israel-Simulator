package com.israelsimulator.event.world.plane;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Commands for managing and triggering the Twin Towers plane crash event:
 * {@code /israelsim event plane_crash start [pos]}
 * {@code /israelsim event plane_crash stop}
 * {@code /israelsim towers setpos [pos]}
 */
public final class PlaneCrashCommands {

    private PlaneCrashCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("israelsim")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("event").then(Commands.literal("plane_crash")
                        .then(Commands.literal("start")
                                .executes(ctx -> start(ctx, BlockPos.containing(ctx.getSource().getPosition())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> start(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("stop").executes(ctx -> {
                            if (!TwinTowersPlaneEvent.isActive()) {
                                ctx.getSource().sendFailure(Component.literal("§cNessun evento aereo attivo al momento."));
                                return 0;
                            }
                            TwinTowersPlaneEvent.stopEvent(ctx.getSource().getLevel());
                            ctx.getSource().sendSuccess(() -> Component.literal("§aEvento aereo arrestato."), true);
                            return 1;
                        }))))
                .then(Commands.literal("towers")
                        .then(Commands.literal("setpos")
                                .executes(ctx -> setTowersPos(ctx, BlockPos.containing(ctx.getSource().getPosition())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> setTowersPos(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))));
    }

    private static int start(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        ServerLevel level = ctx.getSource().getLevel();
        if (!TwinTowersPlaneEvent.startEvent(level, pos)) {
            ctx.getSource().sendFailure(Component.literal("§cImpossibile avviare l'evento: nessuna twin_towers caricata vicino, aereo già in volo o nessun punto di partenza caricato."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("§6Allarme antiaereo attivato! Aereo in arrivo verso le coordinate " + pos.toShortString()), true);
        return 1;
    }

    private static int setTowersPos(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        TwinTowersPlaneEvent.setTowersOrigin(pos);
        ctx.getSource().sendSuccess(() -> Component.literal("§aOrigine Torri Gemelle impostata a " + pos.toShortString()), true);
        return 1;
    }
}

