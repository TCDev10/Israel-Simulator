package com.israelsimulator.festival;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Puts the {@link FestivalManager} calendar into the world: announces when a festival (or Shabbat)
 * begins and ends to every player, and greets players who join during one. The Shuk vendors read
 * the same calendar for their stock and their festive look.
 */
public final class FestivalEvents {
    private static final int CHECK_INTERVAL = 100;
    private static @Nullable FestivalType last;
    private static boolean initialised;

    private FestivalEvents() {}

    public static void reset() {
        last = null;
        initialised = false;
    }

    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD || level.getGameTime() % CHECK_INTERVAL != 0) return;
        FestivalType now = FestivalManager.getCurrentFestival(level.getGameTime());
        if (!initialised) {
            initialised = true;
            last = now;
            return;
        }
        if (now == last) return;
        FestivalType ended = last;
        last = now;
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (ended != null) {
                p.sendSystemMessage(Component.translatable(endKey(ended)).withStyle(ChatFormatting.GRAY));
            }
            if (now != null) {
                p.sendSystemMessage(Component.translatable(beginKey(now)).withStyle(ChatFormatting.GOLD));
                p.level().playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.8F, 1.2F);
            }
        }
    }

    public static void onJoin(ServerPlayer player) {
        FestivalType now = FestivalManager.getCurrentFestival(player.level().getGameTime());
        if (now != null) {
            player.sendSystemMessage(Component.translatable(beginKey(now)).withStyle(ChatFormatting.GOLD));
        }
    }

    public static String beginKey(FestivalType type) {
        return switch (type) {
            case SHABBAT -> "message.israel_simulator.festival.begins.shabbat";
            case ROSH_HASHANAH -> "message.israel_simulator.festival.begins.rosh_hashanah";
            case YOM_KIPPUR -> "message.israel_simulator.festival.begins.yom_kippur";
            case SUKKOT -> "message.israel_simulator.festival.begins.sukkot";
            case HANUKKAH -> "message.israel_simulator.festival.begins.hanukkah";
            case PURIM -> "message.israel_simulator.festival.begins.purim";
            case PESACH -> "message.israel_simulator.festival.begins.pesach";
        };
    }

    public static String endKey(FestivalType type) {
        return switch (type) {
            case SHABBAT -> "message.israel_simulator.festival.ends.shabbat";
            case ROSH_HASHANAH -> "message.israel_simulator.festival.ends.rosh_hashanah";
            case YOM_KIPPUR -> "message.israel_simulator.festival.ends.yom_kippur";
            case SUKKOT -> "message.israel_simulator.festival.ends.sukkot";
            case HANUKKAH -> "message.israel_simulator.festival.ends.hanukkah";
            case PURIM -> "message.israel_simulator.festival.ends.purim";
            case PESACH -> "message.israel_simulator.festival.ends.pesach";
        };
    }
}
