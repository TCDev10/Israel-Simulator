package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every mod structure spawns only in its dedicated Israeli biome.
 */
class StructureBiomeLockTest {

    private static final Map<String, String> EXPECTED = new LinkedHashMap<>();

    static {
        EXPECTED.put("jerusalem_city", "israel_simulator:jerusalem");
        EXPECTED.put("western_wall", "israel_simulator:jerusalem");
        EXPECTED.put("synagogue", "israel_simulator:jerusalem");
        EXPECTED.put("great_synagogue", "israel_simulator:jerusalem");
        EXPECTED.put("historical_house", "israel_simulator:jerusalem");
        EXPECTED.put("grand_market", "israel_simulator:jerusalem");
        EXPECTED.put("tel_aviv_city", "israel_simulator:urban_area");
        EXPECTED.put("startup_office", "israel_simulator:urban_area");
        EXPECTED.put("government_building", "israel_simulator:urban_area");
        EXPECTED.put("jaffa_port", "israel_simulator:mediterranean_coast");
        EXPECTED.put("mediterranean_village", "israel_simulator:mediterranean_coast");
        EXPECTED.put("agricultural_farm", "israel_simulator:israeli_agriculture");
        EXPECTED.put("dead_sea_resort", "israel_simulator:dead_sea");
        EXPECTED.put("desert_ruins", "israel_simulator:judean_desert");
        EXPECTED.put("ein_gedi_oasis", "israel_simulator:judean_desert");
        EXPECTED.put("ancient_sanctuary", "israel_simulator:judean_desert");
        EXPECTED.put("island_temple", "israel_simulator:mediterranean_coast");
    }

    @Test
    @DisplayName("Each of the 17 structures is locked to exactly one Israeli biome")
    void eachStructureSingleIsraeliBiome() throws Exception {
        Gson gson = new Gson();
        assertEquals(17, EXPECTED.size());
        for (Map.Entry<String, String> e : EXPECTED.entrySet()) {
            String path = "/data/israel_simulator/worldgen/structure/" + e.getKey() + ".json";
            InputStream in = getClass().getResourceAsStream(path);
            assertNotNull(in, "missing " + path);
            JsonObject json = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            JsonElement biomes = json.get("biomes");
            assertNotNull(biomes, e.getKey() + " missing biomes");
            if (biomes.isJsonArray()) {
                JsonArray arr = biomes.getAsJsonArray();
                assertEquals(1, arr.size(), e.getKey() + " must list exactly one biome: " + arr);
                assertEquals(e.getValue(), arr.get(0).getAsString(), e.getKey());
            } else {
                assertEquals(e.getValue(), biomes.getAsString(), e.getKey());
            }
            assertFalse(biomes.toString().contains("minecraft:"),
                    e.getKey() + " must not include vanilla biomes: " + biomes);
            assertFalse(biomes.toString().contains("is_israel_region"),
                    e.getKey() + " must not use the broad is_israel_region tag");
        }
    }

    @Test
    @DisplayName("is_israel_region lists only the six Israeli biomes")
    void israelRegionTagIsraeliOnly() throws Exception {
        Gson gson = new Gson();
        InputStream in = getClass().getResourceAsStream(
                "/data/israel_simulator/tags/worldgen/biome/is_israel_region.json");
        assertNotNull(in);
        JsonObject json = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
        JsonArray values = json.getAsJsonArray("values");
        assertEquals(6, values.size(), values.toString());
        for (JsonElement el : values) {
            String id = el.getAsString();
            assertTrue(id.startsWith("israel_simulator:"), "vanilla biome still in tag: " + id);
            assertFalse(id.startsWith("minecraft:"));
        }
    }
}
