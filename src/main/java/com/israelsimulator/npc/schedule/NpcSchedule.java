package com.israelsimulator.npc.schedule;

import com.israelsimulator.npc.NpcProfession;

/**
 * Schedule evaluation authority for NPCs (GAME_DESIGN.md §27, TODO §27).
 * Implements standard daily timeline, Shabbat rest windows, and festival overrides.
 */
public final class NpcSchedule {

    public static final long TICKS_PER_DAY = 24000L;
    public static final long TICKS_PER_WEEK = 168000L; // 7 days

    // Schedule thresholds in Minecraft daytime ticks (0 = 06:00 sunrise)
    public static final long TIME_WAKE = 0L;         // 06:00
    public static final long TIME_WORK_AM = 2000L;   // 08:00
    public static final long TIME_LUNCH = 6000L;     // 12:00 noon
    public static final long TIME_WORK_PM = 8000L;   // 14:00
    public static final long TIME_SOCIAL = 12000L;   // 18:00 sunset
    public static final long TIME_HOME = 16000L;     // 22:00 night
    public static final long TIME_SLEEP = 18000L;    // 00:00 midnight

    private NpcSchedule() {}

    /**
     * Determines whether it is currently Shabbat based on the game world time.
     * In a 7-day cycle: Day 5 sunset (tick 132000) to Day 6 nightfall (tick 156000).
     */
    public static boolean isShabbat(long gameTime) {
        long weekTick = Math.floorMod(gameTime, TICKS_PER_WEEK);
        // Friday sunset (Day 5, tick 12000 = 5 * 24000 + 12000 = 132000)
        // Saturday Havdalah / sunset (Day 6, tick 13000 = 6 * 24000 + 13000 = 157000)
        return weekTick >= 132000L && weekTick < 157000L;
    }

    /**
     * Returns the active schedule state for an NPC given daytime ticks, Shabbat status, and profession.
     */
    public static ScheduleState getScheduleState(long dayTime, boolean isShabbat, NpcProfession profession) {
        long normalizedTime = Math.floorMod(dayTime, TICKS_PER_DAY);

        // Shabbat behavior for observant professions
        if (isShabbat && profession.isObservantShabbat()) {
            if (normalizedTime >= 2000L && normalizedTime < 5000L) {
                return ScheduleState.SHABBAT_PRAYER; // Morning Synagogue service
            } else if (normalizedTime >= 11000L && normalizedTime < 13500L) {
                return ScheduleState.SHABBAT_PRAYER; // Mincha / Evening service
            } else if (normalizedTime >= 18000L || normalizedTime < 1000L) {
                return ScheduleState.SLEEP;
            } else {
                return ScheduleState.SHABBAT_REST;
            }
        }

        // Standard weekday schedule
        if (normalizedTime < TIME_WORK_AM) {
            return ScheduleState.WAKE;
        } else if (normalizedTime < TIME_LUNCH) {
            return ScheduleState.WORK_MORNING;
        } else if (normalizedTime < TIME_WORK_PM) {
            return ScheduleState.LUNCH;
        } else if (normalizedTime < TIME_SOCIAL) {
            return ScheduleState.WORK_AFTERNOON;
        } else if (normalizedTime < TIME_HOME) {
            return ScheduleState.SOCIAL;
        } else if (normalizedTime < TIME_SLEEP) {
            return ScheduleState.HOME;
        } else {
            return ScheduleState.SLEEP;
        }
    }

    /**
     * Checks if the NPC is currently available for commerce/trading.
     */
    public static boolean canNpcTrade(long dayTime, boolean isShabbat, NpcProfession profession) {
        ScheduleState state = getScheduleState(dayTime, isShabbat, profession);
        return state.permitsTrading();
    }
}
