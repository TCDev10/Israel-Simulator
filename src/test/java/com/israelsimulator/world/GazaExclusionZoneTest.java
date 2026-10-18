package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jaffa and the Mediterranean village share the mediterranean_coast biome with the big Gaza Strip
 * jigsaw. Their structure sets keep an exclusion zone around Gaza's placement grid so the three
 * can never overlap: jigsaw starts sit on chunk corners and every piece stays within
 * max_distance_from_center, so two structures can only touch if their start chunks are closer
 * than (gazaRadius + ownRadius) / 16 chunks.
 */
class GazaExclusionZoneTest {

    private static final Gson GSON = new Gson();
    private static final Map<String, String> SETS = Map.of(
            "jaffa_ports", "jaffa_port",
            "mediterranean_villages", "mediterranean_village");

    @Test
    @DisplayName("Jaffa and the Mediterranean village keep a no-overlap exclusion zone around Gaza")
    void exclusionZoneCoversBothRadii() throws Exception {
        int gaza = read("structure/gaza_strip.json").get("max_distance_from_center").getAsInt();
        for (Map.Entry<String, String> e : SETS.entrySet()) {
            JsonObject placement = read("structure_set/" + e.getKey() + ".json").getAsJsonObject("placement");
            assertTrue(placement.has("exclusion_zone"), e.getKey() + " needs an exclusion_zone against Gaza");
            JsonObject zone = placement.getAsJsonObject("exclusion_zone");
            assertEquals("israel_simulator:gaza_strips", zone.get("other_set").getAsString(), e.getKey());
            int chunks = zone.get("chunk_count").getAsInt();
            assertTrue(chunks >= 1 && chunks <= 16, "vanilla limit 1..16, was " + chunks);
            int own = read("structure/" + e.getValue() + ".json").get("max_distance_from_center").getAsInt();
            int needed = (int) Math.ceil((gaza + own) / 16.0);
            assertTrue(chunks >= needed, e.getKey() + " chunk_count " + chunks + " < " + needed
                    + " chunks needed for Gaza radius " + gaza + " + own radius " + own);
        }
        assertNotNull(read("structure_set/gaza_strips.json"), "the excluded set must exist");
    }

    private JsonObject read(String path) throws Exception {
        String full = "data/israel_simulator/worldgen/" + path;
        InputStream in = getClass().getClassLoader().getResourceAsStream(full);
        assertNotNull(in, "Missing " + full);
        return GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
    }
}
