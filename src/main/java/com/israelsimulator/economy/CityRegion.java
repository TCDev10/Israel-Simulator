package com.israelsimulator.economy;

/**
 * Regional markets with localized price modifiers (GAME_DESIGN.md §29, TODO §29).
 */
public enum CityRegion {
    JERUSALEM("jerusalem", 1.25, 0.85, 1.10, 1.0, 1.0),
    TEL_AVIV("tel_aviv", 1.0, 1.0, 0.85, 1.15, 1.0),
    JAFFA("jaffa", 1.15, 0.95, 1.0, 1.10, 1.0),
    DEAD_SEA("dead_sea", 0.9, 1.0, 1.30, 1.20, 0.70),
    RURAL_GALILEE("rural_galilee", 1.0, 1.0, 1.10, 0.85, 1.10);

    private final String id;
    private final double culturalMultiplier;
    private final double judaicaBuyCostMultiplier;
    private final double technologyMultiplier;
    private final double foodMultiplier;
    private final double mineralMultiplier;

    CityRegion(String id, double culturalMultiplier, double judaicaBuyCostMultiplier,
               double technologyMultiplier, double foodMultiplier, double mineralMultiplier) {
        this.id = id;
        this.culturalMultiplier = culturalMultiplier;
        this.judaicaBuyCostMultiplier = judaicaBuyCostMultiplier;
        this.technologyMultiplier = technologyMultiplier;
        this.foodMultiplier = foodMultiplier;
        this.mineralMultiplier = mineralMultiplier;
    }

    public String getId() {
        return id;
    }

    public double getCategoryMultiplier(ProductCategory category) {
        return switch (category) {
            case JUDAICA_CULTURAL -> judaicaBuyCostMultiplier;
            case TECHNOLOGY -> technologyMultiplier;
            case FOOD -> foodMultiplier;
            case COMMODITIES_MINERALS -> mineralMultiplier;
            case AGRICULTURE -> foodMultiplier * 0.95;
        };
    }

    public double getJudaicaBuyCostMultiplier() {
        return judaicaBuyCostMultiplier;
    }
}
