package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.israelsimulator.world.biome.IsraelBiomeClimateParams;
import com.israelsimulator.world.biome.ModBiomes;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** tropical_island: rare warm deep-ocean biome + whole-island structure (gen_tropical_island.py). */
class TropicalIslandTest {

    private static final Gson GSON = new Gson();
    private static final String NBT = "data/israel_simulator/structure/tropical_island.nbt";

    @Test
    @DisplayName("Biome: turquoise water, no vanilla hostile spawns, palms, translated")
    void biomeJson() throws Exception {
        JsonObject b = readJson("data/israel_simulator/worldgen/biome/tropical_island.json");
        assertEquals("#2fe0d2", b.getAsJsonObject("effects").get("water_color").getAsString());
        assertEquals(0, b.getAsJsonObject("spawners").getAsJsonArray("monster").size());
        assertTrue(b.getAsJsonArray("features").toString().contains("israel_simulator:tropical_palm_placed"));
        for (String lang : List.of("en_us", "it_it")) {
            JsonObject l = readJson("assets/israel_simulator/lang/" + lang + ".json");
            assertTrue(l.has("biome.israel_simulator.tropical_island"), lang);
        }
        JsonObject s = readJson("data/israel_simulator/worldgen/structure/tropical_island.json");
        assertEquals("israel_simulator:tropical_island", s.getAsJsonArray("biomes").get(0).getAsString());
        assertTrue(s.get("max_distance_from_center").getAsInt() <= 116);
    }

    @Test
    @DisplayName("Climate: only the far-offshore warm deep-ocean band becomes tropical_island")
    void climateCarve() {
        Climate.ParameterPoint deep = Climate.parameters(Climate.Parameter.span(0.55F, 1F), Climate.Parameter.span(-1F, 1F),
                Climate.Parameter.span(-1.05F, -0.455F), Climate.Parameter.span(-1F, 1F), Climate.Parameter.point(0F),
                Climate.Parameter.span(-1F, 1F), 0F);
        var out = IsraelBiomeClimateParams.remap(deep, Biomes.WARM_OCEAN);
        assertNotNull(out);
        long island = out.stream().filter(p -> p.getSecond().equals(ModBiomes.TROPICAL_ISLAND)).count();
        assertTrue(island >= 1);
        for (var p : out) {
            if (p.getSecond().equals(ModBiomes.TROPICAL_ISLAND)) {
                assertTrue(p.getFirst().continentalness().max() <= Climate.quantizeCoord(-0.455F));
                assertTrue(p.getFirst().continentalness().min() >= Climate.quantizeCoord(-1.05F));
            }
        }
        Climate.ParameterPoint shallow = Climate.parameters(Climate.Parameter.span(0.55F, 1F), Climate.Parameter.span(-1F, 1F),
                Climate.Parameter.span(-0.455F, -0.19F), Climate.Parameter.span(-1F, 1F), Climate.Parameter.point(0F),
                Climate.Parameter.span(-1F, 1F), 0F);
        assertNull(IsraelBiomeClimateParams.remap(shallow, Biomes.WARM_OCEAN), "shallow warm ocean untouched");
        assertNull(IsraelBiomeClimateParams.remap(deep, Biomes.DEEP_COLD_OCEAN), "cold oceans untouched");
    }

    @Test
    @DisplayName("Island template: pavilion, villa roofs, pool, helipads, tennis, dome, dock, roads, palms, chests")
    void islandTemplate() throws Exception {
        Map<String, Integer> count = new HashMap<>();
        int[] chests = {0};
        List<Integer> size = load(count, chests);
        assertEquals(List.of(240, 72, 170), size);
        Map<String, Integer> min = Map.ofEntries(
                Map.entry("minecraft:gold_block", 150), Map.entry("minecraft:blue_concrete", 150),
                Map.entry("minecraft:light_blue_concrete", 300), Map.entry("minecraft:white_concrete", 1000),
                Map.entry("minecraft:water", 200), Map.entry("minecraft:light_gray_concrete", 200),
                Map.entry("minecraft:green_concrete", 100), Map.entry("minecraft:iron_bars", 100),
                Map.entry("minecraft:spruce_planks", 60), Map.entry("minecraft:coarse_dirt", 1500),
                Map.entry("minecraft:sand", 1500), Map.entry("minecraft:stone", 1000),
                Map.entry("minecraft:grass_block", 5000), Map.entry("minecraft:jungle_log", 300),
                Map.entry("minecraft:dark_oak_door", 2), Map.entry("minecraft:birch_door", 20));
        for (var e : min.entrySet()) {
            assertTrue(count.getOrDefault(e.getKey(), 0) >= e.getValue(), e.getKey() + " = " + count.get(e.getKey()));
        }
        assertEquals(1, count.getOrDefault("minecraft:jigsaw", 0), "only the centre anchor jigsaw");
        assertTrue(chests[0] >= 10, "chests with loot: " + chests[0]);
    }

    @SuppressWarnings("unchecked")
    private List<Integer> load(Map<String, Integer> count, int[] chests) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(NBT);
        assertNotNull(in);
        byte[] raw;
        try (GZIPInputStream gz = new GZIPInputStream(in)) {
            raw = gz.readAllBytes();
        }
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(raw));
        assertEquals(10, d.readByte());
        readString(d);
        Map<String, Object> root = readCompound(d);
        List<Map<String, Object>> palette = (List<Map<String, Object>>) root.get("palette");
        for (Map<String, Object> b : (List<Map<String, Object>>) root.get("blocks")) {
            String name = (String) palette.get(((Number) b.get("state")).intValue()).get("Name");
            count.merge(name, 1, Integer::sum);
            Object nbt = b.get("nbt");
            if (nbt instanceof Map<?, ?> m && m.containsKey("LootTable")) chests[0]++;
        }
        return (List<Integer>) root.get("size");
    }

    private JsonObject readJson(String path) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(in, "Missing " + path);
        return GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
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
