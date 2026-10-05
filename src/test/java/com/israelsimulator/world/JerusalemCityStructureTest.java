package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jerusalem Old City is a multi-piece jigsaw village, not a single 7x7 shell.
 */
class JerusalemCityStructureTest {

    private static final Gson GSON = new Gson();

    private static final List<String> PLAZA_AND_STREETS = List.of(
            "jerusalem/plaza",
            "jerusalem/street_straight",
            "jerusalem/street_crossroad",
            "jerusalem/street_corner",
            "jerusalem/terminator"
    );

    private static final List<String> BUILDINGS = List.of(
            "jerusalem/house_a",
            "jerusalem/house_b",
            "jerusalem/house_c",
            "jerusalem/shuk_stalls",
            "jerusalem/synagogue_small",
            "jerusalem/well",
            "jerusalem/wall_gate"
    );

    private static final List<String> POOLS = List.of(
            "jerusalem_city",
            "jerusalem/streets",
            "jerusalem/terminators",
            "jerusalem/buildings"
    );

    @Test
    @DisplayName("Jerusalem start pool places the plaza piece, not a vanilla village")
    void startPoolUsesPlaza() throws Exception {
        JsonObject pool = readJson("data/israel_simulator/worldgen/template_pool/jerusalem_city.json");
        JsonArray elements = pool.getAsJsonArray("elements");
        assertFalse(elements.isEmpty());
        String location = elements.get(0).getAsJsonObject()
                .getAsJsonObject("element").get("location").getAsString();
        assertEquals("israel_simulator:jerusalem/plaza", location);
        assertFalse(location.contains("minecraft:village"));
    }

    @Test
    @DisplayName("Jerusalem template pools and NBT pieces exist and parse")
    void poolsAndPiecesExist() throws Exception {
        for (String poolName : POOLS) {
            String path = "data/israel_simulator/worldgen/template_pool/" + poolName + ".json";
            JsonObject pool = readJson(path);
            assertTrue(pool.has("elements"), path + " must have elements");
            assertTrue(pool.has("fallback"), path + " must have fallback");
            JsonArray elements = pool.getAsJsonArray("elements");
            assertFalse(elements.isEmpty(), path + " must list at least one element");
            for (int i = 0; i < elements.size(); i++) {
                JsonObject element = elements.get(i).getAsJsonObject().getAsJsonObject("element");
                String location = element.get("location").getAsString();
                assertTrue(location.startsWith("israel_simulator:"),
                        "Pool piece must be mod-owned: " + location);
                assertFalse(location.contains("minecraft:village"),
                        "Must not use vanilla village piece: " + location);
                String nbtPath = "data/israel_simulator/structure/"
                        + location.substring("israel_simulator:".length()) + ".nbt";
                assertNbtParses(nbtPath);
            }
        }

        for (String piece : PLAZA_AND_STREETS) {
            assertNbtParses("data/israel_simulator/structure/" + piece + ".nbt");
        }
        for (String piece : BUILDINGS) {
            assertNbtParses("data/israel_simulator/structure/" + piece + ".nbt");
        }
    }

    @Test
    @DisplayName("Jerusalem plaza keeps the yellow terracotta marker")
    void plazaHasMarker() throws Exception {
        byte[] nbt = readGzip("data/israel_simulator/structure/jerusalem/plaza.nbt");
        String decoded = new String(nbt, StandardCharsets.ISO_8859_1);
        assertTrue(decoded.contains("minecraft:yellow_terracotta"),
                "plaza must keep the jerusalem_city marker block");
        assertTrue(decoded.contains("DataVersion"));
        assertTrue(decoded.contains("minecraft:jigsaw"), "plaza must expose street jigsaws");
    }

    @Test
    @DisplayName("Jerusalem buildings include house, shuk, synagogue, well and wall gate")
    void buildingsPoolCoversDistinctTypes() throws Exception {
        JsonObject pool = readJson("data/israel_simulator/worldgen/template_pool/jerusalem/buildings.json");
        String blob = pool.toString();
        for (String required : List.of(
                "house_a", "house_b", "house_c",
                "shuk_stalls", "synagogue_small", "well", "wall_gate")) {
            assertTrue(blob.contains(required), "buildings pool must include " + required);
        }
    }

    private JsonObject readJson(String path) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, "Missing resource: " + path);
        return GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
    }

    private void assertNbtParses(String path) throws Exception {
        byte[] nbt = readGzip(path);
        assertTrue(nbt.length > 16, path + " must not be empty");
        String decoded = new String(nbt, StandardCharsets.ISO_8859_1);
        assertTrue(decoded.contains("DataVersion"), path + " must be a structure NBT");
        assertFalse(decoded.contains("minecraft:village/"),
                path + " must not embed vanilla village references in payload unexpectedly");
    }

    private byte[] readGzip(String path) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, "Missing NBT: " + path);
        try (GZIPInputStream gzip = new GZIPInputStream(stream)) {
            return gzip.readAllBytes();
        }
    }
}
