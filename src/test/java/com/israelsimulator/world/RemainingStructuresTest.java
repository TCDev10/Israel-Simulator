package com.israelsimulator.world;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemainingStructuresTest {

    private static final Map<String, String> EXPECTED_MARKERS = Map.of(
            "government_building", "minecraft:polished_deepslate",
            "startup_office", "minecraft:iron_block",
            "historical_house", "minecraft:bricks",
            "synagogue", "minecraft:purple_stained_glass",
            "ancient_sanctuary", "minecraft:chiseled_sandstone",
            "great_synagogue", "minecraft:blue_stained_glass"
    );

    private static final Map<String, String> EXPECTED_LOOT_TABLES = Map.of(
            "government_building", "israel_simulator:chests/government_building",
            "startup_office", "israel_simulator:chests/startup_office",
            "historical_house", "israel_simulator:chests/historical_house",
            "synagogue", "israel_simulator:chests/synagogue",
            "ancient_sanctuary", "israel_simulator:chests/ancient_sanctuary",
            "great_synagogue", "israel_simulator:chests/synagogue_ark"
    );

    @Test
    @DisplayName("Verify all 7 reconstructed structures exist as real NBT files with substantial block counts")
    void testReconstructedStructuresNbt() throws Exception {
        for (String name : EXPECTED_MARKERS.keySet()) {
            String path = "data/israel_simulator/structure/" + name + ".nbt";
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            assertNotNull(stream, "Missing structure NBT at: " + path);

            byte[] nbtBytes = new GZIPInputStream(stream).readAllBytes();
            String decoded = new String(nbtBytes, StandardCharsets.ISO_8859_1);

            assertTrue(decoded.contains("DataVersion"), name + " must contain DataVersion");
            assertTrue(decoded.contains("size"), name + " must define size");
            assertTrue(decoded.contains("palette"), name + " must define palette");
            assertTrue(decoded.contains("blocks"), name + " must define blocks");

            // Verify marker block
            String marker = EXPECTED_MARKERS.get(name);
            assertTrue(decoded.contains(marker), name + " must contain marker block " + marker);

            // Verify loot table reference
            String loot = EXPECTED_LOOT_TABLES.get(name);
            assertTrue(decoded.contains(loot), name + " must contain loot table reference " + loot);

            // Verify NBT is substantial (not a 7x7 placeholder box < 1000 bytes)
            assertTrue(nbtBytes.length > 5000, name + " must be a substantial real structure, was only " + nbtBytes.length + " bytes");
        }
    }

    @Test
    @DisplayName("Verify synagogue and great_synagogue both contain synagogue_ark loot table")
    void testSynagoguesContainArkLoot() throws Exception {
        for (String name : List.of("synagogue", "great_synagogue")) {
            String path = "data/israel_simulator/structure/" + name + ".nbt";
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            assertNotNull(stream);
            byte[] nbtBytes = new GZIPInputStream(stream).readAllBytes();
            String decoded = new String(nbtBytes, StandardCharsets.ISO_8859_1);
            assertTrue(decoded.contains("israel_simulator:chests/synagogue_ark"),
                    name + " must contain the Aron Kodesh ark loot chest");
        }
    }

    @Test
    @DisplayName("Verify ancient_sanctuary contains ancient_sanctuary loot table with Rabbi's Crown source")
    void testAncientSanctuaryCrownLoot() throws Exception {
        String path = "data/israel_simulator/structure/ancient_sanctuary.nbt";
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream);
        byte[] nbtBytes = new GZIPInputStream(stream).readAllBytes();
        String decoded = new String(nbtBytes, StandardCharsets.ISO_8859_1);
        assertTrue(decoded.contains("israel_simulator:chests/ancient_sanctuary"),
                "ancient_sanctuary must contain ancient_sanctuary loot table");
    }
}
