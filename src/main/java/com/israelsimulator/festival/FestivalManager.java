package com.israelsimulator.festival;

import com.israelsimulator.npc.schedule.NpcSchedule;

/**
 * Server-authoritative coordinator of Jewish festivals, calendar cycles,
 * and holiday activities (GAME_DESIGN.md §32–35, TODO §32–35).
 */
public final class FestivalManager {

    // 1 in-game year = 120 Minecraft days = 2,880,000 ticks
    public static final long TICKS_PER_YEAR = 2880000L;
    public static final long TICKS_PER_MONTH = 240000L; // 10 days per month

    private static FestivalType forcedFestival = null;
    private static long forcedFestivalEndTick = 0L;

    private FestivalManager() {}

    /**
     * Determines which festival is currently active at the given game time.
     */
    public static FestivalType getCurrentFestival(long gameTime) {
        if (forcedFestival != null && gameTime < forcedFestivalEndTick) {
            return forcedFestival;
        } else if (forcedFestival != null) {
            forcedFestival = null;
        }

        // 1. Yearly calendar progression
        long yearTick = Math.floorMod(gameTime, TICKS_PER_YEAR);
        long dayOfYear = yearTick / 24000L; // 0 to 119

        // Rosh Hashanah: Days 1-2
        if (dayOfYear >= 1 && dayOfYear <= 2) {
            return FestivalType.ROSH_HASHANAH;
        }
        // Yom Kippur: Day 10
        if (dayOfYear == 10) {
            return FestivalType.YOM_KIPPUR;
        }
        // Sukkot: Days 15-16
        if (dayOfYear >= 15 && dayOfYear <= 16) {
            return FestivalType.SUKKOT;
        }
        // Hanukkah: Days 35-42 (8 consecutive days)
        if (dayOfYear >= 35 && dayOfYear <= 42) {
            return FestivalType.HANUKKAH;
        }
        // Purim: Day 74
        if (dayOfYear == 74) {
            return FestivalType.PURIM;
        }
        // Pesach: Days 90-91
        if (dayOfYear >= 90 && dayOfYear <= 91) {
            return FestivalType.PESACH;
        }

        // 2. Weekly Shabbat
        if (NpcSchedule.isShabbat(gameTime)) {
            return FestivalType.SHABBAT;
        }

        return null;
    }

    public static boolean isFestivalActive(FestivalType type, long gameTime) {
        return getCurrentFestival(gameTime) == type;
    }

    /**
     * For Hanukkah: returns which candle night is currently active (1 through 8).
     * Returns 0 if Hanukkah is not currently active.
     */
    public static int getHanukkahNight(long gameTime) {
        FestivalType current = getCurrentFestival(gameTime);
        if (current != FestivalType.HANUKKAH) {
            return 0;
        }

        long yearTick = Math.floorMod(gameTime, TICKS_PER_YEAR);
        long dayOfYear = yearTick / 24000L;

        if (dayOfYear >= 35 && dayOfYear <= 42) {
            return (int) (dayOfYear - 35 + 1); // 1 to 8
        }

        // If forced, calculate based on elapsed ticks
        return 1;
    }

    /**
     * Administratively triggers a festival for its standard duration.
     */
    public static void forceStartFestival(FestivalType type, long gameTime) {
        forcedFestival = type;
        forcedFestivalEndTick = gameTime + (type != null ? type.getDurationTicks() : 0L);
    }

    public static void clearForcedFestival() {
        forcedFestival = null;
        forcedFestivalEndTick = 0L;
    }

    /**
     * Gets the greeting associated with the active festival, or a standard shalom if none.
     */
    public static String getContextualGreeting(long gameTime) {
        FestivalType festival = getCurrentFestival(gameTime);
        return festival != null ? festival.getGreeting() : "Shalom!";
    }
}
