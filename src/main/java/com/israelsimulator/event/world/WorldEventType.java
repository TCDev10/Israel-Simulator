package com.israelsimulator.event.world;

/**
 * World event categories and properties (GAME_DESIGN.md §37, §38, TODO §37).
 */
public enum WorldEventType {
    PUBLIC_SPEECH(
            "public_speech", "Public Speech & Civic Gathering",
            "A civic gathering for open, democratic discourse and free expression.",
            1200L, 1200L // exactly 60 s; ends with the Orator dropping the First Amendment
    ),
    MARKET_DAY(
            "market_day", "Shuk Market Day",
            "Bustling market day with vendor discounts and fresh regional arrivals.",
            6000L, 600L
    ),
    FESTIVAL(
            "festival", "Town Holiday Assembly",
            "Community holiday celebration with traditional songs and festive food.",
            12000L, 1200L
    ),
    CONCERT(
            "concert", "Mediterranean Sunset Concert",
            "Open-air acoustic performance of Hava Nagila and klezmer songs.",
            4800L, 600L
    ),
    BEACH_EVENT(
            "beach_event", "Mediterranean Beach Gathering",
            "Coastal community day with water activities and refreshments.",
            6000L, 600L
    ),
    RELIGIOUS_EVENT(
            "religious_event", "Sacred Prayer Assembly",
            "Communal prayer gathering at ancient holy sites and study centers.",
            6000L, 1200L
    ),
    FOOD_FESTIVAL(
            "food_festival", "Levantine Culinary Expo",
            "Street food artisans cooking falafel, shakshuka, hummus, and rugelach.",
            6000L, 600L
    ),
    TECHNOLOGY_CONFERENCE(
            "technology_conference", "Silicon Alley Tech Summit",
            "High-tech innovators showcasing startup algorithms and drone tech.",
            6000L, 600L
    ),
    RARE_NPC_SPAWN(
            "rare_npc_spawn", "Distinguished Guest Visit",
            "A historic scholar or foreign diplomat arrives for dialogue.",
            4800L, 400L
    ),
    BOSS_EVENT(
            "boss_event", "Ancient Wilderness Assembly",
            "Gathering of courageous explorers facing challenging wilderness trials.",
            12000L, 1200L
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final long durationTicks;
    private final long minParticipationTicks;

    WorldEventType(String id, String displayName, String description,
                   long durationTicks, long minParticipationTicks) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.durationTicks = durationTicks;
        this.minParticipationTicks = minParticipationTicks;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public long getDurationTicks() {
        return durationTicks;
    }

    public long getMinParticipationTicks() {
        return minParticipationTicks;
    }
}
