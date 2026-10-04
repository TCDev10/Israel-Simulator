package com.israelsimulator.economy;

import com.israelsimulator.city.CityLifeManager;
import com.israelsimulator.npc.IsraelNpcData;
import com.israelsimulator.npc.NpcProfession;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CityLifeAndEconomyTest {

    @Test
    @DisplayName("Verify CurrencyUnit conversion and formatting (1 Shekel = 100 Agorot)")
    void testCurrencyConversions() {
        assertEquals(100, CurrencyUnit.SHEKEL.getValueInAgorot());
        assertEquals(1, CurrencyUnit.AGORA.getValueInAgorot());

        assertEquals(550L, CurrencyUnit.toAgorot(5, 50));
        assertEquals(5L, CurrencyUnit.toShekels(550L));
        assertEquals(50L, CurrencyUnit.remainderAgorot(550L));
        assertEquals("5.50 ILS", CurrencyUnit.format(550L));
    }

    @Test
    @DisplayName("Verify IsraelEconomy base prices and regional specializations")
    void testBasePricesAndRegionalModifiers() {
        // Tech: Smartphone costs less in Tel Aviv than Dead Sea
        long telAvivPhone = IsraelEconomy.calculateBuyPrice("smartphone", CityRegion.TEL_AVIV, 0);
        long deadSeaPhone = IsraelEconomy.calculateBuyPrice("smartphone", CityRegion.DEAD_SEA, 0);
        assertTrue(telAvivPhone < deadSeaPhone, "Technology must be cheaper in Tel Aviv tech hub than remote Dead Sea");

        // Judaica: Kippah costs less in Jerusalem than elsewhere
        long jerusalemKippah = IsraelEconomy.calculateBuyPrice("kippah", CityRegion.JERUSALEM, 0);
        long defaultKippah = IsraelEconomy.calculateBuyPrice("kippah", null, 0);
        assertTrue(jerusalemKippah <= defaultKippah, "Judaica must be affordable in Jerusalem center");
    }

    @Test
    @DisplayName("Verify reputation discounts on buy price and bonuses on sell price")
    void testReputationDiscounts() {
        String item = "falafel";
        long standardPrice = IsraelEconomy.calculateBuyPrice(item, CityRegion.TEL_AVIV, 0);
        long discountedPrice = IsraelEconomy.calculateBuyPrice(item, CityRegion.TEL_AVIV, 100);

        assertTrue(discountedPrice < standardPrice, "High reputation player must receive purchase discount");
        double discountRatio = (double) discountedPrice / standardPrice;
        assertTrue(discountRatio >= 0.79 && discountRatio <= 0.81, "Maximum reputation discount must be ~20%");
    }

    @Test
    @DisplayName("Verify anti-exploit bid-ask spread (sell price strictly lower than buy price)")
    void testBidAskSpreadAntiExploit() {
        String[] testItems = { "falafel", "smartphone", "kippah", "olives", "salt_block" };

        for (String item : testItems) {
            for (CityRegion region : CityRegion.values()) {
                long buyPrice = IsraelEconomy.calculateBuyPrice(item, region, 0);
                long sellPrice = IsraelEconomy.calculateSellPrice(item, region, 0);

                assertTrue(sellPrice < buyPrice, "Sell price must be strictly lower than buy price to prevent infinite money cycling");
                assertTrue((double) sellPrice / buyPrice <= 0.85, "Spread must enforce at least 15% transaction margin");
            }
        }
    }

    @Test
    @DisplayName("Verify anti-arbitrage inter-city trade verification")
    void testArbitrageSafety() {
        assertTrue(IsraelEconomy.isArbitrageSafe("smartphone", CityRegion.TEL_AVIV, CityRegion.JERUSALEM));
        assertTrue(IsraelEconomy.isArbitrageSafe("olives", CityRegion.RURAL_GALILEE, CityRegion.TEL_AVIV));
    }

    @Test
    @DisplayName("Verify CityLifeManager population caps and pathfinding throttling for 20 TPS")
    void testCityLifeManagerPerformanceSafeguards() {
        assertTrue(CityLifeManager.MAX_NPCS_PER_CHUNK > 0);
        assertTrue(CityLifeManager.MAX_NPCS_PER_DISTRICT >= CityLifeManager.MAX_NPCS_PER_CHUNK);

        UUID testNpc = UUID.randomUUID();
        // First pathfinding call is allowed
        assertFalse(CityLifeManager.shouldThrottlePathfinding(testNpc, 100L));
        // Immediate next tick is throttled
        assertTrue(CityLifeManager.shouldThrottlePathfinding(testNpc, 101L));
        assertTrue(CityLifeManager.shouldThrottlePathfinding(testNpc, 100L + CityLifeManager.PATHFINDING_COOLDOWN_TICKS - 1L));

        // After cooldown expires, pathfinding is allowed again
        assertFalse(CityLifeManager.shouldThrottlePathfinding(testNpc, 100L + CityLifeManager.PATHFINDING_COOLDOWN_TICKS + 1L));

        CityLifeManager.clearPathfindingThrottle(testNpc);
    }

    @Test
    @DisplayName("Verify CityLifeManager urban activity assignments")
    void testUrbanActivityAssignments() {
        IsraelNpcData data = new IsraelNpcData(UUID.randomUUID(), NpcProfession.CHEF,
                BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO);

        // At 09:00 (work) -> WORKING_AT_STALL
        assertEquals(CityLifeManager.UrbanActivity.WORKING_AT_STALL,
                CityLifeManager.getUrbanActivity(data, 3000L, false));

        // At 12:30 (lunch) -> DINING_AT_CAFE
        assertEquals(CityLifeManager.UrbanActivity.DINING_AT_CAFE,
                CityLifeManager.getUrbanActivity(data, 6500L, false));

        // At 18:30 (social) -> STROLLING_STREET
        assertEquals(CityLifeManager.UrbanActivity.STROLLING_STREET,
                CityLifeManager.getUrbanActivity(data, 12500L, false));
    }
}
