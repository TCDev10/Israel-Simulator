package com.israelsimulator.agriculture;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Grapevine yield is grapes. Dates stay the date-palm fruit.
 */
class GrapevineHarvestTest {

    @Test
    @DisplayName("Grapevine seed and right-click harvest drop grapes, not dates or wheat seeds")
    void testGrapevineHarvestDropsGrapes() throws Exception {
        String block = Files.readString(Path.of("src/main/java/com/israelsimulator/block/GrapevineBlock.java"));
        String seed = methodSource(block, "protected ItemLike getBaseSeedId()");
        String harvest = methodSource(block, "protected InteractionResult useWithoutItem(");

        assertTrue(seed.contains("ModItems.GRAPES"), "the vine must be planted with grapes");
        assertTrue(harvest.contains("ModItems.GRAPES"), "a ripe vine must drop grapes");
        assertFalse(seed.contains("DATES") || seed.contains("wheat_seeds"), seed);
        assertFalse(harvest.contains("DATES") || harvest.contains("wheat_seeds"), harvest);
    }

    @Test
    @DisplayName("Grapevine loot drops grapes at every age, never dates or wheat seeds")
    void testGrapevineLootDropsGrapes() throws Exception {
        String loot = Files.readString(Path.of(
                "src/main/resources/data/israel_simulator/loot_table/blocks/grapevine.json"));
        assertTrue(loot.contains("israel_simulator:grapes"), loot);
        assertFalse(loot.contains("dates"), loot);
        assertFalse(loot.contains("wheat_seeds"), loot);
    }

    @Test
    @DisplayName("Dates remain their own item and still drop from the date palm")
    void testDatesStayOnThePalm() throws Exception {
        String items = Files.readString(Path.of("src/main/java/com/israelsimulator/registry/ModItems.java"));
        assertTrue(items.contains("registerSimpleItem(\"dates\""));
        assertTrue(items.contains("registerSimpleItem(\"grapes\""));

        String palm = Files.readString(Path.of(
                "src/main/resources/data/israel_simulator/loot_table/blocks/date_palm_leaves.json"));
        assertTrue(palm.contains("israel_simulator:dates"));
        assertFalse(palm.contains("israel_simulator:grapes"));
    }

    private static String methodSource(String source, String signature) {
        int start = source.indexOf(signature);
        assertTrue(start >= 0, "missing " + signature);
        int next = source.indexOf("\n    @Override", start + signature.length());
        if (next < 0) {
            next = source.indexOf("\n    private ", start + signature.length());
        }
        if (next < 0) {
            next = source.length();
        }
        return source.substring(start, next);
    }
}
