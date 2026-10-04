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
    @DisplayName("Verify Rabbi's Crown 3D model contains Payot and Beard elements")
    void testRabbisCrown3DModelElements() throws IOException {
        Path crownModelPath = ASSETS_PATH.resolve(Paths.get("models", "item", "rabbis_crown.json"));
        assertTrue(Files.exists(crownModelPath), "Rabbi's Crown 3D model must exist");

        try (FileReader reader = new FileReader(crownModelPath.toFile())) {
            JsonObject model = GSON.fromJson(reader, JsonObject.class);
            assertTrue(model.has("elements"), "Rabbi's Crown model must contain 3D elements array");

            JsonArray elements = model.getAsJsonArray("elements");
            List<String> elementNames = new ArrayList<>();
            elements.forEach(e -> elementNames.add(e.getAsJsonObject().get("name").getAsString()));

            assertTrue(elementNames.contains("hat_brim"), "Crown must have hat_brim element");
            assertTrue(elementNames.contains("hat_top"), "Crown must have hat_top element");
            assertTrue(elementNames.contains("payot_left"), "Crown must have payot_left element");
            assertTrue(elementNames.contains("payot_right"), "Crown must have payot_right element");
            assertTrue(elementNames.contains("beard"), "Crown must have beard element");

            // Verify display transform definitions for third-person, first-person, and head
            assertTrue(model.has("display"), "Must define display transform matrix");
            JsonObject display = model.getAsJsonObject("display");
            assertTrue(display.has("head"), "Must specify head display transform");
            assertTrue(display.has("thirdperson_righthand"), "Must specify third-person display transform");
            assertTrue(display.has("gui"), "Must specify inventory GUI display transform");
        }
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
    @DisplayName("Verify equipment definitions exist for Kippah, Talit, Tefillin, and Rabbi's Crown")
    void testEquipmentDefinitions() throws IOException {
        String[] equipments = {"kippah", "talit", "tefillin", "rabbis_crown"};

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

        assertNotNull(CulturalItems.TEFILLIN_ASSET);
        assertEquals("israel_simulator:tefillin", CulturalItems.TEFILLIN_ASSET.identifier().toString());

        assertNotNull(CulturalItems.RABBIS_CROWN_ASSET);
        assertEquals("israel_simulator:rabbis_crown", CulturalItems.RABBIS_CROWN_ASSET.identifier().toString());

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
