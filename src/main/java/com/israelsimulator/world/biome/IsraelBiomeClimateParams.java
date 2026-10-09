package com.israelsimulator.world.biome;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

/**
 * Places the Israeli biomes in the Overworld multi-noise generator by <b>sharing</b> vanilla's hot
 * climate (desert, savanna and badlands families) with the vanilla biomes.
 *
 * <p>Simply adding extra parameter points would only tie with vanilla's identical boxes and lose
 * most of the time (the biomes were found 20-30k blocks out, if at all). Instead every vanilla
 * entry for a hot biome is split along its own weirdness range: the lower half stays vanilla, the
 * upper half becomes Israeli, split into a West→East strip:
 * <ul>
 *   <li>coast continentalness → {@code mediterranean_coast}</li>
 *   <li>near inland → {@code urban_area} (dry) / {@code mediterranean_coast} (wetter)</li>
 *   <li>mid inland → {@code israeli_agriculture} (wetter) / {@code jerusalem} (hills and dry uplands)</li>
 *   <li>far inland → {@code jerusalem} (hilly) / {@code judean_desert} / {@code dead_sea}
 *       (flatter pocket inside the desert band)</li>
 * </ul>
 * Vanilla desert, savanna (incl. plateau/windswept) and badlands variants all keep their half, so
 * they stay common and the Israeli biomes appear as patches next to them. Rivers, oceans and cave
 * biomes are untouched.</p>
 */
public final class IsraelBiomeClimateParams {

    /** Vanilla biomes whose (dry) climate boxes are handed to the Israeli strip. */
    public static final Set<ResourceKey<Biome>> TAKEN = Set.of(
            Biomes.DESERT, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA,
            Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS);


    // continentalness / erosion / humidity cuts (vanilla band edges)
    public static final float COAST_MAX = -0.11F;    // coast band is [-0.19, -0.11]
    public static final float NEAR_MAX = 0.03F;      // near inland [-0.11, 0.03]
    public static final float MID_MAX = 0.30F;       // mid inland [0.03, 0.3], far inland above
    public static final float HILLS_MAX = -0.375F;   // erosion indices 0-1: hills/mountains
    public static final float FLAT_MIN = 0.05F;      // erosion indices 4-6: flatter basins
    public static final float WET_MIN = 0.1F;        // humidity index >= 3
    public static final float FARM_WET_MIN = -0.1F;  // mid inland: farmland from humidity index 2

    /** Warm deep-ocean entries that host the rare {@code tropical_island} patch. */
    public static final Set<ResourceKey<Biome>> ISLAND_HOSTS = Set.of(Biomes.WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN);
    // tropical_island: an isolated sub-box far offshore inside the deep-ocean continentalness band
    public static final float ISLAND_C_MIN = -0.85F;
    public static final float ISLAND_C_MAX = -0.65F;
    public static final float ISLAND_W_MIN = -0.4F;
    public static final float ISLAND_W_MAX = 1.0F;
    public static final float ISLAND_E_MIN = -1.0F;
    public static final float ISLAND_E_MAX = 1.0F;

    private IsraelBiomeClimateParams() {}

    /**
     * Cuts the tropical_island box out of a warm deep-ocean entry; the rest stays vanilla ocean.
     * Returns null when the entry does not overlap the island band (e.g. shallow warm ocean).
     */
    static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> carveIsland(
            Climate.ParameterPoint p, ResourceKey<Biome> biome) {
        long c0 = Climate.quantizeCoord(ISLAND_C_MIN), c1 = Climate.quantizeCoord(ISLAND_C_MAX);
        if (p.continentalness().min() > c0 || p.continentalness().max() < c1) {
            return null;
        }
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out = new ArrayList<>();
        Climate.Parameter islandC = new Climate.Parameter(c0, c1);
        if (p.continentalness().min() < c0) {
            out.add(Pair.of(with(p, new Climate.Parameter(p.continentalness().min(), c0), p.erosion(), p.weirdness()), biome));
        }
        if (c1 < p.continentalness().max()) {
            out.add(Pair.of(with(p, new Climate.Parameter(c1, p.continentalness().max()), p.erosion(), p.weirdness()), biome));
        }
        List<Climate.Parameter> ws = split(p.weirdness(), ISLAND_W_MIN, ISLAND_W_MAX);
        long w0 = Math.max(p.weirdness().min(), Climate.quantizeCoord(ISLAND_W_MIN));
        long w1 = Math.min(p.weirdness().max(), Climate.quantizeCoord(ISLAND_W_MAX));
        long e0 = Math.max(p.erosion().min(), Climate.quantizeCoord(ISLAND_E_MIN));
        long e1 = Math.min(p.erosion().max(), Climate.quantizeCoord(ISLAND_E_MAX));
        for (Climate.Parameter w : ws) {
            boolean wIn = w.min() >= w0 && w.max() <= w1;
            for (Climate.Parameter e : split(p.erosion(), ISLAND_E_MIN, ISLAND_E_MAX)) {
                boolean eIn = e.min() >= e0 && e.max() <= e1;
                out.add(Pair.of(with(p, islandC, e, w), wIn && eIn ? ModBiomes.TROPICAL_ISLAND : biome));
            }
        }
        return out;
    }

    private static Climate.ParameterPoint with(Climate.ParameterPoint p, Climate.Parameter c, Climate.Parameter e,
                                               Climate.Parameter w) {
        return new Climate.ParameterPoint(p.temperature(), p.humidity(), c, e, p.depth(), w, p.offset());
    }

    /** Wraps a biome consumer so the taken vanilla entries are replaced by Israeli ones. */
    public static Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> wrap(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out) {
        return pair -> {
            List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> repl = remap(pair.getFirst(), pair.getSecond());
            if (repl == null) {
                out.accept(pair);
            } else {
                repl.forEach(out);
            }
        };
    }

    /**
     * Replacement entries for a vanilla entry, or null to keep it unchanged. A taken entry keeps its
     * lower weirdness half for the vanilla biome; the upper half is split West→East into Israeli biomes,
     * so both appear side by side as alternating patches.
     */
    public static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> remap(
            Climate.ParameterPoint p, ResourceKey<Biome> biome) {
        if (ISLAND_HOSTS.contains(biome)) {
            return carveIsland(p, biome);
        }
        if (!TAKEN.contains(biome) || p.weirdness().max() - p.weirdness().min() < 2) {
            return null;
        }
        long wMid = (p.weirdness().min() + p.weirdness().max()) / 2;
        Climate.Parameter vanillaW = new Climate.Parameter(p.weirdness().min(), wMid);
        Climate.Parameter israelW = new Climate.Parameter(wMid, p.weirdness().max());
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out = new ArrayList<>();
        out.add(Pair.of(new Climate.ParameterPoint(p.temperature(), p.humidity(), p.continentalness(),
                p.erosion(), p.depth(), vanillaW, p.offset()), biome));
        for (Climate.Parameter c : split(p.continentalness(), COAST_MAX, NEAR_MAX, MID_MAX)) {
            for (Climate.Parameter e : split(p.erosion(), HILLS_MAX, FLAT_MIN)) {
                for (Climate.Parameter h : split(p.humidity(), FARM_WET_MIN, WET_MIN)) {
                    Climate.ParameterPoint q = new Climate.ParameterPoint(
                            p.temperature(), h, c, e, p.depth(), israelW, p.offset());
                    out.add(Pair.of(q, pick(mid(c), mid(e), mid(h))));
                }
            }
        }
        return out;
    }

    /** The West→East assignment for a sub-box centre. */
    public static ResourceKey<Biome> pick(float c, float e, float h) {
        if (c < COAST_MAX) {
            return ModBiomes.MEDITERRANEAN_COAST;
        }
        if (c < NEAR_MAX) {
            return h >= WET_MIN ? ModBiomes.MEDITERRANEAN_COAST : ModBiomes.URBAN_AREA;
        }
        if (c < MID_MAX) {
            if (e < HILLS_MAX) {
                return ModBiomes.JERUSALEM;
            }
            return h >= FARM_WET_MIN ? ModBiomes.ISRAELI_AGRICULTURE : ModBiomes.JERUSALEM;
        }
        if (e < HILLS_MAX) {
            return ModBiomes.JERUSALEM;
        }
        return e >= FLAT_MIN ? ModBiomes.DEAD_SEA : ModBiomes.JUDEAN_DESERT;
    }

    static float mid(Climate.Parameter p) {
        return Climate.unquantizeCoord((p.min() + p.max()) / 2);
    }

    /** Splits a parameter range at the given cuts (only those strictly inside it). */
    static List<Climate.Parameter> split(Climate.Parameter p, float... cuts) {
        List<Climate.Parameter> out = new ArrayList<>();
        long lo = p.min();
        for (float cut : cuts) {
            long q = Climate.quantizeCoord(cut);
            if (q > lo && q < p.max()) {
                out.add(new Climate.Parameter(lo, q));
                lo = q;
            }
        }
        out.add(new Climate.Parameter(lo, p.max()));
        return out;
    }
}
