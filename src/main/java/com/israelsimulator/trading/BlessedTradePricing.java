package com.israelsimulator.trading;

/**
 * Blessed Trader pricing for mod merchants (PLAN.md Phase 37, GAME_DESIGN.md Blessed Effect).
 *
 * <p>A player with Blessed, Blessed Trader or the Rabbi's Crown gets a 15% discount on the
 * first cost of every offer (same factor as {@link IsraelVillagerTrades#calculateAdjustedPrice}),
 * at least 1 item off when the price is above 1, and never below 1 item. The discount is applied
 * as a temporary special-price diff while trading and reset when trading stops, so offers are
 * never permanently mutated and nothing can be farmed.
 */
public final class BlessedTradePricing {
    public static final double BLESSED_DISCOUNT = 0.15D;

    private BlessedTradePricing() {}

    /** Number of items removed from a cost of {@code baseCount} for a blessed player (>= 0). */
    public static int discountFor(int baseCount) {
        if (baseCount <= 1) {
            return 0;
        }
        int discount = Math.max(1, (int) Math.round(baseCount * BLESSED_DISCOUNT));
        return Math.min(discount, baseCount - 1);
    }

    /** Special price diff to set on an offer (negative = cheaper). */
    public static int specialPriceDiff(int baseCount, boolean blessed) {
        return blessed ? -discountFor(baseCount) : 0;
    }
}
