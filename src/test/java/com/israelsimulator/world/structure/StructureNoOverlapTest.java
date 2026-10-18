package com.israelsimulator.world.structure;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * No two Israel-Simulator structures may overlap, whatever their biomes. This is enforced in game
 * by {@link StructureOverlapGuard} (priority list + exact piece boxes, biome-aware), which replaced
 * the biome-blind exclusion zones of PR 65/69. Here we check the data the guard relies on.
 *
 * <p>Reach of a structure from its start chunk corner (JigsawPlacement 26.2): the start template is
 * rotated around the corner and the max-distance box is centred on the start piece, so
 * reach = max_distance_from_center + ceil((longest start side - 1) / 2); a single template without
 * jigsaw blocks only reaches (longest side - 1).
 */
class StructureNoOverlapTest {

    private static final Gson GSON = new Gson();

    private List<String[]> modSets() throws Exception {
        Path dir = Paths.get(getClass().getClassLoader().getResource("data/israel_simulator/worldgen/structure_set").toURI());
        List<String[]> out = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                String set = f.getFileName().toString().replace(".json", "");
                JsonObject j = json("worldgen/structure_set/" + set + ".json");
                assertEquals(1, j.getAsJsonArray("structures").size(), set + ": one structure per set (the guard ranks structures)");
                String s = j.getAsJsonArray("structures").get(0).getAsJsonObject().get("structure").getAsString();
                assertTrue(s.startsWith("israel_simulator:"), s);
                out.add(new String[] {set, s.substring("israel_simulator:".length())});
            }
        }
        assertFalse(out.isEmpty());
        return out;
    }

    @Test
    @DisplayName("Every mod structure set is a plain random_spread without biome-blind exclusion zones")
    void setsArePlainRandomSpread() throws Exception {
        for (String[] e : modSets()) {
            JsonObject p = json("worldgen/structure_set/" + e[0] + ".json").getAsJsonObject("placement");
            assertEquals("minecraft:random_spread", p.get("type").getAsString(), e[0]);
            assertFalse(p.has("exclusion_zone"), e[0] + ": overlaps are handled by StructureOverlapGuard, not exclusion zones");
            assertTrue(p.get("spacing").getAsInt() > p.get("separation").getAsInt(), e[0]);
            assertFalse(p.has("frequency"), e[0] + ": the guard assumes frequency 1.0");
        }
    }

    @Test
    @DisplayName("The guard ranks every mod structure exactly once")
    void priorityCoversAllStructures() throws Exception {
        Set<String> structures = new HashSet<>();
        for (String[] e : modSets()) structures.add(e[1]);
        assertEquals(structures, new HashSet<>(StructureOverlapGuard.PRIORITY), "PRIORITY must list every mod structure");
        assertEquals(StructureOverlapGuard.PRIORITY.size(), new HashSet<>(StructureOverlapGuard.PRIORITY).size(), "no duplicates");
    }

    @Test
    @DisplayName("The guard's reach table is a safe upper bound for every structure")
    void reachTableIsConservative() throws Exception {
        StringBuilder bad = new StringBuilder();
        for (String[] e : modSets()) {
            int computed = reach(e[1]);
            Integer table = StructureOverlapGuard.REACH.get(e[1]);
            if (table == null || table < computed) bad.append(e[1]).append('=').append(computed).append(' ');
        }
        assertEquals("", bad.toString(), "REACH missing or too small (needed values)");
        assertEquals(new HashSet<>(StructureOverlapGuard.PRIORITY), StructureOverlapGuard.REACH.keySet());
    }

    @Test
    @DisplayName("Terrain adaptation margin: beard/bury/encapsulate reshape 12 blocks, none 0")
    void terrainMargin() throws Exception {
        assertEquals(12, StructureOverlapGuard.TERRAIN_MARGIN, "Beardifier kernel radius");
        for (String[] e : modSets()) {
            JsonObject s = json("worldgen/structure/" + e[1] + ".json");
            String t = s.has("terrain_adaptation") ? s.get("terrain_adaptation").getAsString() : "none";
            assertTrue(List.of("none", "beard_thin", "beard_box", "bury", "encapsulate").contains(t), e[1] + ": " + t);
        }
        BoundingBox house = new BoundingBox(0, 60, 0, 10, 70, 10);
        BoundingBox shop = new BoundingBox(20, 60, 0, 30, 70, 10);   // 9 free blocks in between
        assertFalse(StructureOverlapGuard.touches(List.of(house), 0, List.of(shop), 0));
        assertTrue(StructureOverlapGuard.touches(List.of(house), 12, List.of(shop), 0), "beard of one side buries the other");
        assertTrue(StructureOverlapGuard.touches(List.of(house), 0, List.of(shop), 12));
        BoundingBox far = new BoundingBox(34, 60, 0, 40, 70, 10);    // 23 free blocks: the two 12-block beards meet
        assertTrue(StructureOverlapGuard.touches(List.of(house), 12, List.of(far), 12), "both beards: need at least 24 free blocks");
        assertFalse(StructureOverlapGuard.touches(List.of(house), 12, List.of(new BoundingBox(35, 60, 0, 40, 70, 10)), 12));
    }

    @Test
    @DisplayName("The mixin is registered and overlap is tested on X/Z")
    void mixinAndTouches() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("israel_simulator.mixins.json");
        assertNotNull(in);
        assertTrue(new String(in.readAllBytes(), StandardCharsets.UTF_8).contains("\"StructureOverlapGuardMixin\""));
        BoundingBox low = new BoundingBox(0, 60, 0, 10, 70, 10);
        assertTrue(StructureOverlapGuard.touches(List.of(low), 0, List.of(new BoundingBox(10, 120, 10, 20, 130, 20)), 0),
                "touching columns count even at different heights");
        assertFalse(StructureOverlapGuard.touches(List.of(low), 0, List.of(new BoundingBox(11, 60, 0, 20, 70, 10)), 0));
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
