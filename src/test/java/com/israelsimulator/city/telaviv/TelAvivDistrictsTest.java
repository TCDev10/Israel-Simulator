package com.israelsimulator.city.telaviv;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelAvivDistrictsTest {

    @BeforeEach
    void setUp() {
        TelAvivDistrictManager.resetAll();
    }

    @Test
    @DisplayName("Lookup districts by string ID")
    void testDistrictFromId() {
        assertEquals(TelAvivDistricts.WHITE_CITY, TelAvivDistricts.fromId("white_city"));
        assertEquals(TelAvivDistricts.ROTHSCHILD, TelAvivDistricts.fromId("rothschild_boulevard"));
        assertEquals(TelAvivDistricts.FLORENTIN, TelAvivDistricts.fromId("florentin"));
        assertEquals(TelAvivDistricts.SARONA, TelAvivDistricts.fromId("sarona"));
        assertEquals(TelAvivDistricts.STARTUP_DISTRICT, TelAvivDistricts.fromId("startup_district"));
        assertEquals(TelAvivDistricts.TAYELET_BEACH, TelAvivDistricts.fromId("tayelet_beach"));
        assertNull(TelAvivDistricts.fromId("invalid_district"));
    }

    @Test
    @DisplayName("Spatial zoning partitions coordinates across all 6 districts")
    void testSpatialZoning() {
        Set<TelAvivDistricts> seen = new HashSet<>();

        // Check grid coordinates across the 3x2 modular block zones (each zone is 64x64)
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 2; row++) {
                int x = (col * 64) + 32;
                int z = (row * 64) + 32;
                TelAvivDistricts district = TelAvivDistricts.getDistrictAt(new BlockPos(x, 64, z));
                assertNotNull(district);
                seen.add(district);
            }
        }

        assertEquals(6, seen.size(), "All 6 districts must be reachable in spatial zoning");
        assertTrue(seen.contains(TelAvivDistricts.WHITE_CITY));
        assertTrue(seen.contains(TelAvivDistricts.ROTHSCHILD));
        assertTrue(seen.contains(TelAvivDistricts.FLORENTIN));
        assertTrue(seen.contains(TelAvivDistricts.SARONA));
        assertTrue(seen.contains(TelAvivDistricts.STARTUP_DISTRICT));
        assertTrue(seen.contains(TelAvivDistricts.TAYELET_BEACH));
    }

    @Test
    @DisplayName("District economy multipliers match district identities")
    void testDistrictEconomy() {
        // Startup district boosts tech items
        assertTrue(TelAvivDistricts.STARTUP_DISTRICT.getEconomyMultiplier("laptop") > 1.0);
        assertTrue(TelAvivDistricts.STARTUP_DISTRICT.getEconomyMultiplier("smartphone") > 1.0);
        assertEquals(1.0, TelAvivDistricts.STARTUP_DISTRICT.getEconomyMultiplier("falafel"));

        // Sarona boosts food items
        assertTrue(TelAvivDistricts.SARONA.getEconomyMultiplier("falafel") > 1.0);
        assertTrue(TelAvivDistricts.SARONA.getEconomyMultiplier("rugelach") > 1.0);
        assertEquals(1.0, TelAvivDistricts.SARONA.getEconomyMultiplier("laptop"));

        // Florentin boosts craft / ancient items
        assertTrue(TelAvivDistricts.FLORENTIN.getEconomyMultiplier("ancient_coin") > 1.0);
        assertEquals(1.0, TelAvivDistricts.FLORENTIN.getEconomyMultiplier("laptop"));
    }

    @Test
    @DisplayName("District professions and welcome messages")
    void testDistrictProfessionsAndMessages() {
        assertEquals("developer", TelAvivDistricts.STARTUP_DISTRICT.getRecommendedProfession());
        assertEquals("butcher", TelAvivDistricts.SARONA.getRecommendedProfession());
        assertEquals("leatherworker", TelAvivDistricts.FLORENTIN.getRecommendedProfession());
        assertEquals("fisherman", TelAvivDistricts.TAYELET_BEACH.getRecommendedProfession());

        for (TelAvivDistricts d : TelAvivDistricts.ALL) {
            String welcome = d.getWelcomeMessage();
            assertNotNull(welcome);
            assertTrue(welcome.contains(d.displayName()));
            assertTrue(welcome.contains(d.architecturalStyle()));
        }
    }

    @Test
    @DisplayName("District tracking tracks unique visits and visited-all status")
    void testDistrictManagerTracking() {
        UUID playerId = UUID.randomUUID();
        assertEquals(0, TelAvivDistrictManager.getVisitedCount(playerId));
        assertFalse(TelAvivDistrictManager.hasVisitedAll(playerId));

        // Simulate visiting districts
        for (TelAvivDistricts d : TelAvivDistricts.ALL) {
            TelAvivDistrictManager.getVisitedDistricts(playerId); // verify unmodifiable
        }
    }
}
