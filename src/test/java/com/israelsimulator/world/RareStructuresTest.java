package com.israelsimulator.world;

import com.israelsimulator.world.structure.ModStructures;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RareStructuresTest {

    @Test
    @DisplayName("Verify rare structure keys and sets are registered in ModStructures")
    void testRareStructuresRegistryKeys() {
        assertNotNull(ModStructures.HISTORICAL_HOUSE);
        assertNotNull(ModStructures.GRAND_MARKET);
        assertNotNull(ModStructures.STARTUP_OFFICE);
        assertNotNull(ModStructures.GOVERNMENT_BUILDING);
        assertNotNull(ModStructures.ANCIENT_SANCTUARY);

        assertNotNull(ModStructures.HISTORICAL_HOUSES);
        assertNotNull(ModStructures.GRAND_MARKETS);
        assertNotNull(ModStructures.STARTUP_OFFICES);
        assertNotNull(ModStructures.GOVERNMENT_BUILDINGS);
        assertNotNull(ModStructures.ANCIENT_SANCTUARIES);

        assertEquals(16, ModStructures.allStructures().size(), "ModStructures must track 16 structures");
    }

    @Test
    @DisplayName("Verify rare structure JSON definitions exist in data pack")
    void testStructureJsonDefinitions() {
        List<String> structures = List.of(
                "historical_house", "grand_market", "startup_office", "government_building", "ancient_sanctuary"
        );

        for (String name : structures) {
            InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/worldgen/structure/" + name + ".json");
            assertNotNull(stream, "Missing structure JSON for " + name);
            try {
                String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(json.contains("minecraft:jigsaw"), "Structure " + name + " must be jigsaw type");
            } catch (Exception e) {
                fail("Failed reading structure JSON for " + name + ": " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Verify structure sets enforce valid spacing > separation rules for rarity")
    void testStructureSetPlacementRules() {
        List<String> sets = List.of(
                "historical_houses", "grand_markets", "startup_offices", "government_buildings", "ancient_sanctuaries"
        );

        for (String name : sets) {
            InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/worldgen/structure_set/" + name + ".json");
            assertNotNull(stream, "Missing structure set JSON for " + name);
            try {
                String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(json.contains("minecraft:random_spread"), "Structure set " + name + " must use random_spread");
                assertTrue(json.contains("spacing"), "Structure set " + name + " must specify spacing");
                assertTrue(json.contains("separation"), "Structure set " + name + " must specify separation");
            } catch (Exception e) {
                fail("Failed reading structure set JSON for " + name + ": " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Verify chest loot tables exist for all rare structures")
    void testLootTableFiles() {
        List<String> lootTables = List.of(
                "historical_house", "grand_market", "startup_office", "government_building", "ancient_sanctuary"
        );

        for (String name : lootTables) {
            InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/loot_table/chests/" + name + ".json");
            assertNotNull(stream, "Missing chest loot table for " + name);
            try {
                String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(json.contains("minecraft:chest"), "Loot table " + name + " must be chest type");
            } catch (Exception e) {
                fail("Failed reading loot table for " + name + ": " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Verify ultra-rare Rabbi's Crown loot source integration in rare sanctuary and synagogue ark")
    void testRabbisCrownLootIntegration() {
        // 1. Ancient sanctuary chest
        InputStream sanctuaryStream = getClass().getResourceAsStream("/data/israel_simulator/loot_table/chests/ancient_sanctuary.json");
        assertNotNull(sanctuaryStream, "ancient_sanctuary.json must exist");
        try {
            String json = new String(sanctuaryStream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:rabbis_crown"), "Ancient sanctuary must contain ultra-rare Rabbi's Crown");
        } catch (Exception e) {
            fail("Failed reading ancient_sanctuary.json: " + e.getMessage());
        }

        // 2. Synagogue ark chest
        InputStream arkStream = getClass().getResourceAsStream("/data/israel_simulator/loot_table/chests/synagogue_ark.json");
        assertNotNull(arkStream, "synagogue_ark.json must exist");
        try {
            String json = new String(arkStream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:rabbis_crown"), "Synagogue ark must contain ultra-rare Rabbi's Crown");
        } catch (Exception e) {
            fail("Failed reading synagogue_ark.json: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify rare structure translations exist in en_us.json")
    void testRareStructureTranslations() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"structure.israel_simulator.historical_house\""));
            assertTrue(json.contains("\"structure.israel_simulator.grand_market\""));
            assertTrue(json.contains("\"structure.israel_simulator.startup_office\""));
            assertTrue(json.contains("\"structure.israel_simulator.government_building\""));
            assertTrue(json.contains("\"structure.israel_simulator.ancient_sanctuary\""));
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }
}
