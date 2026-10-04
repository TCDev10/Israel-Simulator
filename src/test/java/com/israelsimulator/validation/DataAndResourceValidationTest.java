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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            "hava_nagila_disc", "tahini", "dates", "grapes", "olives", "citrus", "challah", "rugelach",
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
    @DisplayName("Validate recipes exist and are not a single vanilla ingredient")
    void testRecipesValidation() throws IOException {
        Path recipeDir = RESOURCES_PATH.resolve(Paths.get("data", IsraelSimulator.MOD_ID, "recipe"));
        assertTrue(Files.exists(recipeDir), "Recipe directory must be data/israel_simulator/recipe");
        assertFalse(Files.exists(RESOURCES_PATH.resolve(Paths.get("data", IsraelSimulator.MOD_ID, "recipes"))),
                "Legacy recipes/ folder is not loaded on Minecraft 26.2");

        File[] recipes = recipeDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(recipes);
        assertTrue(recipes.length >= 10, "Should have at least 10 culinary and crafting recipes");

        Map<String, List<String>> shapeless = Map.ofEntries(
                Map.entry("tahini.json", List.of("minecraft:wheat_seeds", "minecraft:wheat_seeds", "minecraft:bowl")),
                Map.entry("hummus.json", List.of("israel_simulator:tahini", "minecraft:beetroot", "minecraft:bowl")),
                Map.entry("shakshuka.json", List.of("minecraft:egg", "minecraft:beetroot", "minecraft:bowl")),
                Map.entry("sabich.json", List.of("minecraft:bread", "minecraft:egg", "minecraft:potato", "israel_simulator:tahini")),
                Map.entry("challah.json", List.of("minecraft:wheat", "minecraft:wheat", "minecraft:wheat", "minecraft:egg", "minecraft:sugar")),
                Map.entry("rugelach.json", List.of("minecraft:wheat", "minecraft:sugar", "minecraft:cocoa_beans")),
                Map.entry("dates.json", List.of("minecraft:sweet_berries", "minecraft:sugar")),
                Map.entry("olives.json", List.of("minecraft:kelp", "minecraft:wheat_seeds")),
                Map.entry("citrus.json", List.of("minecraft:glow_berries", "minecraft:sugar")),
                Map.entry("matzo.json", List.of("minecraft:wheat", "minecraft:wheat", "minecraft:water_bucket")),
                Map.entry("sufganiyah.json", List.of("minecraft:wheat", "minecraft:sugar", "minecraft:sweet_berries")),
                Map.entry("hamantash.json", List.of("minecraft:wheat", "minecraft:sugar", "israel_simulator:dates"))
        );
        Map<String, String> results = Map.ofEntries(
                Map.entry("tahini.json", "israel_simulator:tahini"),
                Map.entry("falafel.json", "israel_simulator:falafel"),
                Map.entry("hummus.json", "israel_simulator:hummus"),
                Map.entry("shakshuka.json", "israel_simulator:shakshuka"),
                Map.entry("sabich.json", "israel_simulator:sabich"),
                Map.entry("challah.json", "israel_simulator:challah"),
                Map.entry("rugelach.json", "israel_simulator:rugelach"),
                Map.entry("dates.json", "israel_simulator:dates"),
                Map.entry("olives.json", "israel_simulator:olives"),
                Map.entry("citrus.json", "israel_simulator:citrus"),
                Map.entry("matzo.json", "israel_simulator:matzo"),
                Map.entry("sufganiyah.json", "israel_simulator:sufganiyah"),
                Map.entry("hamantash.json", "israel_simulator:hamantash")
        );

        for (File recipeFile : recipes) {
            try (FileReader reader = new FileReader(recipeFile)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                assertTrue(json.has("type"), "Recipe must have a type: " + recipeFile.getName());
                assertTrue(json.has("result"), "Recipe must define result: " + recipeFile.getName());
                List<String> ingredients = ingredientIds(json);
                long distinct = ingredients.stream().distinct().count();
                assertTrue(ingredients.size() >= 2 && distinct >= 2,
                        recipeFile.getName() + " is still a single ingredient: " + ingredients);
                String resultId = json.getAsJsonObject("result").get("id").getAsString();
                if (results.containsKey(recipeFile.getName())) {
                    assertEquals(results.get(recipeFile.getName()), resultId, recipeFile.getName());
                }
            } catch (AssertionError e) {
                throw e;
            } catch (Exception e) {
                throw new AssertionError("Failed reading recipe: " + recipeFile.getName(), e);
            }
        }

        for (Map.Entry<String, List<String>> expected : shapeless.entrySet()) {
            Path path = recipeDir.resolve(expected.getKey());
            assertTrue(Files.exists(path), "Missing recipe " + expected.getKey());
            JsonObject json = GSON.fromJson(Files.readString(path), JsonObject.class);
            assertEquals("minecraft:crafting_shapeless", json.get("type").getAsString(), expected.getKey());
            assertEquals(expected.getValue(), ingredientIds(json), expected.getKey());
        }

        JsonObject falafel = GSON.fromJson(Files.readString(recipeDir.resolve("falafel.json")), JsonObject.class);
        assertEquals("minecraft:crafting_shaped", falafel.get("type").getAsString());
        assertEquals(3, falafel.getAsJsonObject("result").get("count").getAsInt());
        JsonObject key = falafel.getAsJsonObject("key");
        assertEquals("minecraft:beetroot", key.get("B").getAsString());
        assertEquals("minecraft:wheat_seeds", key.get("S").getAsString());
        assertEquals("minecraft:wheat", key.get("W").getAsString());
        assertTrue(ingredientIds(falafel).contains("minecraft:beetroot"));
        assertTrue(ingredientIds(falafel).contains("minecraft:wheat"));
        assertTrue(ingredientIds(falafel).contains("minecraft:wheat_seeds"));

        String provider = Files.readString(Path.of(
                "src/main/java/com/israelsimulator/datagen/ModRecipeProvider.java"));
        assertTrue(provider.contains("requires(Items.WHEAT_SEEDS, 2)"));
        assertTrue(provider.contains("requires(Items.BOWL)"));
        assertTrue(provider.contains("requires(ModItems.TAHINI.get())"));
        assertTrue(provider.contains("requires(Items.POTATO)"));
        assertTrue(provider.contains("requires(Items.WATER_BUCKET)"));
        assertTrue(provider.contains("ModItems.MATZO.get()"));
        assertTrue(provider.contains("ModItems.SUFGANIYAH.get()"));
        assertTrue(provider.contains("ModItems.HAMANTASH.get()"));
        assertTrue(provider.contains("requires(ModItems.DATES.get())"));
        assertTrue(provider.contains("requires(Items.GLOW_BERRIES)"));
        assertFalse(provider.contains("requires(Items.WHEAT_SEEDS, 3)"), "tahini must not be seeds alone");
        assertFalse(provider.contains("Items.CARROT"), "sabich filling is potato, not carrot");
    }

    @Test
    @DisplayName("Festival foods restore hunger and saturation")
    void testFestivalFoodsHaveFoodComponent() throws IOException {
        String festival = Files.readString(Path.of(
                "src/main/java/com/israelsimulator/item/festival/FestivalItems.java"));
        String items = Files.readString(Path.of(
                "src/main/java/com/israelsimulator/registry/ModItems.java"));
        String food = Files.readString(Path.of(
                "src/main/java/com/israelsimulator/item/food/IsraelFoodProperties.java"));
        String pareve = Files.readString(RESOURCES_PATH.resolve(Paths.get(
                "data", IsraelSimulator.MOD_ID, "tags", "item", "pareve.json")));

        assertTrue(items.contains("registerSimpleItem(\"matzo\", FestivalItems::matzo)"));
        assertTrue(items.contains("registerSimpleItem(\"sufganiyah\", FestivalItems::sufganiyah)"));
        assertTrue(items.contains("registerSimpleItem(\"hamantash\", FestivalItems::hamantash)"));
        assertTrue(festival.contains(".food(IsraelFoodProperties.MATZO, IsraelFoodProperties.MATZO_CONSUMABLE)"));
        assertTrue(festival.contains(".food(IsraelFoodProperties.SUFGANIYAH, IsraelFoodProperties.SUFGANIYAH_CONSUMABLE)"));
        assertTrue(festival.contains(".food(IsraelFoodProperties.HAMANTASH, IsraelFoodProperties.HAMANTASH_CONSUMABLE)"));
        assertTrue(food.contains("public static final FoodProperties MATZO"));
        assertTrue(food.contains("nutrition(4).saturationModifier(0.5F)"));
        assertTrue(food.contains("public static final FoodProperties SUFGANIYAH"));
        assertTrue(food.contains("nutrition(5).saturationModifier(0.6F)"));
        assertTrue(food.contains("public static final FoodProperties HAMANTASH"));
        assertTrue(pareve.contains("israel_simulator:matzo"));
        assertTrue(pareve.contains("israel_simulator:sufganiyah"));
        assertTrue(pareve.contains("israel_simulator:hamantash"));
    }

    private static List<String> ingredientIds(JsonObject json) {
        List<String> ingredients = new ArrayList<>();
        if (json.has("ingredients")) {
            for (JsonElement element : json.getAsJsonArray("ingredients")) {
                ingredients.add(element.getAsString());
            }
        }
        if (json.has("key")) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("key").entrySet()) {
                ingredients.add(entry.getValue().getAsString());
            }
        }
        return ingredients;
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
