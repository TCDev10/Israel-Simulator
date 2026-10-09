package com.israelsimulator.mossad;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /mossad accept <mission> | abandon | status} for every player (the handler's chat menu runs
 * {@code accept}; accepting requires standing next to a Mossad Handler).
 * {@code /mossad spawn_agents [count]} and {@code /mossad reputation <value>} are for operators.
 */
public final class MossadCommands {
    private MossadCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mossad")
                .then(Commands.literal("accept").then(Commands.argument("mission", StringArgumentType.word())
                        .suggests((ctx, b) -> {
                            for (MossadMission m : MossadMission.values()) {
                                b.suggest(m.id());
                            }
                            return b.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            MossadMission m = MossadMission.byId(StringArgumentType.getString(ctx, "mission"));
                            if (m == null) {
                                ctx.getSource().sendFailure(Component.translatable("mossad.israel_simulator.unknown_mission"));
                                return 0;
                            }
                            return MossadMissions.accept(player, m);
                        })))
                .then(Commands.literal("abandon").executes(ctx -> {
                    MossadMissions.abandon(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("status").executes(ctx -> {
                    MossadMissions.status(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("spawn_agents").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> spawn(ctx.getSource(), 2))
                        .then(Commands.argument("count", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 8))
                                .executes(ctx -> spawn(ctx.getSource(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "count")))))
                .then(Commands.literal("reputation").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("value", com.mojang.brigadier.arguments.IntegerArgumentType.integer(-100, 100))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    MossadData data = MossadData.get(ctx.getSource().getServer());
                                    int v = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "value");
                                    data.adjustReputation(p.getUUID(), v - data.reputation(p.getUUID()));
                                    ctx.getSource().sendSuccess(() -> Component.translatable("mossad.israel_simulator.reputation_set", v), true);
                                    return 1;
                                }))));
    }

    private static int spawn(CommandSourceStack source, int count) {
        var level = source.getLevel();
        var at = net.minecraft.core.BlockPos.containing(source.getPosition());
        int n = MossadMissions.spawnAgents(level, at, count, level.getRandom());
        source.sendSuccess(() -> Component.translatable("mossad.israel_simulator.spawned", n), true);
        return n;
    }
}
