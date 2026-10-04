package com.israelsimulator.agriculture;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VegetationAndAgricultureTest {

    @Test
    @DisplayName("Verify custom vegetation block classes are present and loadable")
    void testCustomBlockClassesPresent() throws Exception {
        ClassLoader cl = VegetationAndAgricultureTest.class.getClassLoader();
        Class<?> fruitingLeaves = Class.forName("com.israelsimulator.block.FruitingLeavesBlock", false, cl);
        assertNotNull(fruitingLeaves);

        Class<?> grapevine = Class.forName("com.israelsimulator.block.GrapevineBlock", false, cl);
        assertNotNull(grapevine);

        Class<?> herb = Class.forName("com.israelsimulator.block.MediterraneanHerbBlock", false, cl);
        assertNotNull(herb);
    }

    @Test
    @DisplayName("Verify agricultural commodities definitions and exchange rates")
    void testAgriculturalCommodities() {
        assertEquals(8, AgriculturalCommodity.OLIVE.itemsPerShekel());
        assertEquals(6, AgriculturalCommodity.DATE.itemsPerShekel());
        assertEquals(6, AgriculturalCommodity.CITRUS.itemsPerShekel());
        assertEquals(16, AgriculturalCommodity.WHEAT.itemsPerShekel());
        assertEquals(12, AgriculturalCommodity.CARROT.itemsPerShekel());
        assertEquals(12, AgriculturalCommodity.POTATO.itemsPerShekel());
        assertEquals(12, AgriculturalCommodity.BEETROOT.itemsPerShekel());
    }

    @Test
    @DisplayName("Verify agricultural economy trades calculation")
    void testAgriculturalEconomyCalculation() {
        int olivesAvailable = 16;
        int olivePerShekel = AgriculturalCommodity.OLIVE.itemsPerShekel();
        int shekelsEarned = olivesAvailable / olivePerShekel;
        int itemsConsumed = shekelsEarned * olivePerShekel;
        assertEquals(2, shekelsEarned);
        assertEquals(16, itemsConsumed);

        int partialOlives = 4;
        int agorotEarned = (partialOlives * 100) / olivePerShekel;
        assertEquals(50, agorotEarned);

        int datesAvailable = 9;
        int datePerShekel = AgriculturalCommodity.DATE.itemsPerShekel();
        int dateShekels = datesAvailable / datePerShekel;
        int dateConsumed = dateShekels * datePerShekel;
        assertEquals(1, dateShekels);
        assertEquals(6, dateConsumed);

        int partialDates = 3;
        int dateAgorot = (partialDates * 100) / datePerShekel;
        assertEquals(50, dateAgorot);

        assertTrue(AgriculturalEconomy.calculateTrade(null).isEmpty());
    }

    @Test
    @DisplayName("Verify block models and blockstate resource files exist")
    void testBlockResources() {
        String[] blocks = {
                "olive_leaves",
                "date_palm_leaves",
                "citrus_leaves",
                "grapevine",
                "mediterranean_herbs"
        };

        for (String block : blocks) {
            String bsPath = "/assets/israel_simulator/blockstates/" + block + ".json";
            assertNotNull(getClass().getResourceAsStream(bsPath), "Missing blockstate: " + bsPath);

            String lootPath = "/data/israel_simulator/loot_table/blocks/" + block + ".json";
            assertNotNull(getClass().getResourceAsStream(lootPath), "Missing loot table: " + lootPath);
        }
    }

    @Test
    @DisplayName("Verify block item model resources exist")
    void testBlockItemModels() {
        String[] blocks = {
                "olive_leaves",
                "date_palm_leaves",
                "citrus_leaves",
                "mediterranean_herbs"
        };

        for (String block : blocks) {
            String itemModelPath = "/assets/israel_simulator/models/item/" + block + ".json";
            assertNotNull(getClass().getResourceAsStream(itemModelPath), "Missing item model: " + itemModelPath);
        }
    }

    @Test
    @DisplayName("Verify vegetation worldgen configured features, placed features and biome modifiers exist")
    void testVegetationWorldGenResources() {
        String[] cfFiles = {"grapevine_patch", "mediterranean_herbs_patch"};
        for (String cf : cfFiles) {
            String cfPath = "/data/israel_simulator/worldgen/configured_feature/" + cf + ".json";
            assertNotNull(getClass().getResourceAsStream(cfPath), "Missing configured feature: " + cfPath);
        }

        String[] pfFiles = {"grapevine_patch_placed", "mediterranean_herbs_placed"};
        for (String pf : pfFiles) {
            String pfPath = "/data/israel_simulator/worldgen/placed_feature/" + pf + ".json";
            assertNotNull(getClass().getResourceAsStream(pfPath), "Missing placed feature: " + pfPath);
        }

        String[] modifiers = {"add_vineyards", "add_mediterranean_herbs"};
        for (String mod : modifiers) {
            String modPath = "/data/israel_simulator/neoforge/biome_modifier/" + mod + ".json";
            assertNotNull(getClass().getResourceAsStream(modPath), "Missing biome modifier: " + modPath);
        }
    }

    @Test
    @DisplayName("Verify block localization keys exist in en_us.json")
    void testBlockLocalization() throws Exception {
        InputStream is = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(is, "Missing lang/en_us.json");
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        String[] keys = {
                "block.israel_simulator.olive_leaves",
                "block.israel_simulator.date_palm_leaves",
                "block.israel_simulator.citrus_leaves",
                "block.israel_simulator.grapevine",
                "block.israel_simulator.mediterranean_herbs"
        };

        for (String key : keys) {
            assertTrue(content.contains(key), "Missing block translation: " + key);
        }
    }

    @Test
    @DisplayName("Verify Minecraft 1.21.4+ item model definitions exist in assets/items/")
    void testModernItemModelDefinitionsExist() {
        String[] items = {
                "olives",
                "dates",
                "citrus",
                "shekel",
                "agora",
                "olive_leaves",
                "date_palm_leaves",
                "citrus_leaves",
                "mediterranean_herbs"
        };

        for (String item : items) {
            String path = "/assets/israel_simulator/items/" + item + ".json";
            assertNotNull(getClass().getResourceAsStream(path), "Missing modern item model definition: " + path);
        }
    }
}
