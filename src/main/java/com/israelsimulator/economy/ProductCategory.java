package com.israelsimulator.economy;

/**
 * Economic product categories for Israel-Simulator (GAME_DESIGN.md §29, TODO §29).
 */
public enum ProductCategory {
    FOOD("food", "category.israel_simulator.food"),
    AGRICULTURE("agriculture", "category.israel_simulator.agriculture"),
    JUDAICA_CULTURAL("judaica_cultural", "category.israel_simulator.judaica_cultural"),
    TECHNOLOGY("technology", "category.israel_simulator.technology"),
    COMMODITIES_MINERALS("commodities_minerals", "category.israel_simulator.commodities_minerals");

    private final String id;
    private final String translationKey;

    ProductCategory(String id, String translationKey) {
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
