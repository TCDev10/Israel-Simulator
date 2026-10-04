package com.israelsimulator.reputation;

/**
 * Factions and categories for Israel-Simulator reputation system (GAME_DESIGN.md §31, TODO §31).
 */
public enum ReputationFaction {
    MERCHANT("merchant", "faction.israel_simulator.merchant"),
    CITY("city", "faction.israel_simulator.city"),
    VILLAGE("village", "faction.israel_simulator.village"),
    RELIGIOUS("religious", "faction.israel_simulator.religious"),
    TECH_DISTRICT("tech_district", "faction.israel_simulator.tech_district");

    private final String id;
    private final String translationKey;

    ReputationFaction(String id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
