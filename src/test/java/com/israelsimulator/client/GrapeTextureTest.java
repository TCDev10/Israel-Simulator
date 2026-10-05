package com.israelsimulator.client;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrapeTextureTest {
    @Test
    @DisplayName("grapevine stages and grapes item are 16x16 PNGs under the mod namespace")
    void texturesPresentAndSized() throws Exception {
        for (int i = 0; i <= 7; i++) {
            assertPng16("assets/israel_simulator/textures/block/grapevine_stage" + i + ".png");
        }
        assertPng16("assets/israel_simulator/textures/item/grapes.png");
    }

    private void assertPng16(String path) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, "missing " + path);
            byte[] header = in.readNBytes(24);
            assertEquals(0x89, header[0] & 0xFF);
            assertEquals('P', header[1]);
            int w = ByteBuffer.wrap(header, 16, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            int h = ByteBuffer.wrap(header, 20, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            assertEquals(16, w, path + " width");
            assertEquals(16, h, path + " height");
        }
    }
}
