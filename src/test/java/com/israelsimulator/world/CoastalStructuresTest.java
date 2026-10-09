package com.israelsimulator.world;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoastalStructuresTest {
    @Test
    @DisplayName("jaffa_port, mediterranean_village are no longer tiny placeholders")
    void notPlaceholders() throws Exception {
        for (String name : new String[]{"jaffa_port", "mediterranean_village"}) {
            String path = "data/israel_simulator/structure/" + name + ".nbt";
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(in, path);
                byte[] data = new GZIPInputStream(in).readAllBytes();
                assertTrue(data.length > 2000, name + " too small: " + data.length);
            }
        }
    }
}
