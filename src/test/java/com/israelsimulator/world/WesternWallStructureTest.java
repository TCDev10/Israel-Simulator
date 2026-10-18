package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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

/**
 * Western Wall template is a real Kotel plaza (not a hollow box) with prayer stone and treasury loot.
 */
class WesternWallStructureTest {

    @Test
    @DisplayName("western_wall structure stays locked to jerusalem with beard_box")
    void structureJson() throws Exception {
        JsonObject json = readJson("data/israel_simulator/worldgen/structure/western_wall.json");
        JsonArray biomes = json.getAsJsonArray("biomes");
        assertEquals(1, biomes.size());
        assertEquals("israel_simulator:jerusalem", biomes.get(0).getAsString());
        assertEquals("beard_box", json.get("terrain_adaptation").getAsString());
        assertTrue(json.get("max_distance_from_center").getAsInt() >= 80);
        assertEquals(-4, json.getAsJsonObject("start_height").get("absolute").getAsInt(),
                "floor sits on the surface above a 4-block buried footing");
    }

    @Test
    @DisplayName("Wall face is built of large mixed pale ashlars, prayer stone within reach of the plaza")
    void wallFaceHasLargeAshlarsAndReachablePrayerStone() throws Exception {
        StructureTemplate tpl = StructureTemplate.load(readGzip("data/israel_simulator/structure/western_wall.nbt"));
        Map<String, String> at = new HashMap<>();
        for (PlacedBlock b : tpl.blocks) at.put(b.x + "," + b.y + "," + b.z, b.name);
        int floor = 4, front = 4;
        // Herodian courses: several pale materials, stones 2 high and at least 3 wide
        Set<String> faceMats = new HashSet<>();
        int runs = 0, longRuns = 0;
        for (int y = floor; y < floor + 12; y += 2) {
            String prev = null;
            int len = 0;
            for (int x = 0; x <= tpl.sizeX; x++) {
                String n = x < tpl.sizeX ? at.get(x + "," + y + "," + front) : null;
                String above = at.get(x + "," + (y + 1) + "," + front);
                if (n != null && !n.equals("minecraft:air") && !"minecraft:air".equals(above)) {
                    assertEquals(n, at.get(x + "," + (y + 1) + "," + front), "stone 2 high at x=" + x + " y=" + y);
                    faceMats.add(n);
                }
                if (n != null && n.equals(prev)) {
                    len++;
                } else {
                    if (prev != null && !prev.equals("minecraft:air")) {
                        runs++;
                        if (len >= 3) longRuns++;
                    }
                    prev = n;
                    len = 1;
                }
            }
        }
        faceMats.remove("minecraft:air");
        assertTrue(faceMats.size() >= 3, "pale ashlar mix " + faceMats);
        assertTrue(longRuns * 10 >= runs * 7, "most stones are large: " + longRuns + "/" + runs);
        // reachable from the plaza (eye height): mostly prayer stone
        int wws = 0, total = 0;
        for (int y = floor; y < floor + 4; y++)
            for (int x = 12; x < 52; x++) {
                total++;
                if ("israel_simulator:western_wall_stone".equals(at.get(x + "," + y + "," + front))) wws++;
            }
        assertTrue(wws * 2 >= total, "prayer stone in reach " + wws + "/" + total);
        // plaza furniture: shtenders, washing basins, the big menorah, mechitza, ramp
        Map<String, Integer> counts = new HashMap<>();
        for (PlacedBlock b : tpl.blocks) counts.merge(b.name, 1, Integer::sum);
        assertTrue(counts.getOrDefault("minecraft:lectern", 0) >= 6, "shtenders");
        assertTrue(counts.getOrDefault("minecraft:water_cauldron", 0) >= 3, "hand-washing basins");
        assertTrue(counts.getOrDefault("israel_simulator:menorah", 0) >= 2, "plaza menorah + ark menorah");
        assertTrue(counts.getOrDefault("minecraft:dark_oak_fence", 0) >= 15, "mechitza");
        assertTrue(counts.getOrDefault("minecraft:spruce_planks", 0) >= 40, "Mughrabi ramp");
    }

    @Test
    @DisplayName("western_wall NBT is a large wall+plaza with western_wall_stone and treasury loot")
    void templateHasWallPlazaPrayerStoneAndChest() throws Exception {
        StructureTemplate tpl = StructureTemplate.load(readGzip("data/israel_simulator/structure/western_wall.nbt"));
        assertTrue(tpl.sizeX >= 48 && tpl.sizeX <= 64, "length " + tpl.sizeX);
        assertTrue(tpl.sizeY >= 20 && tpl.sizeY <= 30, "height " + tpl.sizeY);
        assertTrue(tpl.sizeZ >= 30 && tpl.sizeZ <= 48, "depth " + tpl.sizeZ);

        Map<String, Integer> counts = new HashMap<>();
        int chests = 0;
        boolean lootOk = false;
        Set<String> names = new HashSet<>();
        for (PlacedBlock b : tpl.blocks) {
            names.add(b.name);
            counts.merge(b.name, 1, Integer::sum);
            if (b.name.equals("minecraft:chest")) {
                chests++;
                assertNotNull(b.entityNbt, "chest missing NBT");
                Object loot = b.entityNbt.get("LootTable");
                assertEquals("israel_simulator:chests/western_wall_treasury", String.valueOf(loot));
                lootOk = true;
            }
        }
        assertFalse(names.contains("minecraft:gold_block"), "placeholder gold gone");
        assertFalse(names.contains("minecraft:sea_lantern"), "placeholder lantern gone");
        int wws = counts.getOrDefault("israel_simulator:western_wall_stone", 0);
        assertTrue(wws >= 100, "need reachable western_wall_stone for prayer, got " + wws);
        assertTrue(counts.getOrDefault("israel_simulator:jerusalem_stone", 0) >= 100);
        assertEquals(1, chests);
        assertTrue(lootOk);
        // Not a hollow 9x6x5 box: solid blocks should be many thousands
        int solid = tpl.blocks.size() - counts.getOrDefault("minecraft:air", 0);
        assertTrue(solid >= 3000, "solid blocks " + solid);
    }

    @Test
    @DisplayName("Prayer detection still keys off western_wall_stone block id")
    void prayerUsesWesternWallStoneBlock() throws Exception {
        String src = java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/java/com/israelsimulator/event/ModGameEvents.java"));
        assertTrue(src.contains("WESTERN_WALL_STONE"));
        assertTrue(src.contains("WesternWallManager.tryPray"));
    }

    private JsonObject readJson(String path) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, path);
            return new Gson().fromJson(new String(in.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
        }
    }

    private byte[] readGzip(String path) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, "Missing NBT: " + path);
        try (GZIPInputStream gzip = new GZIPInputStream(stream)) {
            return gzip.readAllBytes();
        }
    }

    private static final class PlacedBlock {
        final String name;
        final Map<String, Object> entityNbt;
        int x, y, z;

        PlacedBlock(String name, Map<String, Object> entityNbt) {
            this.name = name;
            this.entityNbt = entityNbt;
        }
    }

    private static final class StructureTemplate {
        final int sizeX, sizeY, sizeZ;
        final List<PlacedBlock> blocks;

        StructureTemplate(int sizeX, int sizeY, int sizeZ, List<PlacedBlock> blocks) {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.blocks = blocks;
        }

        static StructureTemplate load(byte[] raw) throws Exception {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));
            Map<String, Object> root = readNamed(in);
            @SuppressWarnings("unchecked")
            List<Integer> size = (List<Integer>) root.get("size");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> palette = (List<Map<String, Object>>) root.get("palette");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocksTag = (List<Map<String, Object>>) root.get("blocks");
            List<PlacedBlock> blocks = new ArrayList<>();
            for (Map<String, Object> b : blocksTag) {
                int state = ((Number) b.get("state")).intValue();
                Map<String, Object> pe = palette.get(state);
                String name = String.valueOf(pe.get("Name"));
                @SuppressWarnings("unchecked")
                Map<String, Object> nbt = (Map<String, Object>) b.get("nbt");
                PlacedBlock pb = new PlacedBlock(name, nbt);
                @SuppressWarnings("unchecked")
                List<Integer> pos = (List<Integer>) b.get("pos");
                pb.x = pos.get(0); pb.y = pos.get(1); pb.z = pos.get(2);
                blocks.add(pb);
            }
            return new StructureTemplate(size.get(0), size.get(1), size.get(2), blocks);
        }

        // Minimal NBT reader (same approach as JerusalemChestLootTest)
        private static Map<String, Object> readNamed(DataInputStream in) throws Exception {
            in.readByte();
            readUtf(in);
            return readCompound(in);
        }

        private static Map<String, Object> readCompound(DataInputStream in) throws Exception {
            Map<String, Object> map = new HashMap<>();
            while (true) {
                byte type = in.readByte();
                if (type == 0) {
                    return map;
                }
                String name = readUtf(in);
                map.put(name, readPayload(in, type));
            }
        }

        private static Object readPayload(DataInputStream in, byte type) throws Exception {
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
                    yield len;
                }
                case 8 -> readUtf(in);
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
                    yield len;
                }
                default -> throw new IllegalStateException("bad nbt type " + type);
            };
        }

        private static String readUtf(DataInputStream in) throws Exception {
            int len = in.readUnsignedShort();
            byte[] buf = in.readNBytes(len);
            return new String(buf, StandardCharsets.UTF_8);
        }
    }
}
