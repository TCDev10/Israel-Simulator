package com.israelsimulator.world;

import com.israelsimulator.world.biome.IsraelBiomeClimateParams;
import com.israelsimulator.world.biome.IsraelBiomeClimateParams.Slot;
import com.israelsimulator.world.biome.ModBiomes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hot/dry Israeli strip: every biome reachable, dead_sea is a weirdness pocket,
 * surface depth 0.0 only.
 */
class IsraelBiomeClimateTest {

    @Test
    @DisplayName("All six Israeli biomes are registered as climate slots")
    void allSixSlotsPresent() {
        Set<ResourceKey<Biome>> keys = new HashSet<>();
        for (Slot slot : IsraelBiomeClimateParams.SLOTS) {
            keys.add(slot.biome());
        }
        assertEquals(6, keys.size());
        for (ResourceKey<Biome> key : ModBiomes.all()) {
            assertTrue(keys.contains(key), "missing slot for " + key.identifier());
        }
    }

    @Test
    @DisplayName("Injected points use depth 0.0 only (no depth 1.0)")
    void depthZeroOnly() {
        AtomicInteger points = new AtomicInteger();
        IsraelBiomeClimateParams.addAll(pair -> {
            points.incrementAndGet();
            Climate.ParameterPoint p = pair.getFirst();
            assertEquals(0L, p.depth().min(), "depth min must be 0");
            assertEquals(0L, p.depth().max(), "depth max must be 0 (point 0.0 only)");
        });
        assertEquals(6, points.get(), "one ParameterPoint per Israeli biome");
    }

    @Test
    @DisplayName("Monte Carlo: every Israeli biome wins some samples; dead_sea > 0")
    void eachBiomeReachableIncludingDeadSea() {
        Map<ResourceKey<Biome>, Integer> wins = new HashMap<>();
        for (Slot slot : IsraelBiomeClimateParams.SLOTS) {
            wins.put(slot.biome(), 0);
        }
        // Dense grid over the hot/dry inland band
        int samples = 0;
        for (float t = 0.55F; t <= 1.001F; t += 0.05F) {
            for (float h = -1.0F; h <= 0.051F; h += 0.05F) {
                for (float c = -0.19F; c <= 0.851F; c += 0.05F) {
                    for (float e = -0.78F; e <= 0.451F; e += 0.08F) {
                        for (float w = -1.0F; w <= 1.001F; w += 0.08F) {
                            samples++;
                            Slot nearest = IsraelBiomeClimateParams.nearest(t, h, c, e, w);
                            // Only count when sample is inside some slot (true win vs empty)
                            if (nearest != null && nearest.contains(t, h, c, e, w)) {
                                wins.merge(nearest.biome(), 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        assertTrue(samples > 1000, "grid too small: " + samples);
        for (Slot slot : IsraelBiomeClimateParams.SLOTS) {
            int n = wins.getOrDefault(slot.biome(), 0);
            assertTrue(n > 0, slot.biome().identifier() + " never won an inside-slot sample; wins=" + wins);
        }
        assertTrue(wins.get(ModBiomes.DEAD_SEA) > 0, "dead_sea must be reachable");
        // dead_sea should be a meaningful pocket next to judean, not a rounding crumb
        assertTrue(wins.get(ModBiomes.DEAD_SEA) >= 10, "dead_sea too rare in grid: " + wins);
    }

    @Test
    @DisplayName("dead_sea shares arid inland band with judean_desert and splits on weirdness")
    void deadSeaIsWeirdnessPocketOfJudean() {
        Slot judean = null;
        Slot dead = null;
        for (Slot slot : IsraelBiomeClimateParams.SLOTS) {
            if (slot.biome().equals(ModBiomes.JUDEAN_DESERT)) {
                judean = slot;
            }
            if (slot.biome().equals(ModBiomes.DEAD_SEA)) {
                dead = slot;
            }
        }
        assertNotNull(judean);
        assertNotNull(dead);
        // Contiguous weirdness: judean max meets/overlaps dead min
        assertEquals(0.00F, dead.weirdMin(), 1e-5f, "dead_sea weirdness pocket starts at 0.00");
        assertEquals(0.00F, judean.weirdMax(), 1e-5f, "judean weirdness ends where dead_sea begins");
        // Overlapping arid inland envelope
        assertTrue(overlap(judean.tempMin(), judean.tempMax(), dead.tempMin(), dead.tempMax()));
        assertTrue(overlap(judean.humMin(), judean.humMax(), dead.humMin(), dead.humMax()));
        assertTrue(overlap(judean.contMin(), judean.contMax(), dead.contMin(), dead.contMax()));
        assertTrue(overlap(judean.erosMin(), judean.erosMax(), dead.erosMin(), dead.erosMax()));
    }

    @Test
    @DisplayName("Jerusalem and Dead Sea share climate boundary for natural geographic adjacency")
    void jerusalemAndDeadSeaAdjacent() {
        Slot jerusalem = null;
        Slot dead = null;
        for (Slot slot : IsraelBiomeClimateParams.SLOTS) {
            if (slot.biome().equals(ModBiomes.JERUSALEM)) {
                jerusalem = slot;
            }
            if (slot.biome().equals(ModBiomes.DEAD_SEA)) {
                dead = slot;
            }
        }
        assertNotNull(jerusalem);
        assertNotNull(dead);
        // Shared temperature band
        assertTrue(overlap(jerusalem.tempMin(), jerusalem.tempMax(), dead.tempMin(), dead.tempMax()),
                "Jerusalem and Dead Sea must overlap in temperature");
        // Shared humidity band
        assertTrue(overlap(jerusalem.humMin(), jerusalem.humMax(), dead.humMin(), dead.humMax()),
                "Jerusalem and Dead Sea must overlap in humidity");
        // Contiguous/overlapping continentalness (Jerusalem hills transition to Dead Sea valley)
        assertTrue(overlap(jerusalem.contMin(), jerusalem.contMax(), dead.contMin(), dead.contMax()),
                "Jerusalem and Dead Sea must overlap in continentalness");
        // Overlapping weirdness so Dead Sea appears in Jerusalem's weirdness spectrum
        assertTrue(overlap(jerusalem.weirdMin(), jerusalem.weirdMax(), dead.weirdMin(), dead.weirdMax()),
                "Jerusalem and Dead Sea must overlap in weirdness");
    }

    private static boolean overlap(float a0, float a1, float b0, float b1) {
        return a0 <= b1 && b0 <= a1;
    }
}
