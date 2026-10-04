package com.israelsimulator.city.jaffa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JaffaEconomyTest {

    @Test
    @DisplayName("Verify historical landmarks of Jaffa are registered and documented")
    void testLandmarks() {
        assertNotNull(JaffaLandmarks.ALL);
        assertEquals(3, JaffaLandmarks.ALL.size(), "Jaffa should define 3 primary historical landmarks");

        assertTrue(JaffaLandmarks.ALL.contains(JaffaLandmarks.CLOCK_TOWER));
        assertTrue(JaffaLandmarks.ALL.contains(JaffaLandmarks.OLD_PORT));
        assertTrue(JaffaLandmarks.ALL.contains(JaffaLandmarks.FLEA_MARKET));
    }
}
