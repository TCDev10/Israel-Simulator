package com.israelsimulator.reputation;

/**
 * Reputation tiers and gameplay thresholds (GAME_DESIGN.md §31, TODO §31).
 */
public enum ReputationTier {
    EXILED("exiled", -100, -61, 0.25, false, false),
    SUSPICIOUS("suspicious", -60, -1, 0.10, false, false),
    NEUTRAL("neutral", 0, 19, 0.00, false, false),
    RESPECTED("respected", 20, 59, -0.10, true, false),
    HONORED("honored", 60, 89, -0.15, true, true),
    CHAMPION("champion", 90, 100, -0.20, true, true);

    private final String id;
    private final int minScore;
    private final int maxScore;
    private final double priceModifier;
    private final boolean canAccessSpecialTrades;
    private final boolean canAccessRareTrades;

    ReputationTier(String id, int minScore, int maxScore, double priceModifier,
                   boolean canAccessSpecialTrades, boolean canAccessRareTrades) {
        this.id = id;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.priceModifier = priceModifier;
        this.canAccessSpecialTrades = canAccessSpecialTrades;
        this.canAccessRareTrades = canAccessRareTrades;
    }

    public String getId() {
        return id;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public double getPriceModifier() {
        return priceModifier;
    }

    public boolean canAccessSpecialTrades() {
        return canAccessSpecialTrades;
    }

    public boolean canAccessRareTrades() {
        return canAccessRareTrades;
    }

    public static ReputationTier fromScore(int score) {
        int clamped = Math.clamp(score, -100, 100);
        for (ReputationTier tier : values()) {
            if (clamped >= tier.minScore && clamped <= tier.maxScore) {
                return tier;
            }
        }
        return NEUTRAL;
    }
}
