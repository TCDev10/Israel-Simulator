package com.israelsimulator.trading;

import com.israelsimulator.reputation.ReputationFaction;
import com.israelsimulator.reputation.ReputationManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradingAndBlessedTraderTest {

    @Test
    @DisplayName("Verify special trades listings across all categories")
    void testSpecialTradesListings() {
        List<IsraelVillagerTrades.SpecialTrade> cultural = IsraelVillagerTrades.getCulturalTrades();
        assertFalse(cultural.isEmpty());
        assertTrue(cultural.stream().anyMatch(t -> t.outputItemId().equals("kippah")));
        assertTrue(cultural.stream().anyMatch(t -> t.outputItemId().equals("tefillin")));

        List<IsraelVillagerTrades.SpecialTrade> agriculture = IsraelVillagerTrades.getAgriculturalTrades();
        assertFalse(agriculture.isEmpty());
        assertTrue(agriculture.stream().anyMatch(t -> t.outputItemId().equals("olives")));

        List<IsraelVillagerTrades.SpecialTrade> tech = IsraelVillagerTrades.getTechTrades();
        assertFalse(tech.isEmpty());
        assertTrue(tech.stream().anyMatch(t -> t.outputItemId().equals("smartphone")));

        List<IsraelVillagerTrades.SpecialTrade> food = IsraelVillagerTrades.getFoodTrades();
        assertFalse(food.isEmpty());
        assertTrue(food.stream().anyMatch(t -> t.outputItemId().equals("falafel")));
    }

    @Test
    @DisplayName("Verify Blessed Trader eligibility and discounts")
    void testBlessedTraderPerks() {
        UUID player = UUID.randomUUID();
        ReputationFaction faction = ReputationFaction.RELIGIOUS;

        // Base price
        int base = 100;
        int normalPrice = IsraelVillagerTrades.calculateAdjustedPrice(base, player, faction, false);
        assertEquals(base, normalPrice);

        // Blessed trader discount (15% additional discount)
        int blessedPrice = IsraelVillagerTrades.calculateAdjustedPrice(base, player, faction, true);
        assertEquals(85, blessedPrice);
        assertTrue(blessedPrice < normalPrice);
    }

    @Test
    @DisplayName("Verify reputation modifier impacts trade prices")
    void testReputationTradePriceAdjustment() {
        UUID player = UUID.randomUUID();
        ReputationFaction faction = ReputationFaction.MERCHANT;

        int base = 100;
        // Respected tier (-10%)
        ReputationManager.setReputation(player, faction, 30);
        int respectedPrice = IsraelVillagerTrades.calculateAdjustedPrice(base, player, faction, false);
        assertEquals(90, respectedPrice);

        // Champion tier (-20%)
        ReputationManager.setReputation(player, faction, 95);
        int championPrice = IsraelVillagerTrades.calculateAdjustedPrice(base, player, faction, false);
        assertEquals(80, championPrice);

        // Exiled tier (+25%)
        ReputationManager.setReputation(player, faction, -80);
        int exiledPrice = IsraelVillagerTrades.calculateAdjustedPrice(base, player, faction, false);
        assertEquals(125, exiledPrice);

        ReputationManager.clearForPlayer(player);
    }

    @Test
    @DisplayName("Verify trade validation prevents exploits, stock exhaustion, and unauthorized access")
    void testTradeValidation() {
        IsraelVillagerTrades.SpecialTrade regularTrade =
                new IsraelVillagerTrades.SpecialTrade("shekel", 35, "kippah", 1, 10, false, false);
        IsraelVillagerTrades.SpecialTrade rareTrade =
                new IsraelVillagerTrades.SpecialTrade("shekel", 350, "tefillin", 1, 4, true, false);
        IsraelVillagerTrades.SpecialTrade blessedTrade =
                new IsraelVillagerTrades.SpecialTrade("shekel", 100, "ancient_coin", 1, 2, true, true);

        // Insufficient funds
        assertFalse(IsraelVillagerTrades.validateTrade(regularTrade, 20, 0, false, false));

        // Sufficient funds
        assertTrue(IsraelVillagerTrades.validateTrade(regularTrade, 35, 0, false, false));

        // Out of stock
        assertFalse(IsraelVillagerTrades.validateTrade(regularTrade, 50, 10, false, false));

        // Rare trade without honored standing
        assertFalse(IsraelVillagerTrades.validateTrade(rareTrade, 400, 0, false, false));
        // Rare trade with rare access
        assertTrue(IsraelVillagerTrades.validateTrade(rareTrade, 400, 0, true, false));

        // Blessed trade without blessing
        assertFalse(IsraelVillagerTrades.validateTrade(blessedTrade, 200, 0, true, false));
        // Blessed trade with blessing
        assertTrue(IsraelVillagerTrades.validateTrade(blessedTrade, 200, 0, false, true));
    }
}
