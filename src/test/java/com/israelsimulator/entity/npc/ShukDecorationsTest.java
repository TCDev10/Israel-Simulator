package com.israelsimulator.entity.npc;

import static org.junit.jupiter.api.Assertions.*;

import com.israelsimulator.entity.npc.shuk.ShukDecorations;
import com.israelsimulator.festival.FestivalType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShukDecorationsTest {

    @Test
    void eachFestivalHasItsDecoration() {
        assertNull(ShukDecorations.forFestival(null, 0));
        assertNull(ShukDecorations.forFestival(FestivalType.YOM_KIPPUR, 0));
        for (FestivalType f : FestivalType.values()) {
            if (f == FestivalType.YOM_KIPPUR) continue;
            assertNotNull(ShukDecorations.forFestival(f, 1), f.name());
        }
        assertEquals("israel_simulator:menorah", ShukDecorations.forFestival(FestivalType.HANUKKAH, 3).blockId());
        assertEquals(3, ShukDecorations.forFestival(FestivalType.HANUKKAH, 3).candles());
        assertEquals(1, ShukDecorations.forFestival(FestivalType.HANUKKAH, 0).candles());
        assertEquals(8, ShukDecorations.forFestival(FestivalType.HANUKKAH, 12).candles());
        assertEquals(2, ShukDecorations.forFestival(FestivalType.SHABBAT, 0).candles());
        assertTrue(ShukDecorations.forFestival(FestivalType.SHABBAT, 0).lit());
    }

    @Test
    void offsetsAreOnTheCounterAndNeverOnTheVendor() {
        List<int[]> offs = ShukDecorations.candidateOffsets();
        assertEquals(24, offs.size());
        Set<String> seen = new HashSet<>();
        for (int[] o : offs) {
            assertEquals(1, o[1]);
            assertFalse(o[0] == 0 && o[2] == 0);
            assertTrue(seen.add(o[0] + "," + o[2]));
        }
        int[] first = offs.get(0);
        assertTrue(Math.abs(first[0]) <= 1 && Math.abs(first[2]) <= 1);
    }
}
