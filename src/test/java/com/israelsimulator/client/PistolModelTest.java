package com.israelsimulator.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security Pistol: 3D cuboid item model in hand/ground/frame/head, flat icon in the GUI,
 * and Coalition Guards that hold and aim it.
 */
class PistolModelTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/israel_simulator");
    private static final Path MODEL = ASSETS.resolve("models/item/pistol.json");
    private static final Path ICON = ASSETS.resolve("models/item/pistol_icon.json");
    private static final Path ITEM_DEF = ASSETS.resolve("items/pistol.json");
    private static final Path ATLAS = ASSETS.resolve("textures/item/pistol_model.png");
    private static final Path ICON_TEX = ASSETS.resolve("textures/item/pistol.png");
    private static final Set<String> FACES = Set.of("north", "south", "east", "west", "up", "down");
    private static final List<String> CONTEXTS = List.of("thirdperson_righthand", "thirdperson_lefthand",
            "firstperson_righthand", "firstperson_lefthand", "gui", "ground", "fixed", "head");

    private static JsonObject read(Path p) throws Exception {
        assertTrue(Files.exists(p), "missing " + p);
        return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
    }

    private static float[] vec(JsonObject o, String key) {
        JsonArray a = o.getAsJsonArray(key);
        assertEquals(3, a.size(), key);
        return new float[] {a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()};
    }

    @Test
    @DisplayName("3D pistol model: named cuboid parts, bounds -16..32, valid rotations, every face textured")
    void modelElementsAreValid() throws Exception {
        JsonObject model = read(MODEL);
        assertFalse(model.has("parent"), "the 3D model is built from its own elements, not item/generated");
        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals("israel_simulator:item/pistol_model", textures.get("gun").getAsString());
        assertTrue(textures.has("particle"));

        JsonArray elements = model.getAsJsonArray("elements");
        assertTrue(elements.size() >= 12, "expected a detailed model, got " + elements.size() + " elements");
        Set<String> names = new HashSet<>();
        for (JsonElement e : elements) {
            JsonObject el = e.getAsJsonObject();
            names.add(el.get("name").getAsString());
            float[] from = vec(el, "from");
            float[] to = vec(el, "to");
            for (int i = 0; i < 3; i++) {
                assertTrue(from[i] >= -16 && from[i] <= 32 && to[i] >= -16 && to[i] <= 32, "out of bounds: " + el);
                assertTrue(to[i] >= from[i], "inverted cuboid: " + el);
            }
            if (el.has("rotation")) {
                JsonObject rot = el.getAsJsonObject("rotation");
                assertTrue(Set.of("x", "y", "z").contains(rot.get("axis").getAsString()), el.toString());
                float angle = rot.get("angle").getAsFloat();
                assertTrue(angle >= -45 && angle <= 45 && angle % 22.5F == 0,
                        "angle must be a legacy-safe multiple of 22.5 in [-45, 45]: " + el);
                vec(rot, "origin");
            }
            JsonObject faces = el.getAsJsonObject("faces");
            assertEquals(FACES, faces.keySet(), "all six faces must be textured: " + el.get("name"));
            for (Map.Entry<String, JsonElement> f : faces.entrySet()) {
                JsonObject face = f.getValue().getAsJsonObject();
                assertEquals("#gun", face.get("texture").getAsString());
                JsonArray uv = face.getAsJsonArray("uv");
                assertEquals(4, uv.size());
                for (JsonElement v : uv) {
                    assertTrue(v.getAsFloat() >= 0 && v.getAsFloat() <= 16, "uv out of range: " + face);
                }
            }
        }
        for (String part : List.of("slide", "barrel", "serration_1", "frame", "trigger_guard_bottom",
                "trigger_guard_front", "trigger", "grip", "front_sight", "rear_sight_left", "magazine_base")) {
            assertTrue(names.contains(part), "missing part " + part);
        }
    }

    @Test
    @DisplayName("3D pistol model has display transforms for every context, within vanilla limits")
    void displayTransforms() throws Exception {
        JsonObject display = read(MODEL).getAsJsonObject("display");
        for (String ctx : CONTEXTS) {
            assertTrue(display.has(ctx), "missing display." + ctx);
            JsonObject t = display.getAsJsonObject(ctx);
            for (float v : vec(t, "translation")) {
                assertTrue(Math.abs(v) <= 80, ctx + " translation clamped by the game at +-80");
            }
            for (float v : vec(t, "scale")) {
                assertTrue(v > 0 && v <= 4, ctx + " scale clamped by the game at 4");
            }
            vec(t, "rotation");
        }
        // Third person is tuned so the barrel points forward with the vanilla ITEM arm pose.
        float[] tp = vec(display.getAsJsonObject("thirdperson_righthand"), "rotation");
        assertArrayEquals(new float[] {0, -90, -72}, tp);
    }

    @Test
    @DisplayName("Item definition uses the flat icon in the GUI and the 3D model everywhere else")
    void itemDefinitionSelectsByDisplayContext() throws Exception {
        JsonObject model = read(ITEM_DEF).getAsJsonObject("model");
        assertEquals("minecraft:select", model.get("type").getAsString());
        assertEquals("minecraft:display_context", model.get("property").getAsString());
        JsonObject icCase = model.getAsJsonArray("cases").get(0).getAsJsonObject();
        Set<String> when = new HashSet<>();
        icCase.getAsJsonArray("when").forEach(w -> when.add(w.getAsString()));
        assertTrue(when.contains("gui"));
        assertEquals("israel_simulator:item/pistol_icon", icCase.getAsJsonObject("model").get("model").getAsString());
        assertEquals("israel_simulator:item/pistol", model.getAsJsonObject("fallback").get("model").getAsString());

        JsonObject icon = read(ICON);
        assertEquals("minecraft:item/generated", icon.get("parent").getAsString());
        assertEquals("israel_simulator:item/pistol", icon.getAsJsonObject("textures").get("layer0").getAsString());
    }

    @Test
    @DisplayName("Pistol textures exist: 32x32 opaque atlas and the flat icon")
    void texturesExist() throws Exception {
        BufferedImage atlas = ImageIO.read(ATLAS.toFile());
        assertNotNull(atlas);
        assertEquals(32, atlas.getWidth());
        assertEquals(32, atlas.getHeight());
        Set<Integer> colours = new HashSet<>();
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                int argb = atlas.getRGB(x, y);
                assertEquals(0xFF, argb >>> 24, "atlas must be opaque");
                colours.add(argb);
            }
        }
        assertTrue(colours.size() > 20, "atlas should not be flat colour");
        assertNotNull(ImageIO.read(ICON_TEX.toFile()));
    }

    @Test
    @DisplayName("Coalition Guards hold the pistol with the ITEM pose and aim it with raised arms when hostile")
    void guardsAimThePistol() throws Exception {
        String renderer = Files.readString(Path.of("src/main/java/com/israelsimulator/client/renderer/BibiGuardRenderer.java"));
        assertTrue(renderer.contains("ArmPose.BOW_AND_ARROW") && renderer.contains("ArmPose.ITEM"), renderer);
        assertTrue(renderer.contains("new BibiGuardModel("));
        String model = Files.readString(Path.of("src/main/java/com/israelsimulator/client/renderer/BibiGuardModel.java"));
        assertTrue(model.contains("public void translateToHand("));
        String entity = Files.readString(Path.of("src/main/java/com/israelsimulator/entity/boss/BibiGuardEntity.java"));
        assertTrue(entity.contains("this.setAggressive("), "aggressive flag drives the aiming pose on the client");
    }
}
