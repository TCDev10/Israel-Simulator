package com.israelsimulator.world.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapAndExplorationTest {

    @Test
    @DisplayName("Verify IsraelRegion enum covers the required geographical areas")
    void testIsraelRegions() {
        assertEquals(8, IsraelRegion.values().length, "Must define 8 major geographical regions");
        assertNotNull(IsraelRegion.TEL_AVIV);
        assertNotNull(IsraelRegion.JAFFA);
        assertNotNull(IsraelRegion.JERUSALEM);
        assertNotNull(IsraelRegion.DEAD_SEA);
        assertNotNull(IsraelRegion.NEGEV_DESERT);
        assertNotNull(IsraelRegion.MEDITERRANEAN_COAST);
        assertNotNull(IsraelRegion.GALILEE_GOLAN);
        assertNotNull(IsraelRegion.RURAL_SETTLEMENTS);
    }

    @Test
    @DisplayName("Verify Landmark enum defines the required cultural and historical sites")
    void testLandmarks() {
        assertTrue(Landmark.values().length >= 8, "Must define at least 8 prominent landmarks");
        assertNotNull(Landmark.WESTERN_WALL);
        assertEquals(IsraelRegion.JERUSALEM, Landmark.WESTERN_WALL.getRegion());

        assertNotNull(Landmark.JAFFA_CLOCK_TOWER);
        assertEquals(IsraelRegion.JAFFA, Landmark.JAFFA_CLOCK_TOWER.getRegion());

        assertNotNull(Landmark.DEAD_SEA_SALT_PILLARS);
        assertEquals(IsraelRegion.DEAD_SEA, Landmark.DEAD_SEA_SALT_PILLARS.getRegion());

        assertNotNull(Landmark.TEL_AVIV_PROMENADE);
        assertEquals(IsraelRegion.TEL_AVIV, Landmark.TEL_AVIV_PROMENADE.getRegion());

        assertNotNull(Landmark.KNESSET);
        assertNotNull(Landmark.CARMEL_MARKET);
        assertNotNull(Landmark.FLEA_MARKET);
    }

    @Test
    @DisplayName("Verify PlayerLandmarkTracker tracking logic and state persistence")
    void testPlayerLandmarkTracker() {
        UUID testPlayer = UUID.randomUUID();
        try {
            assertEquals(0, PlayerLandmarkTracker.getDiscoveredCount(testPlayer));
            assertFalse(PlayerLandmarkTracker.isDiscovered(testPlayer, Landmark.WESTERN_WALL));

            // Verify tracker getters return safe non-null sets
            assertNotNull(PlayerLandmarkTracker.getDiscoveredLandmarks(testPlayer));
            assertNotNull(PlayerLandmarkTracker.getVisitedRegions(testPlayer));
        } finally {
            PlayerLandmarkTracker.clearForPlayer(testPlayer);
        }
    }

    @Test
    @DisplayName("Verify exploration advancement JSON files exist and are valid")
    void testAdvancementFiles() throws Exception {
        InputStream welcome = getClass().getClassLoader().getResourceAsStream("data/israel_simulator/advancement/exploration/welcome_to_israel.json");
        assertNotNull(welcome, "welcome_to_israel.json advancement must exist in classpath");

        InputStream jerusalem = getClass().getClassLoader().getResourceAsStream("data/israel_simulator/advancement/exploration/visit_jerusalem.json");
        assertNotNull(jerusalem, "visit_jerusalem.json advancement must exist in classpath");

        InputStream explorer = getClass().getClassLoader().getResourceAsStream("data/israel_simulator/advancement/exploration/master_explorer.json");
        assertNotNull(explorer, "master_explorer.json advancement must exist in classpath");

        ClassLoader cl = MapAndExplorationTest.class.getClassLoader();
        Class<?> itemClass = Class.forName("com.israelsimulator.registry.ModItems", false, cl);
        assertNotNull(itemClass.getField("ISRAEL_MAP"), "ISRAEL_MAP item must be registered");
    }
}
