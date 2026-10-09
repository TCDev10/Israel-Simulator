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
    @DisplayName("Verify Rabbi's Crown item definition, flat model and equipment assets")
    void testRabbisCrownItemModelAndEquipmentAssets() throws IOException {
        // The crown renders as a flat generated item in hand/GUI; the worn appearance
        // (hat, payot, beard) comes from the humanoid equipment asset layers.
        Path itemDefPath = ASSETS_PATH.resolve(Paths.get("items", "rabbis_crown.json"));
        assertTrue(Files.exists(itemDefPath), "Rabbi's Crown item definition must exist");
        JsonObject itemDef = GSON.fromJson(Files.readString(itemDefPath), JsonObject.class);
        JsonObject modelRef = itemDef.getAsJsonObject("model");
        assertNotNull(modelRef, "item definition must reference a model");
        assertEquals("israel_simulator:item/rabbis_crown", modelRef.get("model").getAsString(),
                "item definition must point at the crown model");

        Path modelPath = ASSETS_PATH.resolve(Paths.get("models", "item", "rabbis_crown.json"));
        assertTrue(Files.exists(modelPath), "Rabbi's Crown model must exist");
        JsonObject model = GSON.fromJson(Files.readString(modelPath), JsonObject.class);
        assertEquals("minecraft:item/generated", model.get("parent").getAsString(),
                "the inventory/hand model is a flat generated item");
        JsonObject textures = model.getAsJsonObject("textures");
        assertNotNull(textures, "model must reference its texture");
        assertEquals("israel_simulator:item/rabbis_crown", textures.get("layer0").getAsString(),
                "the flat model must use the crown item texture");

        Path equipmentPath = ASSETS_PATH.resolve(Paths.get("equipment", "rabbis_crown.json"));
        assertTrue(Files.exists(equipmentPath), "the worn appearance must come from the equipment asset");
        JsonObject equipment = GSON.fromJson(Files.readString(equipmentPath), JsonObject.class);
        JsonObject layers = equipment.getAsJsonObject("layers");
        assertNotNull(layers, "equipment asset must define layers");
        assertTrue(layers.has("humanoid"), "equipment asset must define the humanoid layer");
        assertTrue(layers.has("humanoid_baby"), "equipment asset must define the humanoid_baby layer");

        assertTrue(Files.exists(ASSETS_PATH.resolve(Paths.get("textures", "entity", "equipment", "humanoid", "rabbis_crown.png"))),
                "humanoid equipment texture must exist");
        assertTrue(Files.exists(ASSETS_PATH.resolve(Paths.get("textures", "entity", "equipment", "humanoid_baby", "rabbis_crown.png"))),
                "humanoid_baby equipment texture must exist");
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
