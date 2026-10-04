package com.israelsimulator.city.jerusalem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JerusalemEconomyTest {

    @Test
    @DisplayName("Verify Jerusalem Judaica valuation rates")
    void testJudaicaPrices() {
        assertEquals(1, JerusalemEconomy.getJudaicaBuyPrice("prayer_note"));
        assertEquals(4, JerusalemEconomy.getJudaicaBuyPrice("kippah"));
        assertEquals(6, JerusalemEconomy.getJudaicaBuyPrice("mezuzah"));
        assertEquals(8, JerusalemEconomy.getJudaicaBuyPrice("talit"));
        assertEquals(12, JerusalemEconomy.getJudaicaBuyPrice("tefillin"));
        assertEquals(0, JerusalemEconomy.getJudaicaBuyPrice("unknown"));
    }
}
