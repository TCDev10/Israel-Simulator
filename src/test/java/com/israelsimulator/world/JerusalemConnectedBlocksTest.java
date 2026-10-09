package com.israelsimulator.world;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * Jerusalem fence / glass pane / wall blocks must store connection properties
 * matching their neighbors in the template (not all-false / all-none stubs).
 */
class JerusalemConnectedBlocksTest {

    private static final List<String> PIECES = List.of(
            "jerusalem/plaza",
            "jerusalem/street_straight",
            "jerusalem/street_crossroad",
            "jerusalem/street_corner",
            "jerusalem/terminator",
            "jerusalem/house_a",
            "jerusalem/house_b",
            "jerusalem/house_c",
            "jerusalem/house_courtyard",
            "jerusalem/house_domed",
            "jerusalem/house_terrace",
            "jerusalem/house_templer",
            "jerusalem/house_rehavia",
            "jerusalem/house_arched",
            "jerusalem/shuk_stalls",
            "jerusalem/synagogue_small",
            "jerusalem/well",
            "jerusalem/wall_gate"
    );

    private static final int[][] HORIZONTAL = {
            {0, 0, -1}, // north
            {0, 0, 1},  // south
            {-1, 0, 0}, // west
            {1, 0, 0}   // east
    };
    private static final String[] DIR_NAMES = {"north", "south", "west", "east"};

    @Test
    @DisplayName("Jerusalem connectables with same-family or solid neighbors are not stored disconnected")
    void connectablesMatchNeighbors() throws Exception {
        int checkedLinks = 0;
        for (String piece : PIECES) {
            String path = "data/israel_simulator/structure/" + piece + ".nbt";
            StructureTemplate tpl = StructureTemplate.load(readGzip(path));
            Map<Long, BlockState> grid = tpl.grid();
            for (Map.Entry<Long, BlockState> e : grid.entrySet()) {
                BlockState self = e.getValue();
                if (!self.isConnectable()) {
                    continue;
                }
                int x = unpackX(e.getKey());
                int y = unpackY(e.getKey());
                int z = unpackZ(e.getKey());
                for (int i = 0; i < HORIZONTAL.length; i++) {
                    int nx = x + HORIZONTAL[i][0];
                    int ny = y + HORIZONTAL[i][1];
                    int nz = z + HORIZONTAL[i][2];
                    BlockState neighbor = grid.get(pack(nx, ny, nz));
                    if (neighbor == null || !self.shouldConnectTo(neighbor)) {
                        continue;
                    }
                    checkedLinks++;
                    String dir = DIR_NAMES[i];
                    String val = self.props.getOrDefault(dir, "");
                    if (self.isWall()) {
                        assertTrue(val.equals("low") || val.equals("tall"),
                                piece + " @" + x + "," + y + "," + z + " " + self.name
                                        + " " + dir + " should be low/tall toward " + neighbor.name
                                        + " but was '" + val + "'");
                    } else {
                        assertEquals("true", val,
                                piece + " @" + x + "," + y + "," + z + " " + self.name
                                        + " " + dir + " should be true toward " + neighbor.name);
                    }
                }
            }
        }
        assertTrue(checkedLinks >= 50,
                "Expected many connected neighbor links across Jerusalem pieces, got " + checkedLinks);
    }


    private byte[] readGzip(String path) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(stream, "Missing NBT: " + path);
        try (GZIPInputStream gzip = new GZIPInputStream(stream)) {
            return gzip.readAllBytes();
        }
    }

    private static long pack(int x, int y, int z) {
        return ((long) (x & 0xFFFF) << 32) | ((long) (y & 0xFFFF) << 16) | (long) (z & 0xFFFF);
    }

    private static int unpackX(long k) {
        return (short) ((k >> 32) & 0xFFFF);
    }

    private static int unpackY(long k) {
        return (short) ((k >> 16) & 0xFFFF);
    }

    private static int unpackZ(long k) {
        return (short) (k & 0xFFFF);
    }

    private static final class BlockState {
        final String name;
        final Map<String, String> props;

        BlockState(String name, Map<String, String> props) {
            this.name = name;
            this.props = props;
        }

        boolean isFence() {
            return name.endsWith("_fence") && !name.endsWith("_fence_gate");
        }

        boolean isFenceGate() {
            return name.endsWith("_fence_gate");
        }

        boolean isPaneOrBars() {
            return name.contains("glass_pane") || name.endsWith("_pane")
                    || name.equals("minecraft:iron_bars");
        }

        boolean isWall() {
            return name.endsWith("_wall") && !name.contains("banner") && !name.contains("sign");
        }

        boolean isConnectable() {
            return isFence() || isPaneOrBars() || isWall();
        }

        boolean isFullSolid() {
            if (name.equals("minecraft:air") || name.equals("minecraft:cave_air")
                    || name.equals("minecraft:void_air")) {
                return false;
            }
            String id = name.contains(":") ? name.substring(name.indexOf(':') + 1) : name;
            if (id.contains("stairs") || id.contains("slab") || id.contains("fence")
                    || id.contains("wall") || id.contains("pane") || id.contains("door")
                    || id.contains("trapdoor") || id.contains("button") || id.contains("pressure_plate")
                    || id.contains("carpet") || id.contains("sign") || id.contains("banner")
                    || id.contains("lantern") || id.contains("torch") || id.contains("flower")
                    || id.contains("leaves") || id.contains("chest") || id.contains("barrel")
                    || id.contains("bed") || id.contains("lectern") || id.contains("composter")
                    || id.contains("jigsaw") || id.contains("water") || id.contains("potted")
                    || id.equals("air")) {
                return false;
            }
            return true;
        }

        boolean shouldConnectTo(BlockState other) {
            if (isFence()) {
                return other.isFence() || other.isFenceGate() || other.isFullSolid();
            }
            if (isPaneOrBars()) {
                return other.isPaneOrBars() || other.isFullSolid() || other.name.contains("glass");
            }
            if (isWall()) {
                return other.isWall() || other.isFenceGate() || other.isFullSolid();
            }
            return false;
        }
    }

    /** Minimal structure-template loader (palette + blocks). */
    private static final class StructureTemplate {
        final List<BlockState> palette;
        final List<int[]> positions; // x,y,z,state

        StructureTemplate(List<BlockState> palette, List<int[]> positions) {
            this.palette = palette;
            this.positions = positions;
        }

        Map<Long, BlockState> grid() {
            Map<Long, BlockState> out = new HashMap<>();
            for (int[] p : positions) {
                out.put(pack(p[0], p[1], p[2]), palette.get(p[3]));
            }
            return out;
        }

        static StructureTemplate load(byte[] uncompressed) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(uncompressed));
            // Root TAG_Compound with empty name (or named)
            byte type = in.readByte();
            assertEquals(10, type, "root must be TAG_Compound");
            readString(in); // root name
            Map<String, Object> root = readCompound(in);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> paletteTag = (List<Map<String, Object>>) root.get("palette");
            assertNotNull(paletteTag, "structure missing palette");
            List<BlockState> palette = new ArrayList<>();
            for (Map<String, Object> entry : paletteTag) {
                String name = (String) entry.get("Name");
                @SuppressWarnings("unchecked")
                Map<String, String> props = (Map<String, String>) entry.getOrDefault("Properties", Map.of());
                // Properties values are strings already from our reader
                Map<String, String> copy = new HashMap<>();
                if (props != null) {
                    for (var e : props.entrySet()) {
                        copy.put(e.getKey(), String.valueOf(e.getValue()));
                    }
                }
                palette.add(new BlockState(name, copy));
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) root.get("blocks");
            assertNotNull(blocks, "structure missing blocks");
            List<int[]> positions = new ArrayList<>();
            for (Map<String, Object> b : blocks) {
                @SuppressWarnings("unchecked")
                List<Integer> pos = (List<Integer>) b.get("pos");
                int state = ((Number) b.get("state")).intValue();
                positions.add(new int[]{pos.get(0), pos.get(1), pos.get(2), state});
            }
            return new StructureTemplate(palette, positions);
        }
    }

    // --- tiny NBT reader (TAG types used by structure templates) ---

    private static Map<String, Object> readCompound(DataInputStream in) throws IOException {
        Map<String, Object> map = new HashMap<>();
        while (true) {
            byte type = in.readByte();
            if (type == 0) { // TAG_End
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
                int[] arr = new int[len];
                for (int i = 0; i < len; i++) {
                    arr[i] = in.readInt();
                }
                yield arr;
            }
            case 12 -> {
                int len = in.readInt();
                long[] arr = new long[len];
                for (int i = 0; i < len; i++) {
                    arr[i] = in.readLong();
                }
                yield arr;
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
        // Structure pos is List[Int]; palette is List[Compound]
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
        byte[] buf = in.readNBytes(len);
        return new String(buf, StandardCharsets.UTF_8);
    }
}
