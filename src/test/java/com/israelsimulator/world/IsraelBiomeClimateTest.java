package com.israelsimulator.world;

import com.israelsimulator.world.biome.IsraelBiomeClimateParams;
import com.israelsimulator.world.biome.ModBiomes;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Israeli strip shares vanilla's hot climate (upper weirdness half of each hot entry). Vanilla's OverworldBiomeBuilder
 * needs a running FML loader, so these tests feed vanilla-shaped boxes (same band edges) through wrap().
 */
class IsraelBiomeClimateTest {

    private static final float[] HUM = {-1F, -0.35F, -0.1F, 0.1F, 0.3F, 1F};
    private static final float[] CONT = {-0.19F, -0.11F, 0.03F, 0.3F, 1F};
    private static final float[] ERO = {-1F, -0.78F, -0.375F, -0.2225F, 0.05F, 0.45F, 0.55F, 1F};

    private static Climate.ParameterPoint box(float h0, float h1, float c0, float c1, float e0, float e1) {
        return Climate.parameters(Climate.Parameter.span(0.55F, 1F), Climate.Parameter.span(h0, h1),
                Climate.Parameter.span(c0, c1), Climate.Parameter.span(e0, e1), Climate.Parameter.point(0F),
                Climate.Parameter.span(-1F, 1F), 0F);
    }

    /** One vanilla-shaped hot-climate grid (desert/badlands/savanna mix) plus one untouched biome. */
    private static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> vanillaGrid() {
        List<ResourceKey<Biome>> hot = List.of(Biomes.DESERT, Biomes.BADLANDS, Biomes.SAVANNA, Biomes.ERODED_BADLANDS);
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out = new ArrayList<>();
        int i = 0;
        for (int h = 0; h < HUM.length - 1; h++)
            for (int c = 0; c < CONT.length - 1; c++)
                for (int e = 0; e < ERO.length - 1; e++)
                    out.add(Pair.of(box(HUM[h], HUM[h + 1], CONT[c], CONT[c + 1], ERO[e], ERO[e + 1]), hot.get(i++ % hot.size())));
        out.add(Pair.of(box(-1F, 1F, -0.19F, 1F, -1F, 1F), Biomes.PLAINS));
        return out;
    }

    private static double vol(Climate.ParameterPoint p) {
        return span(p.temperature()) * span(p.humidity()) * span(p.continentalness()) * span(p.erosion()) * span(p.weirdness());
    }

    private static double span(Climate.Parameter p) {
        return Math.max(p.max() - p.min(), 1) / 10000.0;
    }

    private static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> wrapped() {
        List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> out = new ArrayList<>();
        var w = IsraelBiomeClimateParams.wrap(out::add);
        vanillaGrid().forEach(w);
        return out;
    }

    @Test
    @DisplayName("All six Israeli biomes take climate space; vanilla hot biomes keep half of theirs")
    void allSixPresentAndVanillaKept() {
        Map<ResourceKey<Biome>, Double> v = new HashMap<>();
        for (var p : wrapped()) v.merge(p.getSecond(), vol(p.getFirst()), Double::sum);
        for (ResourceKey<Biome> key : ModBiomes.all()) {
            assertTrue(v.getOrDefault(key, 0.0) > 0, "no climate space for " + key.identifier());
        }
        assertTrue(v.getOrDefault(Biomes.DESERT, 0.0) > 0, "vanilla desert must stay");
        assertTrue(v.getOrDefault(Biomes.PLAINS, 0.0) > 0, "non-hot biomes untouched");
        double isr = ModBiomes.all().stream().mapToDouble(k -> v.getOrDefault(k, 0.0)).sum();
        for (ResourceKey<Biome> key : ModBiomes.all()) {
            assertTrue(v.get(key) / isr > 0.03, key.identifier() + " too small: " + v.get(key) / isr);
        }
    }

    @Test
    @DisplayName("Each hot entry is split exactly in half: vanilla keeps one weirdness half, Israel the other")
    void sharedPartition() {
        for (var pair : vanillaGrid()) {
            var repl = IsraelBiomeClimateParams.remap(pair.getFirst(), pair.getSecond());
            assertEquals(IsraelBiomeClimateParams.TAKEN.contains(pair.getSecond()), repl != null, "take decision for " + pair);
            if (repl == null) continue;
            double total = vol(pair.getFirst()), vanilla = 0, israel = 0;
            for (var r : repl) {
                assertEquals(pair.getFirst().temperature(), r.getFirst().temperature());
                assertEquals(pair.getFirst().depth(), r.getFirst().depth());
                if (r.getSecond().equals(pair.getSecond())) {
                    vanilla += vol(r.getFirst());
                    assertTrue(r.getFirst().weirdness().max() <= r.getFirst().weirdness().min() + (pair.getFirst().weirdness().max() - pair.getFirst().weirdness().min()) / 2 + 1);
                } else {
                    assertTrue(ModBiomes.all().contains(r.getSecond()));
                    israel += vol(r.getFirst());
                }
            }
            assertEquals(total, vanilla + israel, total * 1e-9);
            assertEquals(0.5, vanilla / total, 0.01, "vanilla keeps half");
        }
        Map<ResourceKey<Biome>, Double> v = new HashMap<>();
        for (var p : wrapped()) v.merge(p.getSecond(), vol(p.getFirst()), Double::sum);
        for (ResourceKey<Biome> k : List.of(Biomes.DESERT, Biomes.BADLANDS, Biomes.SAVANNA, Biomes.ERODED_BADLANDS)) {
            assertTrue(v.getOrDefault(k, 0.0) > 0, k.identifier() + " must remain");
        }
    }

    @Test
    @DisplayName("West→East: coast < urban < agriculture < judean/dead_sea by continentalness")
    void westToEastOrdering() {
        Map<ResourceKey<Biome>, double[]> acc = new HashMap<>();
        for (var p : wrapped()) {
            if (!ModBiomes.all().contains(p.getSecond())) continue;
            double w = vol(p.getFirst());
            double c = (p.getFirst().continentalness().min() + p.getFirst().continentalness().max()) / 2e4;
            double[] a = acc.computeIfAbsent(p.getSecond(), k -> new double[2]);
            a[0] += w * c;
            a[1] += w;
        }
        double coast = mean(acc, ModBiomes.MEDITERRANEAN_COAST), urban = mean(acc, ModBiomes.URBAN_AREA);
        double agri = mean(acc, ModBiomes.ISRAELI_AGRICULTURE), judean = mean(acc, ModBiomes.JUDEAN_DESERT);
        assertTrue(coast < urban && urban < agri && agri < judean, coast + " " + urban + " " + agri + " " + judean);
        assertTrue(mean(acc, ModBiomes.DEAD_SEA) >= IsraelBiomeClimateParams.MID_MAX);
    }

    private static double mean(Map<ResourceKey<Biome>, double[]> acc, ResourceKey<Biome> k) {
        return acc.get(k)[0] / acc.get(k)[1];
    }

    @Test
    @DisplayName("dead_sea is the flat-erosion pocket of the judean_desert band")
    void deadSeaPocket() {
        assertEquals(ModBiomes.DEAD_SEA, IsraelBiomeClimateParams.pick(0.5F, 0.6F, -0.6F));
        assertEquals(ModBiomes.JUDEAN_DESERT, IsraelBiomeClimateParams.pick(0.5F, -0.1F, -0.6F));
        assertEquals(ModBiomes.JERUSALEM, IsraelBiomeClimateParams.pick(0.5F, -0.6F, -0.6F));
        assertEquals(ModBiomes.MEDITERRANEAN_COAST, IsraelBiomeClimateParams.pick(-0.15F, 0.0F, -0.6F));
    }
}
