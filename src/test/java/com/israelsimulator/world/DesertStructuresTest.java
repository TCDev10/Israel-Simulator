package com.israelsimulator.world;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DesertStructuresTest {
    @Test
    @DisplayName("desert_ruins, ein_gedi_oasis, dead_sea_resort NBT are no longer tiny placeholders")
    void notPlaceholders() throws Exception {
        for (String name : new String[]{"desert_ruins", "ein_gedi_oasis", "dead_sea_resort"}) {
            String path = "data/israel_simulator/structure/" + name + ".nbt";
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(in, path);
                byte[] data = new GZIPInputStream(in).readAllBytes();
                assertTrue(data.length > 2000, name + " too small: " + data.length);
                String asText = new String(data, java.nio.charset.StandardCharsets.ISO_8859_1);
                assertTrue(asText.contains("LootTable") || asText.contains("israel_simulator:chests/"),
                        name + " missing loot table tag");
            }
        }
    }
}
