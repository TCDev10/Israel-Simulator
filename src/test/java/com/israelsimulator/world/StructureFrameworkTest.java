package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.israelsimulator.world.structure.ModStructures;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructureFrameworkTest {

    @Test
    @DisplayName("Verify all structure keys are registered with israel_simulator namespace")
    void testStructureKeys() {
        List<ResourceKey<Structure>> structures = ModStructures.allStructures();
        assertNotNull(structures);
        assertEquals(16, structures.size(), "Should register 16 structures in framework");

        for (ResourceKey<Structure> structure : structures) {
            assertEquals("israel_simulator", structure.identifier().getNamespace());
            assertFalse(structure.identifier().getPath().isBlank());
        }
    }

    @Test
    @DisplayName("Verify chest loot tables are accessible on the classpath")
    void testChestLootTablesExist() {
        List<String> lootTables = List.of(
                "mediterranean_village",
                "mediterranean_bakery",
                "mediterranean_fisherman",
                "agricultural_farm",
                "dead_sea_resort",
                "desert_ruins",
                "synagogue",
                "synagogue_ark",
                "tel_aviv_tech_office",
                "tel_aviv_apartment",
                "tel_aviv_kiosk",
                "ein_gedi_oasis",
                "jaffa_flea_market",
                "jerusalem_bazaar",
                "western_wall_treasury"
        );

        for (String name : lootTables) {
            String path = "data/israel_simulator/loot_table/chests/" + name + ".json";
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            assertNotNull(stream, "Loot table resource must exist at: " + path);
        }
    }

    @Test
    @DisplayName("No structure JSON uses a vanilla village start pool")
    void testNoStructureUsesVanillaVillagePool() throws Exception {
        Map<String, String> markerBlocks = Map.ofEntries(
                Map.entry("tel_aviv_city", "minecraft:light_blue_stained_glass"),
                Map.entry("jaffa_port", "minecraft:prismarine_bricks"),
                Map.entry("jerusalem_city", "minecraft:yellow_terracotta"),
                Map.entry("western_wall", "minecraft:calcite"),
                Map.entry("synagogue", "minecraft:purple_stained_glass"),
                Map.entry("government_building", "minecraft:polished_deepslate"),
                Map.entry("ancient_sanctuary", "minecraft:chiseled_sandstone"),
                Map.entry("grand_market", "minecraft:red_terracotta"),
                Map.entry("startup_office", "minecraft:iron_block"),
                Map.entry("agricultural_farm", "minecraft:hay_block"),
                Map.entry("great_synagogue", "minecraft:blue_stained_glass"),
                Map.entry("dead_sea_resort", "minecraft:packed_mud"),
                Map.entry("desert_ruins", "minecraft:cracked_stone_bricks"),
                Map.entry("historical_house", "minecraft:bricks"),
                Map.entry("mediterranean_village", "minecraft:light_gray_terracotta"),
                Map.entry("ein_gedi_oasis", "minecraft:moss_block")
        );
        Gson gson = new Gson();
        java.net.URL structuresDir = getClass().getClassLoader()
                .getResource("data/israel_simulator/worldgen/structure");
        assertNotNull(structuresDir, "Structure JSON directory must be on the classpath");
        java.nio.file.Path dir = java.nio.file.Paths.get(structuresDir.toURI());
        java.util.List<java.nio.file.Path> structureFiles;
        try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(dir)) {
            structureFiles = files.filter(path -> path.getFileName().toString().endsWith(".json")).toList();
        }
        assertFalse(structureFiles.isEmpty(), "Expected structure JSON files");

        for (java.nio.file.Path structureFile : structureFiles) {
            String name = structureFile.getFileName().toString().replace(".json", "");
            JsonObject structure = gson.fromJson(
                    java.nio.file.Files.newBufferedReader(structureFile, StandardCharsets.UTF_8),
                    JsonObject.class);
            assertTrue(structure.has("start_pool"), name + " must define start_pool");
            String startPool = structure.get("start_pool").getAsString();
            assertFalse(startPool.contains("minecraft:village"),
                    name + " start_pool must not reference a vanilla village pool, was: " + startPool);
            assertEquals("israel_simulator:" + name, startPool,
                    name + " must start from its own israel_simulator template pool");
            assertTrue(markerBlocks.containsKey(name),
                    name + " must declare a marker block for its template");

            String poolPath = "data/israel_simulator/worldgen/template_pool/" + name + ".json";
            InputStream poolStream = getClass().getClassLoader().getResourceAsStream(poolPath);
            assertNotNull(poolStream, "Missing template pool: " + poolPath);
            JsonObject pool = gson.fromJson(new InputStreamReader(poolStream, StandardCharsets.UTF_8), JsonObject.class);
            String location = pool.getAsJsonArray("elements").get(0).getAsJsonObject()
                    .getAsJsonObject("element").get("location").getAsString();
            assertTrue(location.startsWith("israel_simulator:"),
                    name + " pool must place a mod structure template, was: " + location);
            assertFalse(location.contains("minecraft:village"),
                    name + " pool location must not be a vanilla village piece");
            // Single-piece structures keep location == israel_simulator:<name>.
            // Jerusalem is a multi-piece jigsaw that starts at jerusalem/plaza.
            if ("jerusalem_city".equals(name)) {
                assertEquals("israel_simulator:jerusalem/plaza", location,
                        "jerusalem_city must start from the plaza piece");
            } else if ("mediterranean_village".equals(name)) {
                assertEquals("israel_simulator:mediterranean/square", location,
                        "mediterranean_village must start from the village square piece");
            } else if ("tel_aviv_city".equals(name)) {
                assertEquals("israel_simulator:tel_aviv/square", location,
                        "tel_aviv_city must start from the Dizengoff square piece");
            } else {
                assertEquals("israel_simulator:" + name, location,
                        name + " pool must place this mod's structure template");
            }

            // Marker NBT: single-piece templates use structure/<name>.nbt.
            // Jerusalem also keeps a small root marker file for the yellow terracotta check,
            // and the live plaza piece carries the same marker.
            // Tel Aviv is multi-piece too; its marker lives in the start square's fountain.
            String nbtPath = switch (name) {
                case "jerusalem_city" -> "data/israel_simulator/structure/jerusalem/plaza.nbt";
                case "tel_aviv_city" -> "data/israel_simulator/structure/tel_aviv/square.nbt";
                case "mediterranean_village" -> "data/israel_simulator/structure/mediterranean/square.nbt";
                default -> "data/israel_simulator/structure/" + name + ".nbt";
            };
            InputStream nbtStream = getClass().getClassLoader().getResourceAsStream(nbtPath);
            assertNotNull(nbtStream, "Missing structure template NBT: " + nbtPath);
            byte[] nbt = new GZIPInputStream(nbtStream).readAllBytes();
            String decoded = new String(nbt, StandardCharsets.ISO_8859_1);
            assertTrue(decoded.contains(markerBlocks.get(name)),
                    name + " template must place its own marker block " + markerBlocks.get(name));
            assertTrue(decoded.contains("DataVersion"), name + " template must be a structure NBT file");
            if ("jerusalem_city".equals(name)) {
                InputStream rootMarker = getClass().getClassLoader()
                        .getResourceAsStream("data/israel_simulator/structure/jerusalem_city.nbt");
                assertNotNull(rootMarker, "Root jerusalem_city.nbt marker template must remain");
            }
        }
    }
}
