package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Western Wall (one 64x26x48 template, no jigsaw children) must never overlap the Grand Market
 * or Tel Aviv. Their structure sets carry an exclusion_zone around the Western Wall placement grid.
 *
 * <p>Reach of a structure from its start chunk corner, per axis (JigsawPlacement 26.2): the start
 * template is rotated around the chunk corner and the max-distance box is centred on the start
 * piece's centre, so reach = max_distance_from_center + ceil((longest start side - 1) / 2).
 * A single template without jigsaw blocks only reaches (longest side - 1). An exclusion zone of
 * n chunks keeps the other start at least n + 1 chunks away on some axis, so overlap is impossible
 * when 16 * (n + 1) > reachA + reachB.
 */
class WesternWallExclusionZoneTest {

    private static final Gson GSON = new Gson();
    private static final Map<String, String> SETS = Map.of(
            "grand_markets", "grand_market",
            "tel_aviv_cities", "tel_aviv_city");

    @Test
    @DisplayName("Grand Market and Tel Aviv keep a no-overlap exclusion zone around the Western Wall")
    void exclusionZoneCoversBothReaches() throws Exception {
        int wall = reach("western_wall");
        assertTrue(wall >= 63, "Western Wall template is 64 blocks long, reach " + wall);
        for (Map.Entry<String, String> e : SETS.entrySet()) {
            JsonObject placement = json("worldgen/structure_set/" + e.getKey() + ".json").getAsJsonObject("placement");
            assertTrue(placement.has("exclusion_zone"), e.getKey() + " needs an exclusion_zone against the Western Wall");
            JsonObject zone = placement.getAsJsonObject("exclusion_zone");
            assertEquals("israel_simulator:western_walls", zone.get("other_set").getAsString(), e.getKey());
            int n = zone.get("chunk_count").getAsInt();
            assertTrue(n >= 1 && n <= 16, "vanilla limit 1..16, was " + n);
            int other = reach(e.getValue());
            assertTrue(16 * (n + 1) > wall + other, e.getKey() + " chunk_count " + n + " too small: reaches "
                    + wall + " + " + other + " need at least " + ((wall + other) / 16));
        }
        assertNotNull(json("worldgen/structure_set/western_walls.json"));
    }

    @SuppressWarnings("unchecked")
    private int reach(String structure) throws Exception {
        JsonObject s = json("worldgen/structure/" + structure + ".json");
        JsonObject pool = json("worldgen/template_pool/" + s.get("start_pool").getAsString().split(":")[1] + ".json");
        String loc = pool.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("element")
                .get("location").getAsString().split(":")[1];
        Map<String, Object> root = nbt("structure/" + loc + ".nbt");
        List<Integer> size = (List<Integer>) root.get("size");
        int longest = Math.max(size.get(0), size.get(2));
        List<Map<String, Object>> palette = (List<Map<String, Object>>) root.get("palette");
        boolean jigsaw = palette.stream().anyMatch(p -> "minecraft:jigsaw".equals(p.get("Name")));
        if (!jigsaw || s.get("size").getAsInt() == 0) {
            return longest - 1;
        }
        return s.get("max_distance_from_center").getAsInt() + (longest - 1 + 1) / 2;
    }

    private JsonObject json(String path) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("data/israel_simulator/" + path);
        assertNotNull(in, "Missing " + path);
        return GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
    }

    private Map<String, Object> nbt(String path) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("data/israel_simulator/" + path);
        assertNotNull(in, "Missing " + path);
        byte[] raw;
        try (GZIPInputStream gz = new GZIPInputStream(in)) {
            raw = gz.readAllBytes();
        }
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(raw));
        assertEquals(10, d.readByte());
        readString(d);
        return readCompound(d);
    }

    private static Map<String, Object> readCompound(DataInputStream in) throws IOException {
        Map<String, Object> map = new HashMap<>();
        while (true) {
            byte type = in.readByte();
            if (type == 0) {
                return map;
            }
            String name = readString(in);
            map.put(name, readPayload(in, type));
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
            case 9 -> readList(in);
            case 10 -> readCompound(in);
            case 11 -> {
                int len = in.readInt();
                for (int i = 0; i < len; i++) {
                    in.readInt();
                }
                yield new int[0];
            }
            case 12 -> {
                int len = in.readInt();
                for (int i = 0; i < len; i++) {
                    in.readLong();
                }
                yield new long[0];
            }
            default -> throw new IOException("Unsupported NBT type " + type);
        };
    }

    private static Object readList(DataInputStream in) throws IOException {
        byte elemType = in.readByte();
        int len = in.readInt();
        List<Object> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(readPayload(in, elemType));
        }
        if (elemType == 3) {
            List<Integer> ints = new ArrayList<>(len);
            for (Object o : list) {
                ints.add(((Number) o).intValue());
            }
            return ints;
        }
        if (elemType == 10) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> compounds = (List<Map<String, Object>>) (List<?>) list;
            return compounds;
        }
        return list;
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        return new String(in.readNBytes(len), StandardCharsets.UTF_8);
    }
}
