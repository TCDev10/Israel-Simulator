package com.israelsimulator.world.biome;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

/**
 * Hot/dry West→East Israeli biome strip for Overworld multi-noise injection.
 *
 * <p>Aligned with vanilla temperature index 4 (0.55–1.0) and dry humidity
 * (−1.0––0.35), partitioned by continentalness. {@code dead_sea} is a weirdness
 * pocket sharing Judean Desert T/H/C/E. Surface depth {@code 0.0} only.</p>
 */
public final class IsraelBiomeClimateParams {
    public record Slot(
            ResourceKey<Biome> biome,
            float tempMin,
            float tempMax,
            float humMin,
            float humMax,
            float contMin,
            float contMax,
            float erosMin,
            float erosMax,
            float weirdMin,
            float weirdMax
    ) {
        public boolean contains(float t, float h, float c, float e, float w) {
            return t >= tempMin && t <= tempMax
                    && h >= humMin && h <= humMax
                    && c >= contMin && c <= contMax
                    && e >= erosMin && e <= erosMax
                    && w >= weirdMin && w <= weirdMax;
        }

        /** Squared distance to box (0 if inside). */
        public double dist2(float t, float h, float c, float e, float w) {
            return axis(t, tempMin, tempMax)
                    + axis(h, humMin, humMax)
                    + axis(c, contMin, contMax)
                    + axis(e, erosMin, erosMax)
                    + axis(w, weirdMin, weirdMax);
        }

        private static double axis(float v, float min, float max) {
            if (v < min) {
                double d = min - v;
                return d * d;
            }
            if (v > max) {
                double d = v - max;
                return d * d;
            }
            return 0.0;
        }
    }

    /** Ordered West→East strip. */
    public static final List<Slot> SLOTS = List.of(
            new Slot(ModBiomes.MEDITERRANEAN_COAST, 0.55F, 0.85F, -0.35F, 0.05F, -0.19F, -0.05F, 0.05F, 0.45F, -0.40F, 0.40F),
            new Slot(ModBiomes.URBAN_AREA, 0.55F, 0.90F, -0.55F, -0.15F, -0.05F, 0.15F, -0.22F, 0.45F, -0.20F, 0.35F),
            new Slot(ModBiomes.ISRAELI_AGRICULTURE, 0.55F, 0.85F, -0.35F, 0.00F, 0.05F, 0.28F, -0.375F, 0.05F, -0.45F, 0.15F),
            new Slot(ModBiomes.JERUSALEM, 0.55F, 0.80F, -0.70F, -0.25F, 0.22F, 0.55F, -0.78F, -0.22F, 0.00F, 0.45F),
            new Slot(ModBiomes.JUDEAN_DESERT, 0.60F, 1.00F, -1.00F, -0.30F, 0.28F, 0.85F, -0.40F, 0.45F, -1.00F, 0.25F),
            new Slot(ModBiomes.DEAD_SEA, 0.60F, 1.00F, -1.00F, -0.30F, 0.28F, 0.85F, -0.40F, 0.45F, 0.25F, 1.00F)
    );

    private IsraelBiomeClimateParams() {}

    public static void addAll(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
        for (Slot slot : SLOTS) {
            biomes.accept(Pair.of(
                    Climate.parameters(
                            Climate.Parameter.span(slot.tempMin(), slot.tempMax()),
                            Climate.Parameter.span(slot.humMin(), slot.humMax()),
                            Climate.Parameter.span(slot.contMin(), slot.contMax()),
                            Climate.Parameter.span(slot.erosMin(), slot.erosMax()),
                            Climate.Parameter.point(0.0F),
                            Climate.Parameter.span(slot.weirdMin(), slot.weirdMax()),
                            0.0F
                    ),
                    slot.biome()
            ));
        }
    }

    /** Nearest Israeli slot by box distance; null if every dist2 is infinite (never). */
    public static Slot nearest(float t, float h, float c, float e, float w) {
        Slot best = null;
        double bestD = Double.POSITIVE_INFINITY;
        for (Slot slot : SLOTS) {
            double d = slot.dist2(t, h, c, e, w);
            if (d < bestD) {
                bestD = d;
                best = slot;
            }
        }
        return best;
    }
}
