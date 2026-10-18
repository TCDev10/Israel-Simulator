package com.israelsimulator.entity.npc;

import static org.junit.jupiter.api.Assertions.*;

import com.israelsimulator.entity.npc.shuk.ShukMarket;
import com.israelsimulator.entity.npc.shuk.ShukMarket.Listing;
import com.israelsimulator.entity.npc.shuk.ShukMarket.Period;
import com.israelsimulator.entity.npc.shuk.ShukMarket.Stall;
import com.israelsimulator.festival.FestivalManager;
import com.israelsimulator.festival.FestivalType;
import com.israelsimulator.registry.ModItems;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ShukMarketTest {
    /** Day 50 of the 120-day year, start of the week: no festival, no Shabbat. */
    private static final long PLAIN_DAY = 50L * 24000L;
    /** Day 54, week tick 140000: Friday night, Shabbat. */
    private static final long SHABBAT = 7L * 168000L + 140000L;

    @AfterEach
    void clear() {
        FestivalManager.clearForcedFestival();
    }

    @Test
    void periodsFollowTheDay() {
        assertEquals(Period.MORNING, ShukMarket.periodAt(1000, PLAIN_DAY));
        assertEquals(Period.AFTERNOON, ShukMarket.periodAt(6000, PLAIN_DAY));
        assertEquals(Period.CLOSING, ShukMarket.periodAt(11500, PLAIN_DAY));
        assertEquals(Period.CLOSED, ShukMarket.periodAt(18000, PLAIN_DAY));
        assertEquals(Period.MORNING, ShukMarket.periodAt(24000 + 100, PLAIN_DAY));
    }

    @Test
    void shabbatAndYomKippurCloseTheMarket() {
        assertEquals(FestivalType.SHABBAT, FestivalManager.getCurrentFestival(SHABBAT));
        assertEquals(Period.CLOSED, ShukMarket.periodAt(3000, SHABBAT));
        assertEquals(Period.CLOSED, ShukMarket.periodAt(3000, 10L * 24000L)); // Yom Kippur (day 10)
        assertTrue(ShukMarket.listings(Stall.BAKERY, Period.CLOSED, null).isEmpty());
    }

    @Test
    void closingSaleGivesMoreForTheSamePrice() {
        for (Stall s : Stall.values()) {
            List<Listing> day = ShukMarket.listings(s, Period.AFTERNOON, null);
            List<Listing> sale = ShukMarket.listings(s, Period.CLOSING, null);
            assertFalse(day.isEmpty(), s.name());
            assertEquals(day.size(), sale.size());
            for (int i = 0; i < day.size(); i++) {
                assertEquals(day.get(i).shekels(), sale.get(i).shekels());
                assertTrue(sale.get(i).count() > day.get(i).count(), s + " " + i);
            }
        }
    }

    @Test
    void morningProduceIsBigger() {
        assertTrue(ShukMarket.listings(Stall.FRUIT, Period.MORNING, null).get(0).count()
                > ShukMarket.listings(Stall.FRUIT, Period.AFTERNOON, null).get(0).count());
        assertEquals(ShukMarket.listings(Stall.SPICE, Period.MORNING, null).get(0).count(),
                ShukMarket.listings(Stall.SPICE, Period.AFTERNOON, null).get(0).count());
    }

    @Test
    void festivalsChangeTheBakery() {
        assertSame(ModItems.SUFGANIYAH, ShukMarket.listings(Stall.BAKERY, Period.AFTERNOON, FestivalType.HANUKKAH).get(0).item());
        assertSame(ModItems.HAMANTASH, ShukMarket.listings(Stall.BAKERY, Period.AFTERNOON, FestivalType.PURIM).get(0).item());
        List<Listing> pesach = ShukMarket.listings(Stall.BAKERY, Period.AFTERNOON, FestivalType.PESACH);
        assertSame(ModItems.MATZO, pesach.get(0).item());
        assertTrue(pesach.stream().noneMatch(l -> l.item() == ModItems.CHALLAH || l.item() == ModItems.RUGELACH));
        assertTrue(ShukMarket.listings(Stall.BAKERY, Period.AFTERNOON, null).stream().anyMatch(l -> l.item() == ModItems.CHALLAH));
    }

    @Test
    void stockFestivalUsesCalendarAndForcedFestival() {
        assertEquals(FestivalType.HANUKKAH, ShukMarket.stockFestival(36L * 24000L));
        assertNull(ShukMarket.stockFestival(SHABBAT));
        FestivalManager.forceStartFestival(FestivalType.PURIM, PLAIN_DAY);
        assertEquals(FestivalType.PURIM, ShukMarket.stockFestival(PLAIN_DAY + 10));
        assertNotEquals(ShukMarket.stockKey(Period.MORNING, null), ShukMarket.stockKey(Period.MORNING, FestivalType.PURIM));
    }
}
