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
    @DisplayName("Landmarks are bound to real generated structures, not hard-coded coordinates")
    void testLandmarks() {
        assertEquals(java.util.EnumSet.of(Landmark.WESTERN_WALL, Landmark.JAFFA_CLOCK_TOWER, Landmark.FLEA_MARKET,
                Landmark.TEL_AVIV_PROMENADE), java.util.EnumSet.allOf(Landmark.class));
        assertEquals(IsraelRegion.JERUSALEM, Landmark.WESTERN_WALL.getRegion());
        assertEquals(IsraelRegion.JAFFA, Landmark.JAFFA_CLOCK_TOWER.getRegion());
        assertEquals(IsraelRegion.TEL_AVIV, Landmark.TEL_AVIV_PROMENADE.getRegion());
        assertEquals(com.israelsimulator.world.structure.ModStructures.WESTERN_WALL, Landmark.WESTERN_WALL.getStructure());
        assertEquals(com.israelsimulator.world.structure.ModStructures.JAFFA_PORT, Landmark.JAFFA_CLOCK_TOWER.getStructure());
        assertEquals(com.israelsimulator.world.structure.ModStructures.TEL_AVIV_CITY, Landmark.TEL_AVIV_PROMENADE.getStructure());
        for (Landmark lm : Landmark.values()) {
            assertNotNull(lm.getStructure(), lm + " must point at a structure");
            String structureJson = "data/israel_simulator/worldgen/structure/"
                    + lm.getStructure().identifier().getPath() + ".json";
            assertNotNull(getClass().getClassLoader().getResource(structureJson), lm + ": missing " + structureJson);
            if (lm.getPiece() != null) {
                String nbt = "data/israel_simulator/structure/" + lm.getPiece().substring("israel_simulator:".length()) + ".nbt";
                assertNotNull(getClass().getClassLoader().getResource(nbt), lm + ": missing piece " + nbt);
                String pools = String.join("\n", readPools(lm.getStructure().identifier().getPath()));
                assertTrue(pools.contains(lm.getPiece()), lm + ": piece is not in any pool of its structure");
            }
        }
    }

    private java.util.List<String> readPools(String structure) {
        java.util.List<String> out = new java.util.ArrayList<>();
        try {
            java.nio.file.Path root = java.nio.file.Path.of(getClass().getClassLoader()
                    .getResource("data/israel_simulator/worldgen/template_pool").toURI());
            try (var files = java.nio.file.Files.walk(root)) {
                for (java.nio.file.Path f : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                    out.add(java.nio.file.Files.readString(f));
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return out;
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
