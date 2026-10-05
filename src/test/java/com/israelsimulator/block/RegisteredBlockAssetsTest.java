package com.israelsimulator.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures every registered mod block has a blockstate, resolvable models, and
 * textures that exist either as mod PNGs or as vanilla 26.2 textures (no purple/black).
 */
class RegisteredBlockAssetsTest {

    private static final Pattern REGISTER_BLOCK = Pattern.compile(
            "registerBlock\\(\\s*\"([a-z0-9_]+)\"");

    private static final Path VANILLA_JAR = Path.of(
            "build/moddev/artifacts/minecraft-patched-26.2.0.88.jar");

    private static final Path MOD_BLOCKS_JAVA = Path.of(
            "src/main/java/com/israelsimulator/registry/ModBlocks.java");

    private List<String> registeredBlockIds() throws Exception {
        String src = Files.readString(MOD_BLOCKS_JAVA, StandardCharsets.UTF_8);
        Matcher m = REGISTER_BLOCK.matcher(src);
        List<String> ids = new ArrayList<>();
        while (m.find()) {
            ids.add(m.group(1));
        }
        assertFalse(ids.isEmpty(), "Expected DeferredBlock registrations in ModBlocks");
        return ids;
    }

    private JsonObject readJsonResource(String classpathPath) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(classpathPath);
        assertNotNull(in, "Missing resource: " + classpathPath);
        return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private boolean resourceExists(String classpathPath) {
        return getClass().getClassLoader().getResource(classpathPath) != null;
    }

    private boolean vanillaTextureExists(String texturePathNoExt) throws Exception {
        // texturePathNoExt like "minecraft:block/stone" -> assets/minecraft/textures/block/stone.png
        String[] parts = texturePathNoExt.split(":", 2);
        String ns = parts.length == 2 ? parts[0] : "minecraft";
        String path = parts.length == 2 ? parts[1] : parts[0];
        String entry = "assets/" + ns + "/textures/" + path + ".png";
        if ("israel_simulator".equals(ns)) {
            return resourceExists(entry);
        }
        assertTrue(Files.isRegularFile(VANILLA_JAR), "Vanilla jar missing: " + VANILLA_JAR);
        try (ZipFile zip = new ZipFile(VANILLA_JAR.toFile())) {
            ZipEntry e = zip.getEntry(entry);
            return e != null;
        }
    }

    private void collectTextureRefs(JsonObject model, Set<String> out) {
        if (model.has("textures") && model.get("textures").isJsonObject()) {
            JsonObject textures = model.getAsJsonObject("textures");
            for (var e : textures.entrySet()) {
                if (e.getValue().isJsonPrimitive()) {
                    String ref = e.getValue().getAsString();
                    if (ref.startsWith("#")) {
                        continue; // reference to another texture key
                    }
                    out.add(ref);
                }
            }
        }
        if (model.has("parent") && model.get("parent").isJsonPrimitive()) {
            String parent = model.get("parent").getAsString();
            // Resolve mod parents; vanilla parents are assumed present in the game jar.
            if (parent.startsWith("israel_simulator:")) {
                String path = "assets/israel_simulator/models/"
                        + parent.substring("israel_simulator:".length()) + ".json";
                try {
                    collectTextureRefs(readJsonResource(path), out);
                } catch (Exception ex) {
                    fail("Failed resolving parent model " + parent + ": " + ex.getMessage());
                }
            }
        }
    }

    private Set<String> modelsReferencedByBlockstate(JsonObject blockstate) {
        Set<String> models = new LinkedHashSet<>();
        if (blockstate.has("variants")) {
            JsonObject variants = blockstate.getAsJsonObject("variants");
            for (var e : variants.entrySet()) {
                JsonElement v = e.getValue();
                if (v.isJsonObject() && v.getAsJsonObject().has("model")) {
                    models.add(v.getAsJsonObject().get("model").getAsString());
                } else if (v.isJsonArray()) {
                    for (JsonElement el : v.getAsJsonArray()) {
                        if (el.isJsonObject() && el.getAsJsonObject().has("model")) {
                            models.add(el.getAsJsonObject().get("model").getAsString());
                        }
                    }
                }
            }
        }
        if (blockstate.has("multipart")) {
            for (JsonElement el : blockstate.getAsJsonArray("multipart")) {
                JsonObject apply = el.getAsJsonObject().getAsJsonObject("apply");
                if (apply != null && apply.has("model")) {
                    models.add(apply.get("model").getAsString());
                }
            }
        }
        return models;
    }

    @Test
    @DisplayName("Every registered block has blockstate, models, and resolvable textures")
    void everyRegisteredBlockHasResolvableAssets() throws Exception {
        List<String> missing = new ArrayList<>();
        for (String id : registeredBlockIds()) {
            String bsPath = "assets/israel_simulator/blockstates/" + id + ".json";
            if (!resourceExists(bsPath)) {
                missing.add(id + ": missing blockstate");
                continue;
            }
            JsonObject blockstate = readJsonResource(bsPath);
            Set<String> models = modelsReferencedByBlockstate(blockstate);
            assertFalse(models.isEmpty(), id + " blockstate references no models");

            // Item model should exist for placeable blocks (block items).
            String itemPath = "assets/israel_simulator/models/item/" + id + ".json";
            // grapevine uses wheat models and may only have grapes item — allow missing item model
            // only when blockstate exclusively references minecraft: models.
            boolean onlyVanillaModels = models.stream().allMatch(m -> m.startsWith("minecraft:"));
            if (!onlyVanillaModels) {
                assertTrue(resourceExists(itemPath), id + " missing item model");
            }

            for (String modelId : models) {
                if (modelId.startsWith("minecraft:")) {
                    // Vanilla block models + their textures are provided by the game jar.
                    continue;
                }
                assertTrue(modelId.startsWith("israel_simulator:"),
                        id + " unexpected model namespace: " + modelId);
                String rel = modelId.substring("israel_simulator:".length());
                String modelPath = "assets/israel_simulator/models/" + rel + ".json";
                assertTrue(resourceExists(modelPath), id + " missing model " + modelPath);
                JsonObject model = readJsonResource(modelPath);
                Set<String> textures = new LinkedHashSet<>();
                collectTextureRefs(model, textures);
                for (String tex : textures) {
                    if (!vanillaTextureExists(tex)) {
                        missing.add(id + ": unresolved texture " + tex + " via " + modelId);
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "Unresolved block assets: " + missing);
    }

    @Test
    @DisplayName("Jerusalem stone, salt block, and paved road resolve to valid textures")
    void keySurfaceBlocksUseValidTextures() throws Exception {
        for (String id : List.of("jerusalem_stone", "salt_block", "paved_road")) {
            JsonObject model = readJsonResource("assets/israel_simulator/models/block/" + id + ".json");
            String all = model.getAsJsonObject("textures").get("all").getAsString();
            assertTrue(all.startsWith("minecraft:block/") || all.startsWith("israel_simulator:block/"),
                    id + " should use a valid block texture");
            assertTrue(vanillaTextureExists(all), id + " texture missing: " + all);
            assertTrue(resourceExists("assets/israel_simulator/blockstates/" + id + ".json"));
            assertTrue(resourceExists("assets/israel_simulator/models/item/" + id + ".json"));
        }
    }
}
