package com.israelsimulator.city.telaviv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelAvivEconomyTest {

    @Test
    @DisplayName("Verify tech item buying and selling valuations")
    void testTechPrices() {
        assertEquals(32, TelAvivEconomy.getTechBuyPrice("laptop"));
        assertEquals(24, TelAvivEconomy.getTechSellPrice("laptop"));
        assertTrue(TelAvivEconomy.getTechBuyPrice("laptop") > TelAvivEconomy.getTechSellPrice("laptop"),
                "Buy price should exceed sell price to ensure economy balance");

        assertEquals(16, TelAvivEconomy.getTechBuyPrice("smartphone"));
        assertEquals(12, TelAvivEconomy.getTechSellPrice("smartphone"));

        assertEquals(8, TelAvivEconomy.getTechBuyPrice("drone_part"));
        assertEquals(6, TelAvivEconomy.getTechSellPrice("drone_part"));

        assertEquals(0, TelAvivEconomy.getTechBuyPrice("unknown_item"));
        assertEquals(0, TelAvivEconomy.getTechSellPrice("unknown_item"));
    }

    @Test
    @DisplayName("Verify Tel Aviv districts definitions and uniqueness")
    void testDistricts() {
        assertNotNull(TelAvivDistricts.ALL);
        assertEquals(6, TelAvivDistricts.ALL.size(), "Should have 6 recognized Tel Aviv districts");

        assertTrue(TelAvivDistricts.ALL.contains(TelAvivDistricts.WHITE_CITY));
        assertTrue(TelAvivDistricts.ALL.contains(TelAvivDistricts.ROTHSCHILD));
        assertTrue(TelAvivDistricts.ALL.contains(TelAvivDistricts.FLORENTIN));
        assertTrue(TelAvivDistricts.ALL.contains(TelAvivDistricts.SARONA));
        assertTrue(TelAvivDistricts.ALL.contains(TelAvivDistricts.STARTUP_DISTRICT));
        assertTrue(TelAvivDistricts.ALL.contains(TelAvivDistricts.TAYELET_BEACH));
    }
}
