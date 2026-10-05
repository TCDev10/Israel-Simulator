package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonOverworldBiomeTagTest {

    private static final List<String> OTHER_ISRAEL_BIOMES = List.of(
            "israel_simulator:mediterranean_coast",
            "israel_simulator:israeli_agriculture",
            "israel_simulator:judean_desert",
            "israel_simulator:dead_sea",
            "israel_simulator:urban_area"
    );

    private static final String JERUSALEM = "israel_simulator:jerusalem";

    @Test
    @DisplayName("c:is_overworld includes jerusalem when the other Israel biomes are listed")
    void jerusalemIsListedWithTheOtherIsraelBiomes() throws Exception {
        List<String> values = readOverworldTagValues();
        boolean othersPresent = values.containsAll(OTHER_ISRAEL_BIOMES);
        assertTrue(othersPresent, "c:is_overworld must keep the other Israel biomes: " + values);
        assertFalse(
                othersPresent && !values.contains(JERUSALEM),
                "c:is_overworld lists the other Israel biomes but omits " + JERUSALEM
        );
    }

    private static List<String> readOverworldTagValues() throws Exception {
        String path = "/data/c/tags/worldgen/biome/is_overworld.json";
        InputStream is = CommonOverworldBiomeTagTest.class.getResourceAsStream(path);
        assertNotNull(is, "Missing biome tag resource: " + path);
        JsonObject tag = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
        JsonArray values = tag.getAsJsonArray("values");
        assertNotNull(values, "c:is_overworld has no values array");
        List<String> ids = new ArrayList<>();
        for (JsonElement element : values) {
            if (element.isJsonPrimitive()) {
                ids.add(element.getAsString());
            } else if (element.isJsonObject() && element.getAsJsonObject().has("id")) {
                ids.add(element.getAsJsonObject().get("id").getAsString());
            }
        }
        return ids;
    }
}
