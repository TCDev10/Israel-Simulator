package com.israelsimulator.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionalFaunaAndCropsTest {
    private static final Path ROOT = Path.of("src", "main", "resources");
    private static final String[] BIOMES = {
            "mediterranean_coast", "israeli_agriculture", "judean_desert",
            "dead_sea", "urban_area", "jerusalem", "tropical_island"
    };
    private static final String[] CROPS = {
            "chickpea_crop", "tomato_crop", "eggplant_crop", "sesame_crop"
    };

    @Test
    @DisplayName("Every mod biome has peaceful fauna suited to its region")
    void biomeFaunaIsDistinctAndAppropriate() throws Exception {
        JsonObject coast = biome("mediterranean_coast");
        assertContains(spawns(coast, "creature"), "minecraft:turtle", "minecraft:fox");
        assertContains(spawns(coast, "water_ambient"), "minecraft:cod");
        assertContains(spawns(coast, "water_creature"), "minecraft:dolphin");

        JsonObject agriculture = biome("israeli_agriculture");
        assertContains(spawns(agriculture, "creature"), "minecraft:sheep", "minecraft:cow",
                "minecraft:chicken", "minecraft:goat", "minecraft:rabbit");
        assertContains(spawns(agriculture, "creature"), "israel_simulator:mountain_gazelle");
        assertFalse(spawns(agriculture, "water_ambient").contains("minecraft:tropical_fish"));
        assertFalse(spawns(agriculture, "water_creature").contains("minecraft:dolphin"));

        JsonObject desert = biome("judean_desert");
        assertContains(spawns(desert, "creature"), "minecraft:camel", "minecraft:rabbit",
                "minecraft:fox", "minecraft:goat", "israel_simulator:nubian_ibex",
                "israel_simulator:cape_hyrax");

        JsonObject deadSea = biome("dead_sea");
        assertContains(spawns(deadSea, "creature"), "minecraft:fox", "minecraft:goat",
                "israel_simulator:nubian_ibex", "israel_simulator:cape_hyrax");
        assertTrue(spawns(deadSea, "water_ambient").isEmpty());
        assertTrue(spawns(deadSea, "water_creature").isEmpty());
        assertTrue(spawns(deadSea, "underground_water_creature").isEmpty());

        assertContains(spawns(biome("urban_area"), "creature"),
                "minecraft:cat", "minecraft:chicken", "minecraft:fox");
        assertContains(spawns(biome("jerusalem"), "creature"),
                "minecraft:sheep", "minecraft:cat", "minecraft:goat",
                "israel_simulator:mountain_gazelle", "israel_simulator:nubian_ibex",
                "israel_simulator:cape_hyrax");
        assertContains(spawns(coast, "creature"), "israel_simulator:mountain_gazelle",
                "israel_simulator:cape_hyrax");
        assertFalse(spawns(biome("urban_area"), "creature").stream()
                .anyMatch(id -> id.startsWith("israel_simulator:")));
        assertFalse(spawns(biome("tropical_island"), "creature").stream()
                .anyMatch(id -> id.startsWith("israel_simulator:")));
        assertContains(spawns(biome("tropical_island"), "creature"),
                "minecraft:parrot", "minecraft:ocelot", "minecraft:turtle");
        assertContains(spawns(biome("tropical_island"), "water_ambient"),
                "minecraft:tropical_fish");
        assertContains(spawns(biome("tropical_island"), "water_creature"),
                "minecraft:dolphin");

        for (String id : BIOMES) {
            JsonArray creatures = biome(id).getAsJsonObject("spawners").getAsJsonArray("creature");
            assertFalse(creatures.isEmpty(), id + " should define peaceful creatures");
            for (int i = 0; i < creatures.size(); i++) {
                JsonObject spawn = creatures.get(i).getAsJsonObject();
                assertTrue(spawn.get("minCount").getAsInt() > 0, id + " has an empty spawn group");
                assertTrue(spawn.get("maxCount").getAsInt() <= 8, id + " has an excessive spawn group");
            }
        }
    }

    @Test
    @DisplayName("Custom regional wildlife has loot, spawn eggs, and original client assets")
    void customWildlifeHasCompleteResources() throws Exception {
        String[][] species = {
                {"mountain_gazelle", "minecraft:mutton"},
                {"nubian_ibex", "minecraft:mutton"},
                {"cape_hyrax", "minecraft:rabbit"}
        };
        for (String[] entry : species) {
            String id = entry[0];
            JsonObject loot = json(ROOT.resolve("data/israel_simulator/loot_table/entities/" + id + ".json"));
            assertEquals("minecraft:entity", loot.get("type").getAsString());
            assertTrue(loot.toString().contains(entry[1]), id + " should yield its expected meat drop");
            JsonObject itemModel = json(ROOT.resolve("assets/israel_simulator/models/item/" + id + "_spawn_egg.json"));
            assertEquals("minecraft:item/generated", itemModel.get("parent").getAsString());
            assertTrue(Files.exists(ROOT.resolve("assets/israel_simulator/items/" + id + "_spawn_egg.json")));
            assertEquals(16, ImageIO.read(ROOT.resolve("assets/israel_simulator/textures/item/"
                    + id + "_spawn_egg.png").toFile()).getWidth());
            BufferedImage texture = ImageIO.read(ROOT.resolve("assets/israel_simulator/textures/entity/"
                    + id + ".png").toFile());
            assertEquals(64, texture.getWidth());
            assertEquals(32, texture.getHeight());
            for (int y = 0; y < texture.getHeight(); y++) {
                for (int x = 0; x < texture.getWidth(); x++) {
                    assertEquals(0xFF, texture.getRGB(x, y) >>> 24,
                            id + " texture has transparent texel at " + x + "," + y);
                }
            }
        }
    }

    @Test
    @DisplayName("Regional food crops have complete stage models, textures, and harvest loot")
    void cropsHaveModelsTexturesAndLoot() throws Exception {
        for (String crop : CROPS) {
            JsonObject blockstate = json(ROOT.resolve("assets/israel_simulator/blockstates/" + crop + ".json"));
            JsonObject variants = blockstate.getAsJsonObject("variants");
            assertEquals(8, variants.size(), crop + " should define every age");

            Set<String> referencedModels = new HashSet<>();
            for (int age = 0; age <= 7; age++) {
                String modelId = variants.getAsJsonObject("age=" + age).get("model").getAsString();
                String modelPath = modelId.substring("israel_simulator:".length()).replace(':', '/');
                referencedModels.add(modelPath);
            }
            assertEquals(4, referencedModels.size(), crop + " should show four growth stages");
            for (String modelPath : referencedModels) {
                JsonObject model = json(ROOT.resolve("assets/israel_simulator/models/" + modelPath + ".json"));
                String texture = model.getAsJsonObject("textures").get("crop").getAsString();
                String[] textureParts = texture.split(":", 2);
                String texturePath = "assets/" + textureParts[0] + "/textures/" + textureParts[1];
                assertEquals(16, ImageIO.read(ROOT.resolve(texturePath + ".png").toFile()).getWidth());
            }

            JsonObject loot = json(ROOT.resolve("data/israel_simulator/loot_table/blocks/" + crop + ".json"));
            assertEquals("minecraft:block", loot.get("type").getAsString());
            assertTrue(loot.toString().contains("\"age\":\"7\""), crop + " should have a mature harvest");
        }

        for (String item : new String[]{"chickpeas", "tomato", "eggplant", "sesame_seeds"}) {
            assertTrue(Files.exists(ROOT.resolve("assets/israel_simulator/models/item/" + item + ".json")));
            assertEquals(16, ImageIO.read(ROOT.resolve("assets/israel_simulator/textures/item/" + item + ".png")
                    .toFile()).getWidth());
            String farmLoot = Files.readString(ROOT.resolve(
                    "data/israel_simulator/loot_table/chests/agricultural_farm.json"));
            assertTrue(farmLoot.contains("israel_simulator:" + item),
                    "Farm loot should provide a starting supply of " + item);
        }
    }

    @Test
    @DisplayName("Traditional food recipes use the dedicated crop ingredients")
    void recipesUseRealisticIngredients() throws Exception {
        assertRecipeContains("falafel", "israel_simulator:chickpeas");
        assertRecipeContains("falafel", "israel_simulator:mediterranean_herbs");
        assertRecipeContains("hummus", "israel_simulator:chickpeas");
        assertRecipeContains("shakshuka", "israel_simulator:tomato");
        assertRecipeContains("sabich", "israel_simulator:eggplant");
        assertRecipeContains("tahini", "israel_simulator:sesame_seeds");

        assertFalse(Files.exists(ROOT.resolve("data/israel_simulator/recipe/dates.json")));
        assertFalse(Files.exists(ROOT.resolve("data/israel_simulator/recipe/olives.json")));
        assertFalse(Files.exists(ROOT.resolve("data/israel_simulator/recipe/citrus.json")));
    }

    private JsonObject biome(String id) throws Exception {
        return json(ROOT.resolve("data/israel_simulator/worldgen/biome/" + id + ".json"));
    }

    private JsonObject json(Path path) throws Exception {
        assertTrue(Files.exists(path), "Missing resource: " + path);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private Set<String> spawns(JsonObject biome, String category) {
        Set<String> result = new HashSet<>();
        JsonArray spawners = biome.getAsJsonObject("spawners").getAsJsonArray(category);
        for (int i = 0; i < spawners.size(); i++) {
            result.add(spawners.get(i).getAsJsonObject().get("type").getAsString());
        }
        return result;
    }

    private void assertContains(Set<String> actual, String... expected) {
        for (String id : expected) {
            assertTrue(actual.contains(id), "Missing expected local fauna: " + id);
        }
    }

    private void assertRecipeContains(String recipe, String ingredient) throws Exception {
        JsonObject data = json(ROOT.resolve("data/israel_simulator/recipe/" + recipe + ".json"));
        assertTrue(data.toString().contains(ingredient),
                recipe + " recipe should contain ingredient " + ingredient);
    }
}
