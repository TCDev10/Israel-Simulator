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
 * Jerusalem house/shuk/synagogue containers must carry israel_simulator chest loot tables.
 */
class JerusalemChestLootTest {

    private static final List<String> PIECES = List.of(
            "jerusalem/house_a",
            "jerusalem/house_b",
            "jerusalem/house_c",
            "jerusalem/shuk_stalls",
            "jerusalem/synagogue_small"
    );

    @Test
    @DisplayName("Every jerusalem chest/barrel has an israel_simulator:chests/ LootTable")
    void containersHaveLootTables() throws Exception {
        int containers = 0;
        for (String piece : PIECES) {
            String path = "data/israel_simulator/structure/" + piece + ".nbt";
            StructureTemplate tpl = StructureTemplate.load(readGzip(path));
            for (PlacedBlock b : tpl.blocks) {
                if (!b.name.equals("minecraft:chest") && !b.name.equals("minecraft:barrel")) {
                    continue;
                }
                containers++;
                assertNotNull(b.entityNbt, piece + " " + b.name + " at " + b.x + "," + b.y + "," + b.z
                        + " missing block entity NBT");
                Object loot = b.entityNbt.get("LootTable");
                assertNotNull(loot, piece + " missing LootTable");
                String lootId = String.valueOf(loot);
                assertTrue(lootId.startsWith("israel_simulator:chests/"),
                        piece + " LootTable must be under israel_simulator:chests/, got " + lootId);
                assertTrue(b.entityNbt.containsKey("LootTableSeed"),
                        piece + " should set LootTableSeed");
                assertFalse(b.entityNbt.containsKey("Items"),
                        piece + " must not pre-fill Items; loot fills on open");
            }
        }
        assertTrue(containers >= 10, "Expected house+shuk+synagogue containers, got " + containers);
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
        final int x, y, z;
        final Map<String, Object> entityNbt;

        PlacedBlock(String name, int x, int y, int z, Map<String, Object> entityNbt) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.entityNbt = entityNbt;
        }
    }

    private static final class StructureTemplate {
        final List<PlacedBlock> blocks;

        StructureTemplate(List<PlacedBlock> blocks) {
            this.blocks = blocks;
        }

        static StructureTemplate load(byte[] uncompressed) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(uncompressed));
            assertEquals(10, in.readByte());
            readString(in);
            Map<String, Object> root = readCompound(in);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> palette = (List<Map<String, Object>>) root.get("palette");
            List<String> names = new ArrayList<>();
            for (Map<String, Object> e : palette) {
                names.add((String) e.get("Name"));
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocksTag = (List<Map<String, Object>>) root.get("blocks");
            List<PlacedBlock> out = new ArrayList<>();
            for (Map<String, Object> b : blocksTag) {
                @SuppressWarnings("unchecked")
                List<Integer> pos = (List<Integer>) b.get("pos");
                int state = ((Number) b.get("state")).intValue();
                @SuppressWarnings("unchecked")
                Map<String, Object> nbt = (Map<String, Object>) b.get("nbt");
                out.add(new PlacedBlock(names.get(state), pos.get(0), pos.get(1), pos.get(2), nbt));
            }
            return new StructureTemplate(out);
        }
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
