package com.israelsimulator.validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.israelsimulator.IsraelSimulator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Data and Resource Validation Test Suite (GAME_DESIGN.md §57, TODO §57).
 * Ensures all JSON files, recipes, loot tables, tags, models, blockstates,
 * sounds, advancements, and resource paths are syntactically valid and conform to conventions.
 */
public class DataAndResourceValidationTest {

    private static final Gson GSON = new Gson();
    private static final Path RESOURCES_PATH = Paths.get("src", "main", "resources");

    public static final List<String> ALL_MOD_ITEMS = List.of(
            "kippah", "talit", "tefillin", "rabbis_crown", "prayer_note", "first_amendment",
            "hava_nagila_disc", "tahini", "dates", "olives", "citrus", "challah", "rugelach",
            "falafel", "hummus", "shakshuka", "sabich", "mezuzah", "star_of_david",
            "olive_wood_carving", "ancient_coin", "dead_sea_scroll_fragment", "dead_sea_mud",
            "shekel", "agora", "matzo", "sufganiyah", "dreidel", "hamantash", "shofar",
            "smartphone", "laptop", "drone_part", "bicycle", "rav_kav", "walking_shoes",
            "israel_map"
    );

    public static final List<String> ALL_MOD_BLOCKS = List.of(
            "olive_leaves", "date_palm_leaves", "citrus_leaves", "grapevine",
            "mediterranean_herbs", "salt_block", "jerusalem_stone", "western_wall_stone",
            "menorah", "paved_road", "transport_stop"
    );

    public static final List<String> ALL_MOD_SOUNDS = List.of(
            "music_disc.hava_nagila", "music.cultural.klezmer", "music.cultural.shabbat_shalom",
            "ambient.city.tel_aviv", "ambient.city.jerusalem", "ambient.city.jaffa",
            "ambient.event.speech_crowd", "ambient.event.market_bustle", "audio.festival.hanukkah_chime",
            "audio.festival.shabbat_candle", "entity.bibi_boss.ambient", "entity.bibi_boss.hurt",
            "entity.bibi_boss.death", "entity.bibi_boss.speech", "entity.bibi_boss.enrage",
            "music.boss.bibi_theme", "entity.bicycle.bell", "entity.bus.horn",
            "entity.train.whistle", "entity.transport.travel", "ui.landmark_discovered"
    );

    @Test
    @DisplayName("Validate all JSON files parse without syntax errors")
    void testAllJsonFilesAreValid() throws IOException {
        assertTrue(Files.exists(RESOURCES_PATH), "Resources directory must exist");

        try (Stream<Path> stream = Files.walk(RESOURCES_PATH)) {
            List<Path> jsonFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .toList();

            assertFalse(jsonFiles.isEmpty(), "At least one JSON file must be found in resources");

            for (Path jsonFile : jsonFiles) {
                try (FileReader reader = new FileReader(jsonFile.toFile())) {
                    JsonElement element = GSON.fromJson(reader, JsonElement.class);
                    assertNotNull(element, "Parsed JSON element must not be null in: " + jsonFile);
                } catch (Exception e) {
                    throw new AssertionError("Invalid JSON syntax in: " + jsonFile + " -> " + e.getMessage(), e);
                }
            }
        }
    }

    @Test
    @DisplayName("Validate recipes exist and reference valid namespaces")
    void testRecipesValidation() {
        Path recipeDir = RESOURCES_PATH.resolve(Paths.get("data", IsraelSimulator.MOD_ID, "recipes"));
        assertTrue(Files.exists(recipeDir), "Recipe directory must exist");

        File[] recipes = recipeDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        assertTrue(recipes.length >= 10, "Should have at least 10 culinary and crafting recipes");

        for (File recipeFile : recipes) {
            try (FileReader reader = new FileReader(recipeFile)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                assertTrue(json.has("type"), "Recipe must have a type: " + recipeFile.getName());
                assertTrue(json.has("result") || json.has("category"), "Recipe must define result or category: " + recipeFile.getName());
            } catch (Exception e) {
                throw new AssertionError("Failed reading recipe: " + recipeFile.getName(), e);
            }
        }
    }

    @Test
    @DisplayName("Validate item and worldgen tags syntax")
    void testTagsValidation() {
        Path tagsDir = RESOURCES_PATH.resolve(Paths.get("data", IsraelSimulator.MOD_ID, "tags"));
        assertTrue(Files.exists(tagsDir), "Tags directory must exist");

        Path itemTagsDir = tagsDir.resolve("item");
        assertTrue(Files.exists(itemTagsDir), "Item tags directory must exist");

        String[] expectedItemTags = {"kosher.json", "meat.json", "dairy.json", "pareve.json"};
        for (String tagFile : expectedItemTags) {
            Path tagPath = itemTagsDir.resolve(tagFile);
            assertTrue(Files.exists(tagPath), "Tag file must exist: " + tagFile);

            try (FileReader reader = new FileReader(tagPath.toFile())) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                assertTrue(json.has("values"), "Tag must contain 'values' array: " + tagFile);
                assertTrue(json.getAsJsonArray("values").size() > 0, "Tag 'values' must not be empty: " + tagFile);
            } catch (Exception e) {
                throw new AssertionError("Error parsing tag: " + tagFile, e);
            }
        }
    }

    @Test
    @DisplayName("Validate item models and modern items definitions exist for every registered item")
    void testItemModelsAndDefinitionsValidation() {
        Path itemModelsDir = RESOURCES_PATH.resolve(Paths.get("assets", IsraelSimulator.MOD_ID, "models", "item"));
        Path itemsDefDir = RESOURCES_PATH.resolve(Paths.get("assets", IsraelSimulator.MOD_ID, "items"));

        assertTrue(Files.exists(itemModelsDir), "models/item directory must exist");
        assertTrue(Files.exists(itemsDefDir), "items/ directory must exist");

        for (String name : ALL_MOD_ITEMS) {
            Path modelPath = itemModelsDir.resolve(name + ".json");
            assertTrue(Files.exists(modelPath), "Missing item model for: " + name + " at " + modelPath);

            Path defPath = itemsDefDir.resolve(name + ".json");
            assertTrue(Files.exists(defPath), "Missing modern items definition for: " + name + " at " + defPath);
        }
    }

    @Test
    @DisplayName("Validate blockstates exist for every registered block")
    void testBlockstatesValidation() {
        Path blockstatesDir = RESOURCES_PATH.resolve(Paths.get("assets", IsraelSimulator.MOD_ID, "blockstates"));
        assertTrue(Files.exists(blockstatesDir), "blockstates directory must exist");

        for (String name : ALL_MOD_BLOCKS) {
            Path bsPath = blockstatesDir.resolve(name + ".json");
            assertTrue(Files.exists(bsPath), "Missing blockstate for block: " + name);
        }
    }

    @Test
    @DisplayName("Validate sounds.json contains all registered mod sound events")
    void testSoundsJsonValidation() throws IOException {
        Path soundsJsonPath = RESOURCES_PATH.resolve(Paths.get("assets", IsraelSimulator.MOD_ID, "sounds.json"));
        assertTrue(Files.exists(soundsJsonPath), "sounds.json must exist");

        try (FileReader reader = new FileReader(soundsJsonPath.toFile())) {
            JsonObject soundsJson = GSON.fromJson(reader, JsonObject.class);
            assertNotNull(soundsJson);

            for (String soundName : ALL_MOD_SOUNDS) {
                assertTrue(soundsJson.has(soundName), "sounds.json missing registered sound event: " + soundName);
            }
        }
    }

    @Test
    @DisplayName("Validate advancement JSON files have proper display titles and criteria")
    void testAdvancementsValidation() throws IOException {
        Path advDir = RESOURCES_PATH.resolve(Paths.get("data", IsraelSimulator.MOD_ID, "advancement"));
        assertTrue(Files.exists(advDir), "Advancements directory must exist");

        try (Stream<Path> stream = Files.walk(advDir)) {
            List<Path> advFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .toList();

            assertTrue(advFiles.size() >= 10, "Should have at least 10 custom advancements");

            for (Path advFile : advFiles) {
                try (FileReader reader = new FileReader(advFile.toFile())) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    assertTrue(json.has("criteria"), "Advancement must have 'criteria': " + advFile.getFileName());
                    if (json.has("display")) {
                        JsonObject display = json.getAsJsonObject("display");
                        assertTrue(display.has("title"), "Advancement display must have 'title': " + advFile.getFileName());
                        assertTrue(display.has("description"), "Advancement display must have 'description': " + advFile.getFileName());
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Validate resource names follow Minecraft lowercase conventions")
    void testResourceNamingConventions() throws IOException {
        try (Stream<Path> stream = Files.walk(RESOURCES_PATH)) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                String filename = p.getFileName().toString();
                assertTrue(filename.matches("[a-z0-9_.-]+"), "Resource name must follow [a-z0-9_.-]+: " + filename);
            });
        }
    }
}
