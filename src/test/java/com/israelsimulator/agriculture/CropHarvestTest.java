package com.israelsimulator.agriculture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.israelsimulator.block.HarvestRules;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Olive/date/citrus leaves and Mediterranean herbs: no infinite right-click food,
 * growth by random tick and bone meal, and real (non-grey, non-vanilla) textures.
 */
class CropHarvestTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/israel_simulator");
    private static final Path DATA = Path.of("src/main/resources/data/israel_simulator");
    private static final List<String> LEAVES = List.of("olive_leaves", "date_palm_leaves", "citrus_leaves");
    private static final List<String> PLANTS = List.of("olive_leaves", "date_palm_leaves", "citrus_leaves", "mediterranean_herbs");

    @Test
    @DisplayName("Harvest resets a mature plant to a lower age")
    void harvestResetsAge() {
        assertTrue(HarvestRules.canHarvest(HarvestRules.MAX_AGE));
        int after = HarvestRules.ageAfterHarvest(HarvestRules.MAX_AGE);
        assertTrue(after < HarvestRules.MAX_AGE, "age must drop after harvest");
        assertEquals(HarvestRules.RESET_AGE, after);
        assertFalse(HarvestRules.canHarvest(after), "a just-harvested plant cannot be harvested again");
    }

    @Test
    @DisplayName("No harvest while immature: age stays unchanged")
    void noHarvestWhenImmature() {
        for (int age = 0; age < HarvestRules.MAX_AGE; age++) {
            assertFalse(HarvestRules.canHarvest(age), "age " + age);
            assertEquals(age, HarvestRules.ageAfterHarvest(age));
        }
    }

    @Test
    @DisplayName("Repeated right-clicks never give more than one harvest without regrowth")
    void noInfiniteHarvest() {
        int age = HarvestRules.MAX_AGE;
        int harvests = 0;
        for (int click = 0; click < 100; click++) {
            if (HarvestRules.canHarvest(age)) {
                harvests++;
            }
            age = HarvestRules.ageAfterHarvest(age);
        }
        assertEquals(1, harvests);
    }

    @Test
    @DisplayName("Growth (random tick / bone meal) climbs one stage at a time and stops at max age")
    void growthStepsToMax() {
        int age = HarvestRules.RESET_AGE;
        int steps = 0;
        while (HarvestRules.canGrow(age)) {
            age = HarvestRules.ageAfterGrowth(age);
            steps++;
        }
        assertEquals(HarvestRules.MAX_AGE, age);
        assertEquals(HarvestRules.MAX_AGE - HarvestRules.RESET_AGE, steps);
        assertEquals(HarvestRules.MAX_AGE, HarvestRules.ageAfterGrowth(HarvestRules.MAX_AGE));
    }

    @Test
    @DisplayName("Plant blocks gate right-click harvest on maturity and support bone meal and random ticks")
    void blocksUseHarvestRules() throws Exception {
        for (String cls : List.of("FruitingLeavesBlock", "MediterraneanHerbBlock")) {
            String src = Files.readString(Path.of("src/main/java/com/israelsimulator/block/" + cls + ".java"));
            assertTrue(src.contains("implements BonemealableBlock"), cls);
            assertTrue(src.contains("HarvestRules.canHarvest(age)"), cls + " must check maturity before harvesting");
            assertTrue(src.contains("HarvestRules.ageAfterHarvest(age)"), cls + " must reset the age after harvest");
            assertTrue(src.contains("protected void randomTick("), cls);
            assertTrue(src.contains("Items.BONE_MEAL"), cls + " must let bone meal through");
        }
    }

    @Test
    @DisplayName("Blockstates map every age 0-3 to mod models with mod textures (no vanilla fern/leaves)")
    void blockstatesAndModels() throws Exception {
        for (String id : PLANTS) {
            JsonObject variants = json(ASSETS.resolve("blockstates/" + id + ".json")).getAsJsonObject("variants");
            for (int age = 0; age <= 3; age++) {
                String model = variants.getAsJsonObject("age=" + age).get("model").getAsString();
                assertEquals("israel_simulator:block/" + id + "_stage" + age, model);
                String modelJson = Files.readString(ASSETS.resolve("models/block/" + id + "_stage" + age + ".json"));
                assertTrue(modelJson.contains("israel_simulator:block/" + id + "_stage" + age), modelJson);
                assertFalse(modelJson.contains("tintindex"), "colours are baked in, no tint expected");
            }
            String itemModel = Files.readString(ASSETS.resolve("models/item/" + id + ".json"));
            assertFalse(itemModel.contains("minecraft:block/"), id + " item must not use a vanilla texture: " + itemModel);
        }
    }

    @Test
    @DisplayName("Plant and fruit textures are coloured 16x16 PNGs, not grey")
    void texturesAreColoured() throws Exception {
        for (String id : PLANTS) {
            for (int age = 0; age <= 3; age++) {
                assertColoured16(ASSETS.resolve("textures/block/" + id + "_stage" + age + ".png"));
            }
        }
        for (String item : List.of("dates", "olives", "citrus", "mediterranean_herbs")) {
            assertColoured16(ASSETS.resolve("textures/item/" + item + ".png"));
        }
    }

    @Test
    @DisplayName("Breaking leaves only drops fruit when ripe; worldgen spawns mixed ages")
    void lootAndWorldgen() throws Exception {
        for (String id : LEAVES) {
            String loot = Files.readString(DATA.resolve("loot_table/blocks/" + id + ".json"));
            assertTrue(loot.contains("minecraft:block_state_property") && loot.contains("\"age\": \"3\""), loot);
        }
        String herbs = Files.readString(DATA.resolve("loot_table/blocks/mediterranean_herbs.json"));
        assertTrue(herbs.contains("israel_simulator:mediterranean_herbs"));
        assertFalse(herbs.contains("wheat_seeds"));
        for (String f : List.of("olive_tree", "date_palm", "citrus_orchard", "mediterranean_herbs_patch")) {
            String cf = Files.readString(DATA.resolve("worldgen/configured_feature/" + f + ".json"));
            assertTrue(cf.contains("minecraft:randomized_int_state_provider") && cf.contains("\"property\": \"age\""), f);
        }
    }

    private static JsonObject json(Path p) throws Exception {
        return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
    }

    private static void assertColoured16(Path p) throws Exception {
        assertTrue(Files.exists(p), "missing " + p);
        BufferedImage img = ImageIO.read(p.toFile());
        assertEquals(16, img.getWidth(), p + " width");
        assertEquals(16, img.getHeight(), p + " height");
        int coloured = 0;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int argb = img.getRGB(x, y);
                if ((argb >>> 24) == 0) continue;
                int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                if (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) > 24) coloured++;
            }
        }
        assertTrue(coloured >= 8, p + " looks grey (" + coloured + " coloured pixels)");
    }
}
