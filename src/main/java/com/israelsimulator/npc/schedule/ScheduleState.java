package com.israelsimulator.npc.schedule;

/**
 * Daily activity states for Israel-Simulator NPCs (GAME_DESIGN.md §27, TODO §27).
 */
public enum ScheduleState {
    WAKE("wake", "Waking up and preparing for the day", false),
    WORK_MORNING("work_morning", "Morning work and duties", true),
    LUNCH("lunch", "Lunch break at cafe or home", true),
    WORK_AFTERNOON("work_afternoon", "Afternoon work and market trading", true),
    SOCIAL("social", "Socializing at squares, cafes, and parks", true),
    HOME("home", "Returning home and leisure", false),
    SLEEP("sleep", "Resting and sleeping", false),
    SHABBAT_REST("shabbat_rest", "Shabbat holy rest and family meal", false),
    SHABBAT_PRAYER("shabbat_prayer", "Shabbat synagogue prayer service", false);

    private final String id;
    private final String description;
    private final boolean permitsTrading;

    ScheduleState(String id, String description, boolean permitsTrading) {
        this.id = id;
        this.description = description;
        this.permitsTrading = permitsTrading;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean permitsTrading() {
        return permitsTrading;
    }
}
