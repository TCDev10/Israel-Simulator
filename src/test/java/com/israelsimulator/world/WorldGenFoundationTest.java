package com.israelsimulator.world;

import com.israelsimulator.world.biome.ModBiomes;
import com.israelsimulator.world.feature.ModConfiguredFeatures;
import com.israelsimulator.world.feature.ModPlacedFeatures;
import com.israelsimulator.world.structure.ModStructures;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.registries.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldGenFoundationTest {

    @Test
    @DisplayName("Verify custom biome registry keys and count")
    void testCustomBiomes() {
        assertEquals(6, ModBiomes.all().size());
        assertTrue(ModBiomes.all().contains(ModBiomes.MEDITERRANEAN_COAST));
        assertTrue(ModBiomes.all().contains(ModBiomes.ISRAELI_AGRICULTURE));
        assertTrue(ModBiomes.all().contains(ModBiomes.JUDEAN_DESERT));
        assertTrue(ModBiomes.all().contains(ModBiomes.DEAD_SEA));
        assertTrue(ModBiomes.all().contains(ModBiomes.URBAN_AREA));
        assertTrue(ModBiomes.all().contains(ModBiomes.JERUSALEM));

        for (var key : ModBiomes.all()) {
            assertEquals(Registries.BIOME, key.registryKey());
            assertEquals("israel_simulator", key.identifier().getNamespace());
            assertFalse(key.identifier().getPath().isBlank());
        }
    }

    @Test
    @DisplayName("Verify configured features keys")
    void testConfiguredFeatures() {
        assertEquals(8, ModConfiguredFeatures.all().size());
        for (var key : ModConfiguredFeatures.all()) {
            assertEquals(Registries.CONFIGURED_FEATURE, key.registryKey());
            assertEquals("israel_simulator", key.identifier().getNamespace());
        }
    }

    @Test
    @DisplayName("Verify placed features keys")
    void testPlacedFeatures() {
        assertEquals(8, ModPlacedFeatures.all().size());
        for (var key : ModPlacedFeatures.all()) {
            assertEquals(Registries.PLACED_FEATURE, key.registryKey());
            assertEquals("israel_simulator", key.identifier().getNamespace());
        }
    }

    @Test
    @DisplayName("Verify structure and structure set keys")
    void testStructures() {
        assertEquals(16, ModStructures.allStructures().size());
        for (var key : ModStructures.allStructures()) {
            assertEquals(Registries.STRUCTURE, key.registryKey());
            assertEquals("israel_simulator", key.identifier().getNamespace());
        }

        assertNotNull(ModStructures.MEDITERRANEAN_VILLAGES);
        assertEquals(Registries.STRUCTURE_SET, ModStructures.MEDITERRANEAN_VILLAGES.registryKey());

        assertNotNull(ModStructures.ANCIENT_RUINS_WEATHERING);
        assertEquals(Registries.PROCESSOR_LIST, ModStructures.ANCIENT_RUINS_WEATHERING.registryKey());
    }

    @Test
    @DisplayName("Verify worldgen biome JSON resources exist and are valid")
    void testBiomeJsonResources() {
        String[] biomes = {
                "mediterranean_coast",
                "israeli_agriculture",
                "judean_desert",
                "dead_sea",
                "urban_area",
                "jerusalem"
        };

        for (String biome : biomes) {
            String path = "/data/israel_simulator/worldgen/biome/" + biome + ".json";
            InputStream is = getClass().getResourceAsStream(path);
            assertNotNull(is, "Missing biome resource: " + path);
        }
    }

    @Test
    @DisplayName("Verify localization keys exist for all biomes in en_us.json")
    void testBiomeLocalization() throws Exception {
        InputStream is = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(is, "Missing lang/en_us.json");
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        for (var key : ModBiomes.all()) {
            String expected = "biome." + key.identifier().getNamespace() + "." + key.identifier().getPath();
            assertTrue(content.contains(expected), "Missing localization entry: " + expected);
        }
    }
}
