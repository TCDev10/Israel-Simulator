package com.israelsimulator.world;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import com.israelsimulator.world.structure.StructureVegetationGuard;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * No structure template may bake in vegetation the game would not keep: plants without their soil,
 * vines on nothing, leaf blobs floating in the air, decaying (non-persistent) leaves.
 * Same rules as scripts/worldgen/vegetation.py, which every generator runs before saving.
 */
class StructureVegetationTest {

    private static final Path ROOT = Path.of("src/main/resources/data/israel_simulator/structure");
    private static final Set<String> SOIL = Set.of("grass_block", "dirt", "coarse_dirt", "podzol", "rooted_dirt", "mud",
            "moss_block", "farmland", "mycelium", "pale_moss_block", "muddy_mangrove_roots", "dirt_path");
    private static final Set<String> DRY_SOIL = Set.of("sand", "red_sand", "suspicious_sand", "terracotta");
    private static final Set<String> SMALL = Set.of("short_grass", "fern", "bush", "firefly_bush", "mediterranean_herbs",
            "dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip", "white_tulip",
            "pink_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley", "torchflower", "pink_petals", "wildflowers",
            "azalea", "flowering_azalea", "sweet_berry_bush", "tall_grass", "large_fern", "sunflower", "lilac",
            "rose_bush", "peony");
    private static final Set<String> DRY = Set.of("dead_bush", "short_dry_grass", "tall_dry_grass");
    private static final Set<String> CROPS = Set.of("wheat", "carrots", "potatoes", "beetroots", "melon_stem",
            "pumpkin_stem", "grapevine", "tomato_crop", "eggplant_crop", "chickpea_crop", "sesame_crop");
    private static final Pattern NOT_FULL = Pattern.compile("(_slab|_stairs|_wall|fence|_pane|bars|door|trapdoor|button|"
            + "plate|carpet|sign|banner|torch|lantern|chain|ladder|rail|pot|candle|lever|head|skull|campfire|rod|bed|"
            + "chest|barrel|lectern|anvil|cauldron|hopper|bell|grindstone|stonecutter|beehive|jigsaw|composter|glass|"
            + "leaves|_log|_wood|vine|snow|water|lava)");
    private static final int[][] N6 = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    @Test
    @DisplayName("Every template keeps its plants, vines and leaves physically supported")
    void templatesHaveNoFloatingOrUnsupportedVegetation() throws Exception {
        List<String> problems = new ArrayList<>();
        int templates = 0;
        try (Stream<Path> files = Files.walk(ROOT)) {
            for (Path f : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".nbt"))::iterator) {
                templates++;
                check(ROOT.relativize(f).toString(), Template.load(Files.readAllBytes(f)), problems);
            }
        }
        assertTrue(templates > 100, "templates found: " + templates);
        assertTrue(problems.isEmpty(), problems.size() + " vegetation problems, e.g. "
                + problems.subList(0, Math.min(12, problems.size())));
    }

    @Test
    @DisplayName("Vegetation guard keeps world features a tree-canopy away from structure pieces")
    void guardMargin() {
        BoundingBox box = new BoundingBox(0, 60, 0, 15, 70, 15);
        assertTrue(StructureVegetationGuard.near(box, 8, 64, 8), "inside");
        assertTrue(StructureVegetationGuard.near(box, -StructureVegetationGuard.MARGIN, 61, 20), "margin corner");
        assertFalse(StructureVegetationGuard.near(box, -StructureVegetationGuard.MARGIN - 1, 64, 8), "beyond margin");
        assertFalse(StructureVegetationGuard.near(box, 8, 73, 8), "above the roof is open sky");
        assertTrue(StructureVegetationGuard.MARGIN >= 5, "acacia/oak canopies reach 4-5 blocks sideways");
    }

    private static String s(String n) {
        int i = n.indexOf(':');
        return i < 0 ? n : n.substring(i + 1);
    }

    private static boolean isPlant(String n) {
        String s = s(n);
        return SMALL.contains(s) || DRY.contains(s) || CROPS.contains(s) || s.equals("cactus") || s.equals("sugar_cane")
                || (s.endsWith("_sapling") && !s.startsWith("potted_"));
    }

    private static boolean isTree(String n) {
        String s = s(n);
        return s.endsWith("_leaves") || ((s.endsWith("_log") || s.endsWith("_wood")) && !s.startsWith("stripped_"));
    }

    private static boolean isVeg(String n) {
        return isPlant(n) || isTree(n) || s(n).equals("vine");
    }

    private static void check(String file, Template t, List<String> problems) {
        Map<Long, Block> g = t.blocks;
        for (Map.Entry<Long, Block> e : g.entrySet()) {
            Block b = e.getValue();
            int x = Template.ux(e.getKey()), y = Template.uy(e.getKey()), z = Template.uz(e.getKey());
            String n = b.name, sn = s(n);
            if (sn.endsWith("_leaves") && "false".equals(b.props.get("persistent"))) {
                problems.add(file + " decaying leaves at " + x + "," + y + "," + z);
            }
            if ((sn.endsWith("_log") || sn.endsWith("_wood")) && !sn.startsWith("stripped_")
                    && "y".equals(b.props.getOrDefault("axis", "y"))) {
                Block below = g.get(Template.key(x, y - 1, z));
                if (below != null && s(below.name).equals("air")) {
                    problems.add(file + " trunk on air at " + x + "," + y + "," + z);
                }
            }
            if (isPlant(n)) {
                if ("upper".equals(b.props.get("half"))) {
                    Block below = g.get(Template.key(x, y - 1, z));
                    if (below == null || !below.name.equals(n)) problems.add(file + " half " + sn + " at " + x + "," + y + "," + z);
                    continue;
                }
                Block below = g.get(Template.key(x, y - 1, z));
                if (below == null || below.name.equals("minecraft:structure_void") || below.name.equals("minecraft:jigsaw")) continue;
                String bn = s(below.name);
                boolean ok;
                if (CROPS.contains(sn)) ok = bn.equals("farmland");
                else if (sn.equals("cactus")) ok = bn.equals("sand") || bn.equals("red_sand") || bn.equals("cactus");
                else if (sn.equals("sugar_cane")) ok = SOIL.contains(bn) || DRY_SOIL.contains(bn) || bn.equals("sugar_cane");
                else if (DRY.contains(sn)) ok = SOIL.contains(bn) || DRY_SOIL.contains(bn) || bn.endsWith("_terracotta");
                else ok = SOIL.contains(bn);
                if (!ok) problems.add(file + " " + sn + " on " + bn + " at " + x + "," + y + "," + z);
            }
            if (sn.equals("vine")) {
                boolean any = false;
                Block above = g.get(Template.key(x, y + 1, z));
                String[] faces = {"north", "south", "east", "west", "up"};
                int[][] d = {{0, 0, -1}, {0, 0, 1}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}};
                for (int i = 0; i < faces.length; i++) {
                    if (!"true".equals(b.props.get(faces[i]))) continue;
                    Block nb = g.get(Template.key(x + d[i][0], y + d[i][1], z + d[i][2]));
                    boolean outside = !t.inside(x + d[i][0], y + d[i][1], z + d[i][2]);
                    boolean hang = i < 4 && above != null && above.name.equals(n) && "true".equals(above.props.get(faces[i]));
                    if (outside || nb == null || full(nb) || hang) any = true;
                }
                if (!any) problems.add(file + " vine on nothing at " + x + "," + y + "," + z);
            }
        }
        // floating leaf / log blobs
        Set<Long> tree = new HashSet<>();
        for (Map.Entry<Long, Block> e : g.entrySet()) if (isTree(e.getValue().name)) tree.add(e.getKey());
        Set<Long> seen = new HashSet<>();
        for (Long start : tree) {
            if (seen.contains(start)) continue;
            ArrayDeque<Long> q = new ArrayDeque<>(List.of(start));
            seen.add(start);
            boolean anchored = false;
            int size = 0;
            while (!q.isEmpty()) {
                long c = q.pop();
                size++;
                int x = Template.ux(c), y = Template.uy(c), z = Template.uz(c);
                for (int[] d : N6) {
                    int nx = x + d[0], ny = y + d[1], nz = z + d[2];
                    long k = Template.key(nx, ny, nz);
                    if (tree.contains(k)) {
                        if (seen.add(k)) q.push(k);
                        continue;
                    }
                    Block nb = g.get(k);
                    if (!t.inside(nx, ny, nz)) {
                        continue;
                    }
                    if (nb == null || nb.name.equals("minecraft:structure_void")) {
                        anchored = true;
                        continue;
                    }
                    String ns = s(nb.name);
                    if (!ns.equals("air") && !isVeg(nb.name) && !ns.contains("water")) anchored = true;
                    if ((ns.equals("azalea") || ns.equals("flowering_azalea")) && d[1] == -1) anchored = true;
                }
            }
            if (!anchored) problems.add(file + " floating leaves/logs (" + size + ") at "
                    + Template.ux(start) + "," + Template.uy(start) + "," + Template.uz(start));
        }
    }

    private static boolean full(Block b) {
        String n = s(b.name);
        if (n.equals("air") || n.equals("cave_air")) return false;
        if (n.endsWith("_leaves") || n.endsWith("_log") || n.endsWith("_wood")) return true;
        return !isPlant(b.name) && !NOT_FULL.matcher(n).find();
    }

    private record Block(String name, Map<String, String> props) {}

    private static final class Template {
        final int sx, sy, sz;
        final Map<Long, Block> blocks = new HashMap<>();

        Template(int sx, int sy, int sz) {
            this.sx = sx; this.sy = sy; this.sz = sz;
        }

        boolean inside(int x, int y, int z) {
            return x >= 0 && y >= 0 && z >= 0 && x < sx && y < sy && z < sz;
        }

        static long key(int x, int y, int z) {
            return ((long) (x + 1024) << 40) | ((long) (y + 1024) << 20) | (z + 1024);
        }

        static int ux(long k) { return (int) (k >>> 40) - 1024; }
        static int uy(long k) { return (int) ((k >>> 20) & 0xFFFFF) - 1024; }
        static int uz(long k) { return (int) (k & 0xFFFFF) - 1024; }

        @SuppressWarnings("unchecked")
        static Template load(byte[] gz) throws Exception {
            byte[] raw;
            try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(gz))) {
                raw = in.readAllBytes();
            }
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));
            in.readByte();
            readUtf(in);
            Map<String, Object> root = readCompound(in);
            List<Integer> size = (List<Integer>) root.get("size");
            Template t = new Template(size.get(0), size.get(1), size.get(2));
            List<Map<String, Object>> palette = (List<Map<String, Object>>) root.get("palette");
            List<Block> pal = new ArrayList<>();
            for (Map<String, Object> p : palette) {
                Map<String, String> props = new HashMap<>();
                Object pr = p.get("Properties");
                if (pr instanceof Map<?, ?> m) m.forEach((k, v) -> props.put(String.valueOf(k), String.valueOf(v)));
                pal.add(new Block(String.valueOf(p.get("Name")), props));
            }
            for (Map<String, Object> b : (List<Map<String, Object>>) root.get("blocks")) {
                List<Integer> pos = (List<Integer>) b.get("pos");
                t.blocks.put(key(pos.get(0), pos.get(1), pos.get(2)), pal.get(((Number) b.get("state")).intValue()));
            }
            return t;
        }

        private static Map<String, Object> readCompound(DataInputStream in) throws Exception {
            Map<String, Object> map = new HashMap<>();
            while (true) {
                byte type = in.readByte();
                if (type == 0) return map;
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
                case 7 -> { int len = in.readInt(); in.skipBytes(len); yield len; }
                case 8 -> readUtf(in);
                case 9 -> {
                    byte lt = in.readByte();
                    int len = in.readInt();
                    List<Object> list = new ArrayList<>(len);
                    for (int i = 0; i < len; i++) list.add(readPayload(in, lt));
                    yield list;
                }
                case 10 -> readCompound(in);
                case 11 -> {
                    int len = in.readInt();
                    List<Integer> list = new ArrayList<>(len);
                    for (int i = 0; i < len; i++) list.add(in.readInt());
                    yield list;
                }
                case 12 -> { int len = in.readInt(); for (int i = 0; i < len; i++) in.readLong(); yield len; }
                default -> throw new IllegalStateException("bad nbt type " + type);
            };
        }

        private static String readUtf(DataInputStream in) throws Exception {
            int len = in.readUnsignedShort();
            return new String(in.readNBytes(len), StandardCharsets.UTF_8);
        }
    }
}
