package com.israelsimulator.festival;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * {@code /festival status} for everyone; {@code /festival start <type>} and {@code /festival stop}
 * for operators (force a festival for its normal duration, e.g. to test Shuk stock or decorations).
 */
public final class FestivalCommands {
    private FestivalCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("festival")
                .then(Commands.literal("status").executes(ctx -> {
                    FestivalType f = FestivalManager.getCurrentFestival(ctx.getSource().getLevel().getGameTime());
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.israel_simulator.festival.status",
                            f == null ? Component.translatable("command.israel_simulator.festival.none")
                                    : Component.literal(f.name().toLowerCase(Locale.ROOT))), false);
                    return 1;
                }))
                .then(Commands.literal("stop").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx -> {
                    FestivalManager.clearForcedFestival();
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.israel_simulator.festival.stopped"), true);
                    return 1;
                }))
                .then(Commands.literal("start").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, b) -> {
                                    for (FestivalType t : FestivalType.values()) b.suggest(t.name().toLowerCase(Locale.ROOT));
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "type").toUpperCase(Locale.ROOT);
                                    FestivalType t;
                                    try {
                                        t = FestivalType.valueOf(id);
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendFailure(Component.translatable("command.israel_simulator.festival.unknown"));
                                        return 0;
                                    }
                                    FestivalManager.forceStartFestival(t, ctx.getSource().getLevel().getGameTime());
                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.israel_simulator.festival.started",
                                            Component.literal(t.name().toLowerCase(Locale.ROOT))), true);
                                    return 1;
                                }))));
    }
}
