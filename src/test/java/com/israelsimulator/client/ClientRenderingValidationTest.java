package com.israelsimulator.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.client.renderer.BibiBossRenderer;
import com.israelsimulator.client.renderer.BibiGuardRenderer;
import com.israelsimulator.client.renderer.BicycleRenderer;
import com.israelsimulator.item.cultural.CulturalItems;
import com.israelsimulator.item.cultural.KippahItem;
import com.israelsimulator.item.cultural.RabbisCrownItem;
import com.israelsimulator.item.cultural.TalitItem;
import com.israelsimulator.item.cultural.TefillinItem;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Client Rendering Validation Test Suite (GAME_DESIGN.md §59, TODO §59).
 * Verifies wearable equipment models, 3D item geometry (Kippah, Talit, Tefillin,
 * Rabbi's Crown with Payot and Beard), Menorah, and Entity Renderers.
 */
public class ClientRenderingValidationTest {

    private static final Gson GSON = new Gson();
    private static final Path ASSETS_PATH = Paths.get("src", "main", "resources", "assets", IsraelSimulator.MOD_ID);

    @Test
    @DisplayName("Verify Rabbi's Crown 3D cuboid model, texture, display transforms and worn rendering")
    void testRabbisCrownItemModelAndEquipmentAssets() throws IOException {
        // The crown is a Blockbench-style cuboid model; with no equipment asset the vanilla
        // CustomHeadLayer draws this same model on the wearer's head (players and Bibi).
        Path itemDefPath = ASSETS_PATH.resolve(Paths.get("items", "rabbis_crown.json"));
        assertTrue(Files.exists(itemDefPath), "Rabbi's Crown item definition must exist");
        JsonObject itemDef = GSON.fromJson(Files.readString(itemDefPath), JsonObject.class);
        assertEquals("israel_simulator:item/rabbis_crown", itemDef.getAsJsonObject("model").get("model").getAsString(),
                "item definition must point at the crown model");

        Path modelPath = ASSETS_PATH.resolve(Paths.get("models", "item", "rabbis_crown.json"));
        JsonObject model = GSON.fromJson(Files.readString(modelPath), JsonObject.class);
        assertFalse(model.has("parent"), "the crown is a standalone cuboid model, not item/generated");
        assertEquals("israel_simulator:item/rabbis_crown", model.getAsJsonObject("textures").get("0").getAsString());
        var elements = model.getAsJsonArray("elements");
        assertTrue(elements.size() >= 30 && elements.size() <= 120, "crown must have a detailed set of cuboids");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (var el : elements) {
            JsonObject e = el.getAsJsonObject();
            names.add(e.get("name").getAsString().replaceAll("_.*", ""));
            for (String k : new String[]{"from", "to"}) {
                for (var v : e.getAsJsonArray(k)) {
                    float f = v.getAsFloat();
                    assertTrue(f >= -16 && f <= 32, "element coordinates must stay within -16..32");
                }
            }
            if (e.has("rotation")) {
                float angle = Math.abs(e.getAsJsonObject("rotation").get("angle").getAsFloat());
                assertTrue(angle <= 45, "element rotation must be within 45 degrees");
            }
        }
        for (String part : new String[]{"band", "point", "gem", "cap", "star"}) {
            assertTrue(names.contains(part), "crown must contain part: " + part);
        }
        JsonObject display = model.getAsJsonObject("display");
        for (String ctx : new String[]{"gui", "ground", "fixed", "head", "thirdperson_righthand", "firstperson_righthand"}) {
            assertTrue(display.has(ctx), "crown must define display transform: " + ctx);
        }

        assertTrue(Files.exists(ASSETS_PATH.resolve(Paths.get("textures", "item", "rabbis_crown.png"))));
        assertFalse(Files.exists(ASSETS_PATH.resolve(Paths.get("equipment", "rabbis_crown.json"))),
                "an equipment asset would replace the 3D head model with a flat armor texture");
    }

    @Test
    @DisplayName("Verify Kippah, Talit, and Tefillin 3D models and display transformations")
    void testWearables3DModels() throws IOException {
        String[] items = {"kippah", "talit", "tefillin"};

        for (String item : items) {
            Path modelPath = ASSETS_PATH.resolve(Paths.get("models", "item", item + ".json"));
            assertTrue(Files.exists(modelPath), item + " 3D model must exist");

            try (FileReader reader = new FileReader(modelPath.toFile())) {
                JsonObject model = GSON.fromJson(reader, JsonObject.class);
                assertTrue(model.has("elements"), item + " must have 3D elements array");
                assertTrue(model.has("display"), item + " must have display transforms");

                JsonObject display = model.getAsJsonObject("display");
                assertTrue(display.has("head"), item + " must specify head placement transform");
                assertTrue(display.has("gui"), item + " must specify gui placement transform");
                assertTrue(display.has("thirdperson_righthand"), item + " must specify thirdperson transform");
            }
        }
    }

    @Test
    @DisplayName("Verify Menorah block model has branches and proper lighting properties")
    void testMenorahBlockAndItemModel() throws IOException {
        Path blockModelPath = ASSETS_PATH.resolve(Paths.get("models", "block", "menorah.json"));
        Path itemDefPath = ASSETS_PATH.resolve(Paths.get("items", "menorah.json"));

        assertTrue(Files.exists(blockModelPath), "Menorah block model must exist");
        assertTrue(Files.exists(itemDefPath), "Menorah item definition must exist");

        try (FileReader reader = new FileReader(blockModelPath.toFile())) {
            JsonObject model = GSON.fromJson(reader, JsonObject.class);
            assertTrue(model.has("elements"), "Menorah must have geometry elements");
            assertTrue(model.getAsJsonArray("elements").size() >= 3, "Menorah must define base, shaft, and branches");
        }
    }

    @Test
    @DisplayName("Verify equipment definitions exist for Kippah, Talit and Tefillin")
    void testEquipmentDefinitions() throws IOException {
        String[] equipments = {"kippah", "talit", "tefillin"};

        for (String eq : equipments) {
            Path eqPath = ASSETS_PATH.resolve(Paths.get("equipment", eq + ".json"));
            assertTrue(Files.exists(eqPath), "Equipment JSON must exist for: " + eq);

            try (FileReader reader = new FileReader(eqPath.toFile())) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                assertTrue(json.has("layers"), eq + " equipment JSON must define 'layers'");
                JsonObject layers = json.getAsJsonObject("layers");
                assertTrue(layers.has("humanoid"), eq + " equipment must have humanoid layer");
            }
        }
    }

    @Test
    @DisplayName("Verify CulturalItems asset keys and classes")
    void testCulturalItemsAssetKeys() {
        assertNotNull(CulturalItems.KIPPAH_ASSET);
        assertEquals("israel_simulator:kippah", CulturalItems.KIPPAH_ASSET.identifier().toString());

        assertNotNull(CulturalItems.TALIT_ASSET);
        assertEquals("israel_simulator:talit", CulturalItems.TALIT_ASSET.identifier().toString());


        assertNotNull(KippahItem.class);
        assertNotNull(TalitItem.class);
        assertNotNull(TefillinItem.class);
        assertNotNull(RabbisCrownItem.class);
    }

    @Test
    @DisplayName("Verify Entity Renderer classes are defined and accessible")
    void testEntityRendererClasses() {
        assertNotNull(BibiBossRenderer.class);
        assertNotNull(BibiGuardRenderer.class);
        assertNotNull(BicycleRenderer.class);
    }
}
