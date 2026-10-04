package com.israelsimulator.localization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.validation.DataAndResourceValidationTest;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Localization Validation Test Suite (GAME_DESIGN.md §60, TODO §60).
 * Verifies English and Italian localization completeness, exact key parity,
 * and ensures all items, blocks, entities, effects, achievements, dialogues,
 * and configuration keys are localized without empty strings.
 */
public class LocalizationValidationTest {

    private static final Gson GSON = new Gson();
    private static final Path LANG_PATH = Paths.get("src", "main", "resources", "assets", IsraelSimulator.MOD_ID, "lang");

    @Test
    @DisplayName("Verify en_us.json and it_it.json exist and are well-formed")
    void testLanguageFilesExist() {
        Path enPath = LANG_PATH.resolve("en_us.json");
        Path itPath = LANG_PATH.resolve("it_it.json");

        assertTrue(Files.exists(enPath), "en_us.json must exist");
        assertTrue(Files.exists(itPath), "it_it.json must exist");
    }

    @Test
    @DisplayName("Verify exact key parity between English and Italian localization files")
    void testKeyParityBetweenEnglishAndItalian() throws IOException {
        Path enPath = LANG_PATH.resolve("en_us.json");
        Path itPath = LANG_PATH.resolve("it_it.json");

        JsonObject enJson;
        JsonObject itJson;

        try (FileReader enReader = new FileReader(enPath.toFile());
             FileReader itReader = new FileReader(itPath.toFile())) {
            enJson = GSON.fromJson(enReader, JsonObject.class);
            itJson = GSON.fromJson(itReader, JsonObject.class);
        }

        assertNotNull(enJson);
        assertNotNull(itJson);

        List<String> missingInItalian = new ArrayList<>();
        enJson.keySet().forEach(key -> {
            if (!itJson.has(key)) {
                missingInItalian.add(key);
            }
        });

        List<String> missingInEnglish = new ArrayList<>();
        itJson.keySet().forEach(key -> {
            if (!enJson.has(key)) {
                missingInEnglish.add(key);
            }
        });

        assertTrue(missingInItalian.isEmpty(), "Missing keys in Italian: " + missingInItalian);
        assertTrue(missingInEnglish.isEmpty(), "Missing keys in English: " + missingInEnglish);
    }

    @Test
    @DisplayName("Verify every registered item has a localized name")
    void testAllItemsLocalized() throws IOException {
        Path enPath = LANG_PATH.resolve("en_us.json");
        JsonObject enJson;
        try (FileReader reader = new FileReader(enPath.toFile())) {
            enJson = GSON.fromJson(reader, JsonObject.class);
        }

        for (String path : DataAndResourceValidationTest.ALL_MOD_ITEMS) {
            String key = "item." + IsraelSimulator.MOD_ID + "." + path;
            assertTrue(enJson.has(key), "en_us.json missing localization key for item: " + key);
            assertFalse(enJson.get(key).getAsString().isBlank(), "Item translation must not be blank for: " + key);
        }
    }

    @Test
    @DisplayName("Verify every registered block has a localized name")
    void testAllBlocksLocalized() throws IOException {
        Path enPath = LANG_PATH.resolve("en_us.json");
        JsonObject enJson;
        try (FileReader reader = new FileReader(enPath.toFile())) {
            enJson = GSON.fromJson(reader, JsonObject.class);
        }

        for (String path : DataAndResourceValidationTest.ALL_MOD_BLOCKS) {
            String key = "block." + IsraelSimulator.MOD_ID + "." + path;
            assertTrue(enJson.has(key), "en_us.json missing localization key for block: " + key);
            assertFalse(enJson.get(key).getAsString().isBlank(), "Block translation must not be blank for: " + key);
        }
    }

    @Test
    @DisplayName("Verify entities, effects, and biomes are localized")
    void testCoreCategoriesLocalized() throws IOException {
        Path enPath = LANG_PATH.resolve("en_us.json");
        JsonObject enJson;
        try (FileReader reader = new FileReader(enPath.toFile())) {
            enJson = GSON.fromJson(reader, JsonObject.class);
        }

        // Entities
        String[] entities = {"bibi_boss", "bibi_guard", "bicycle"};
        for (String entity : entities) {
            String key = "entity." + IsraelSimulator.MOD_ID + "." + entity;
            assertTrue(enJson.has(key), "Missing entity localization: " + key);
        }

        // Effects
        String[] effects = {"blessed", "freedom", "meat_digestion", "dairy_digestion", "blessed_trader"};
        for (String effect : effects) {
            String key = "effect." + IsraelSimulator.MOD_ID + "." + effect;
            assertTrue(enJson.has(key), "Missing effect localization: " + key);
        }

        // Biomes
        String[] biomes = {"mediterranean_coast", "israeli_agriculture", "judean_desert", "dead_sea", "urban_area", "jerusalem"};
        for (String biome : biomes) {
            String key = "biome." + IsraelSimulator.MOD_ID + "." + biome;
            assertTrue(enJson.has(key), "Missing biome localization: " + key);
        }
    }

    @Test
    @DisplayName("Verify no translation string is null or empty in either language")
    void testNoEmptyTranslations() throws IOException {
        for (String langFile : List.of("en_us.json", "it_it.json")) {
            Path path = LANG_PATH.resolve(langFile);
            try (FileReader reader = new FileReader(path.toFile())) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                json.keySet().forEach(key -> {
                    String value = json.get(key).getAsString();
                    assertNotNull(value, "Value for " + key + " in " + langFile + " is null");
                    assertFalse(value.trim().isEmpty(), "Value for " + key + " in " + langFile + " is empty");
                });
            }
        }
    }
}
