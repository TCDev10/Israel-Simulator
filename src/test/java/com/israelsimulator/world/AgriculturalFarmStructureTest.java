package com.israelsimulator.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgriculturalFarmStructureTest {

    @Test
    @DisplayName("agricultural_farm is biome-locked and uses beard_box")
    void structureJson() throws Exception {
        JsonObject o = readJson("data/israel_simulator/worldgen/structure/agricultural_farm.json");
        assertEquals("israel_simulator:israeli_agriculture", o.getAsJsonArray("biomes").get(0).getAsString());
        assertEquals("beard_box", o.get("terrain_adaptation").getAsString());
    }

    @Test
    @DisplayName("farm NBT has fields, greenhouse glass, shed chest with loot")
    void nbtContents() throws Exception {
        Map<String, Object> root = loadNbt("data/israel_simulator/structure/agricultural_farm.nbt");
        @SuppressWarnings("unchecked")
        List<Integer> size = (List<Integer>) root.get("size");
        assertEquals(List.of(48, 10, 40), size);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> palette = (List<Map<String, Object>>) root.get("palette");
        Set<String> names = new HashSet<>();
        for (Map<String, Object> e : palette) {
            names.add((String) e.get("Name"));
        }
        assertTrue(names.contains("minecraft:farmland"));
        assertTrue(names.contains("minecraft:wheat") || names.contains("israel_simulator:grapevine"));
        assertTrue(names.contains("minecraft:glass") || names.contains("minecraft:glass_pane"));
        assertTrue(names.contains("minecraft:oak_fence"));
        assertTrue(names.contains("minecraft:chest"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> blocks = (List<Map<String, Object>>) root.get("blocks");
        assertTrue(blocks.size() > 1000);

        int chests = 0;
        String loot = null;
        for (Map<String, Object> b : blocks) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nbt = (Map<String, Object>) b.get("nbt");
            if (nbt != null && nbt.containsKey("LootTable")) {
                chests++;
                loot = (String) nbt.get("LootTable");
            }
        }
        assertEquals(1, chests);
        assertEquals("israel_simulator:chests/agricultural_farm", loot);
    }

    private JsonObject readJson(String path) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, path);
            return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private Map<String, Object> loadNbt(String path) throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            try (GZIPInputStream gzip = new GZIPInputStream(stream);
                 DataInputStream in = new DataInputStream(new ByteArrayInputStream(gzip.readAllBytes()))) {
                assertEquals(10, in.readByte());
                readString(in);
                return readCompound(in);
            }
        }
    }

    private static Map<String, Object> readCompound(DataInputStream in) throws IOException {
        Map<String, Object> map = new HashMap<>();
        while (true) {
            byte type = in.readByte();
            if (type == 0) {
                return map;
            }
            map.put(readString(in), readPayload(in, type));
        }
    }

    private static Object readPayload(DataInputStream in, byte type) throws IOException {
        return switch (type) {
            case 1 -> in.readByte();
            case 2 -> in.readShort();
            case 3 -> in.readInt();
            case 4 -> in.readLong();
            case 5 -> in.readFloat();
            case 6 -> in.readDouble();
            case 7 -> {
                int len = in.readInt();
                in.skipBytes(len);
                yield new byte[0];
            }
            case 8 -> readString(in);
            case 9 -> {
                byte listType = in.readByte();
                int len = in.readInt();
                List<Object> list = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    list.add(readPayload(in, listType));
                }
                yield list;
            }
            case 10 -> readCompound(in);
            case 11 -> {
                int len = in.readInt();
                List<Integer> list = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    list.add(in.readInt());
                }
                yield list;
            }
            case 12 -> {
                int len = in.readInt();
                for (int i = 0; i < len; i++) {
                    in.readLong();
                }
                yield List.of();
            }
            default -> throw new IOException("Unknown NBT type " + type);
        };
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        byte[] data = in.readNBytes(len);
        return new String(data, StandardCharsets.UTF_8);
    }
}
