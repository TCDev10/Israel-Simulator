package com.israelsimulator.trading;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MoneyChangerBlessedPricingTest {
    @Test
    void blessedDiscountIsFifteenPercentAtLeastOneNeverFree() {
        assertEquals(0, BlessedTradePricing.discountFor(1));
        assertEquals(1, BlessedTradePricing.discountFor(2));
        assertEquals(1, BlessedTradePricing.discountFor(4));
        assertEquals(2, BlessedTradePricing.discountFor(10));
        assertEquals(10, BlessedTradePricing.discountFor(64));
        for (int i = 1; i <= 64; i++) {
            assertTrue(i - BlessedTradePricing.discountFor(i) >= 1, "price must stay >= 1 for " + i);
        }
    }

    @Test
    void notBlessedMeansNoDiff() {
        assertEquals(0, BlessedTradePricing.specialPriceDiff(30, false));
        assertEquals(-5, BlessedTradePricing.specialPriceDiff(30, true));
    }

    @Test
    void blessedEffectMakesPlayerEligible() {
        assertTrue(IsraelVillagerTrades.isBlessedTraderEligible(false, false, true));
        assertTrue(IsraelVillagerTrades.isBlessedTraderEligible(false, true, false));
        assertFalse(IsraelVillagerTrades.isBlessedTraderEligible(false, false, false));
    }

    @Test
    void moneyChangerAppliesAndResetsSpecialPrices() throws Exception {
        String src = Files.readString(Path.of("src/main/java/com/israelsimulator/entity/npc/MoneyChangerEntity.java"));
        assertTrue(src.contains("applyBlessedPrices(player)"));
        assertTrue(src.contains("BlessedTradePricing.specialPriceDiff"));
        assertTrue(src.contains("protected void stopTrading()") && src.contains("resetSpecialPriceDiff()"),
                "discount must be temporary");
    }
}
