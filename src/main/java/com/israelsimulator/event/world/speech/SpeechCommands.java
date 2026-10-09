package com.israelsimulator.event.world.speech;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/** {@code /israelsim event speech start [pos] | stop | status} (operators only). */
public final class SpeechCommands {
    private SpeechCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("israelsim")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("event").then(Commands.literal("speech")
                        .then(Commands.literal("start")
                                .executes(ctx -> {
                                    ServerLevel level = ctx.getSource().getLevel();
                                    BlockPos at = BlockPos.containing(ctx.getSource().getPosition());
                                    return start(ctx, level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at));
                                })
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> start(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                        .then(Commands.literal("stop").executes(ctx -> {
                            if (!PublicSpeechEvent.isActive()) {
                                ctx.getSource().sendFailure(Component.translatable("commands.israel_simulator.speech.not_active"));
                                return 0;
                            }
                            PublicSpeechEvent.end(ctx.getSource().getLevel(), true);
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.israel_simulator.speech.stopped"), true);
                            return 1;
                        }))
                        .then(Commands.literal("status").executes(ctx -> {
                            if (!PublicSpeechEvent.isActive()) {
                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.israel_simulator.speech.not_active"), false);
                                return 0;
                            }
                            BlockPos c = PublicSpeechEvent.center();
                            long secs = PublicSpeechEvent.remainingTicks(ctx.getSource().getLevel().getGameTime()) / 20;
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.israel_simulator.speech.status",
                                    c.getX(), c.getY(), c.getZ(), secs), false);
                            return 1;
                        })))));
    }

    private static int start(CommandContext<CommandSourceStack> ctx, BlockPos ground) {
        if (!PublicSpeechEvent.start(ctx.getSource().getLevel(), ground)) {
            ctx.getSource().sendFailure(Component.translatable("commands.israel_simulator.speech.already_active"));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.israel_simulator.speech.started",
                ground.getX(), ground.getY(), ground.getZ()), true);
        return 1;
    }
}
