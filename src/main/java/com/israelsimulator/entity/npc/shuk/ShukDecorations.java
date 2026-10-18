package com.israelsimulator.entity.npc.shuk;

import com.israelsimulator.festival.FestivalType;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Temporary festival decorations that Shuk vendors put on their stall counter while a festival
 * is on, and take away when it ends. Pure logic: which block for which festival, and where to try.
 */
public final class ShukDecorations {

    /** Most decorations a single vendor puts out. */
    public static final int MAX_PER_VENDOR = 2;

    /** A decoration: block id and, for candles and the menorah, how many candles (0 = default). */
    public record Decoration(String blockId, int candles, boolean lit) {}

    private ShukDecorations() {}

    /** The decoration for a festival, or null when nothing is put out (no festival, Yom Kippur). */
    public static @Nullable Decoration forFestival(@Nullable FestivalType festival, int hanukkahNight) {
        if (festival == null) return null;
        return switch (festival) {
            case HANUKKAH -> new Decoration("israel_simulator:menorah", Math.max(1, Math.min(8, hanukkahNight)), false);
            case SHABBAT -> new Decoration("minecraft:white_candle", 2, true);
            case ROSH_HASHANAH -> new Decoration("minecraft:yellow_candle", 1, true);
            case SUKKOT -> new Decoration("minecraft:lantern", 0, false);
            case PURIM -> new Decoration("minecraft:cake", 0, false);
            case PESACH -> new Decoration("minecraft:decorated_pot", 0, false);
            case YOM_KIPPUR -> null;
        };
    }

    /**
     * Offsets from the vendor's feet to try, nearest first: the counter top is one block up and
     * one or two blocks away. The vendor's own column is never used.
     */
    public static List<int[]> candidateOffsets() {
        List<int[]> out = new ArrayList<>();
        for (int r = 1; r <= 2; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == r) out.add(new int[] {dx, 1, dz});
                }
            }
        }
        return out;
    }
}
