package com.israelsimulator.festival;

/**
 * Jewish festivals and holidays in Israel-Simulator (GAME_DESIGN.md §32–35, TODO §32–35).
 */
public enum FestivalType {
    SHABBAT(
            "shabbat",
            "Shabbat",
            "The holy weekly day of rest, contemplation, and family peace.",
            "challah",
            "Shabbat Shalom!",
            25000L
    ),
    ROSH_HASHANAH(
            "rosh_hashanah",
            "Rosh Hashanah",
            "The Jewish New Year, celebrated with shofar sounding and sweet wishes.",
            "citrus",
            "Shana Tova U'Metuka!",
            48000L
    ),
    YOM_KIPPUR(
            "yom_kippur",
            "Yom Kippur",
            "The Day of Atonement, marked by solemn fasting, prayer, and forgiveness.",
            "none",
            "Gmar Chatima Tova!",
            24000L
    ),
    SUKKOT(
            "sukkot",
            "Sukkot",
            "The Feast of Tabernacles and autumn harvest festival.",
            "dates",
            "Chag Sukkot Sameach!",
            48000L
    ),
    HANUKKAH(
            "hanukkah",
            "Hanukkah",
            "The Festival of Lights celebrating miracles with the eight-branched Menorah and Sufganiyot.",
            "sufganiyah",
            "Chag Hanukkah Sameach!",
            192000L // 8 full in-game days
    ),
    PURIM(
            "purim",
            "Purim",
            "The joyous celebration of deliverance with costumes, charity, and Hamantashen.",
            "hamantash",
            "Chag Purim Sameach!",
            24000L
    ),
    PESACH(
            "pesach",
            "Pesach (Passover)",
            "The Festival of Freedom commemorating the Exodus, celebrated with unleavened Matzo.",
            "matzo",
            "Chag Pesach Sameach!",
            48000L
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final String primaryFood;
    private final String greeting;
    private final long durationTicks;

    FestivalType(String id, String displayName, String description,
                 String primaryFood, String greeting, long durationTicks) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.primaryFood = primaryFood;
        this.greeting = greeting;
        this.durationTicks = durationTicks;
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

    public String getPrimaryFood() {
        return primaryFood;
    }

    public String getGreeting() {
        return greeting;
    }

    public long getDurationTicks() {
        return durationTicks;
    }
}
