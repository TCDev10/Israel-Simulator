package com.israelsimulator.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the datapack override of {@code minecraft:worldgen/noise_settings}
 * that injects Israel-Simulator biome surfaces.
 */
class IsraelBiomeSurfaceRulesTest {

    private static final List<String> NOISE_SETTINGS = List.of(
            "overworld.json",
            "amplified.json",
            "large_biomes.json"
    );

    /** Biomes that receive custom surface rules (must match generator SURFACE_SPECS). */
    private static final List<String> CUSTOM_SURFACE_BIOMES = List.of(
            "jerusalem",
            "judean_desert",
            "dead_sea",
            "mediterranean_coast",
            "tropical_island",
            "urban_area"
    );

    private static final String AGRICULTURE = "israeli_agriculture";

    private static final Path VANILLA_JAR = Path.of(
            "build/moddev/artifacts/minecraft-patched-26.2.0.88.jar");

    private JsonObject loadOverride(String fileName) throws Exception {
        String path = "data/minecraft/worldgen/noise_settings/" + fileName;
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, "Missing noise_settings override: " + path);
        return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private JsonObject loadVanillaFromJar(String fileName) throws Exception {
        assertTrue(Files.isRegularFile(VANILLA_JAR),
                "Vanilla jar missing (run createMinecraftArtifacts): " + VANILLA_JAR);
        String entryName = "data/minecraft/worldgen/noise_settings/" + fileName;
        try (ZipFile zip = new ZipFile(VANILLA_JAR.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            assertNotNull(entry, "Jar missing " + entryName);
            try (InputStream in = zip.getInputStream(entry)) {
                return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            }
        }
    }

    private JsonObject findAbovePreliminary(JsonObject noiseSettings) {
        JsonObject surface = noiseSettings.getAsJsonObject("surface_rule");
        assertEquals("minecraft:sequence", surface.get("type").getAsString());
        for (JsonElement el : surface.getAsJsonArray("sequence")) {
            JsonObject node = el.getAsJsonObject();
            JsonObject ifTrue = node.getAsJsonObject("if_true");
            if ("minecraft:above_preliminary_surface".equals(ifTrue.get("type").getAsString())) {
                return node;
            }
        }
        fail("above_preliminary_surface branch not found");
        return null;
    }

    private JsonArray aboveSequence(JsonObject noiseSettings) {
        JsonObject above = findAbovePreliminary(noiseSettings);
        JsonObject then = above.getAsJsonObject("then_run");
        assertEquals("minecraft:sequence", then.get("type").getAsString());
        return then.getAsJsonArray("sequence");
    }

    @Test
    @DisplayName("Noise settings overrides parse and keep vanilla non-surface fields")
    void overridesParseAndPreserveNonSurfaceFields() throws Exception {
        for (String name : NOISE_SETTINGS) {
            JsonObject ours = loadOverride(name);
            JsonObject vanilla = loadVanillaFromJar(name);
            assertEquals(vanilla.keySet(), ours.keySet(), name + " top-level keys");
            for (String key : vanilla.keySet()) {
                if ("surface_rule".equals(key)) {
                    continue;
                }
                assertEquals(vanilla.get(key), ours.get(key),
                        name + " field must match vanilla: " + key);
            }
            assertNotNull(ours.get("surface_rule"));
            assertEquals("minecraft:sequence",
                    ours.getAsJsonObject("surface_rule").get("type").getAsString());
        }
    }

    @Test
    @DisplayName("Above-preliminary sequence keeps vanilla rules after Israeli prepend")
    void vanillaAbovePreliminaryRulesUnchanged() throws Exception {
        for (String name : NOISE_SETTINGS) {
            JsonArray ours = aboveSequence(loadOverride(name));
            JsonArray vanilla = aboveSequence(loadVanillaFromJar(name));
            assertEquals(vanilla.size() + 2, ours.size(),
                    name + " should prepend exactly two Israeli rules");
            for (int i = 0; i < vanilla.size(); i++) {
                assertEquals(vanilla.get(i), ours.get(i + 2),
                        name + " vanilla rule [" + i + "] must be unchanged");
            }
        }
    }

    @Test
    @DisplayName("Each Israeli biome with a custom surface is present; agriculture stays vanilla")
    void israeliBiomeSurfaceRulesPresent() throws Exception {
        Set<String> biomeFiles;
        try (var stream = Files.list(Path.of("src/main/resources/data/israel_simulator/worldgen/biome"))) {
            biomeFiles = stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .collect(java.util.stream.Collectors.toSet());
        }
        assertTrue(biomeFiles.containsAll(CUSTOM_SURFACE_BIOMES));
        assertTrue(biomeFiles.contains(AGRICULTURE));

        JsonObject overworld = loadOverride("overworld.json");
        String injected = aboveSequence(overworld).get(0).toString()
                + aboveSequence(overworld).get(1).toString();

        for (String biome : CUSTOM_SURFACE_BIOMES) {
            assertTrue(injected.contains("israel_simulator:" + biome),
                    "Missing surface rule for biome " + biome);
        }
        assertFalse(injected.contains("israel_simulator:" + AGRICULTURE),
                "Agriculture should keep vanilla grass/dirt (no injected rule)");

        assertTrue(injected.contains("israel_simulator:jerusalem_stone"));
        assertTrue(injected.contains("israel_simulator:salt_block"));
        assertTrue(injected.contains("minecraft:red_sand"));
        assertTrue(injected.contains("minecraft:sand"));
    }

    @Test
    @DisplayName("All israel_simulator biome JSON files are listed for surface coverage decisions")
    void allBiomesAccountedForInSurfacePlan() throws Exception {
        List<String> biomes = new ArrayList<>();
        try (var stream = Files.list(Path.of("src/main/resources/data/israel_simulator/worldgen/biome"))) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .sorted()
                    .forEach(biomes::add);
        }
        assertEquals(
                List.of("dead_sea", "israeli_agriculture", "jerusalem",
                        "judean_desert", "mediterranean_coast", "tropical_island", "urban_area"),
                biomes);
        for (String biome : biomes) {
            if (AGRICULTURE.equals(biome)) {
                continue;
            }
            assertTrue(CUSTOM_SURFACE_BIOMES.contains(biome),
                    "Biome " + biome + " needs a SURFACE_SPECS entry or explicit vanilla-keep");
        }
    }
}
