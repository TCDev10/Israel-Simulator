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
                "agricultural_farm",
                "dead_sea_resort",
                "desert_ruins",
                "synagogue",
                "synagogue_ark",
                "tel_aviv_tech_office",
                "tel_aviv_apartment",
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
    @DisplayName("City structures start from mod template pools, not vanilla villages")
    void testCityStructuresDoNotUseVanillaVillagePools() throws Exception {
        Map<String, String> markerBlocks = Map.of(
                "tel_aviv_city", "minecraft:light_blue_stained_glass",
                "jaffa_port", "minecraft:prismarine_bricks",
                "jerusalem_city", "minecraft:yellow_terracotta",
                "western_wall", "minecraft:calcite"
        );
        Gson gson = new Gson();

        for (Map.Entry<String, String> city : markerBlocks.entrySet()) {
            String name = city.getKey();
            String structurePath = "data/israel_simulator/worldgen/structure/" + name + ".json";
            InputStream structureStream = getClass().getClassLoader().getResourceAsStream(structurePath);
            assertNotNull(structureStream, "Missing structure JSON: " + structurePath);
            JsonObject structure = gson.fromJson(new InputStreamReader(structureStream, StandardCharsets.UTF_8), JsonObject.class);
            assertTrue(structure.has("start_pool"), name + " must define start_pool");
            String startPool = structure.get("start_pool").getAsString();
            assertFalse(startPool.contains("minecraft:village"),
                    name + " start_pool must not reference a vanilla village pool, was: " + startPool);
            assertEquals("israel_simulator:" + name, startPool,
                    name + " must start from its own israel_simulator template pool");

            String poolPath = "data/israel_simulator/worldgen/template_pool/" + name + ".json";
            InputStream poolStream = getClass().getClassLoader().getResourceAsStream(poolPath);
            assertNotNull(poolStream, "Missing template pool: " + poolPath);
            JsonObject pool = gson.fromJson(new InputStreamReader(poolStream, StandardCharsets.UTF_8), JsonObject.class);
            String location = pool.getAsJsonArray("elements").get(0).getAsJsonObject()
                    .getAsJsonObject("element").get("location").getAsString();
            assertEquals("israel_simulator:" + name, location,
                    name + " pool must place this mod's structure template");
            assertFalse(location.contains("minecraft:village"),
                    name + " pool location must not be a vanilla village piece");

            String nbtPath = "data/israel_simulator/structure/" + name + ".nbt";
            InputStream nbtStream = getClass().getClassLoader().getResourceAsStream(nbtPath);
            assertNotNull(nbtStream, "Missing structure template NBT: " + nbtPath);
            byte[] nbt = new GZIPInputStream(nbtStream).readAllBytes();
            String decoded = new String(nbt, StandardCharsets.ISO_8859_1);
            assertTrue(decoded.contains(city.getValue()),
                    name + " template must place its own marker block " + city.getValue());
            assertTrue(decoded.contains("DataVersion"), name + " template must be a structure NBT file");
        }
    }
}
