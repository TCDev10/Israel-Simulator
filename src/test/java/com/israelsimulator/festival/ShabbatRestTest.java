package com.israelsimulator.festival;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ShabbatRestTest {

    @AfterEach
    void clear() {
        FestivalManager.clearForcedFestival();
    }

    @Test
    void restNeedsAMinuteWithoutWork() {
        assertFalse(ShabbatRest.rested(1000, 1000));
        assertFalse(ShabbatRest.rested(1000, 1000 + ShabbatRest.REST_TICKS - 1));
        assertTrue(ShabbatRest.rested(1000, 1000 + ShabbatRest.REST_TICKS));
        assertTrue(ShabbatRest.rested(Long.MIN_VALUE / 2, 0));
    }

    @Test
    void shabbatFollowsTheFestivalCalendar() {
        FestivalManager.forceStartFestival(FestivalType.SHABBAT, 500);
        assertTrue(ShabbatRest.isShabbat(600));
        FestivalManager.forceStartFestival(FestivalType.PURIM, 500);
        assertFalse(ShabbatRest.isShabbat(600));
    }

    @Test
    void bonusIsShorterThanItsInterval() {
        assertTrue(ShabbatRest.REGEN_TICKS <= ShabbatRest.BONUS_EVERY);
        assertTrue(ShabbatRest.VILLAGER_SLOW_TICKS > 100);
    }
}
