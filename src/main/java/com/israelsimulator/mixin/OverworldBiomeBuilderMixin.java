package com.israelsimulator.mixin;

import com.israelsimulator.world.biome.ModBiomes;
import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects Israel-Simulator custom biomes into vanilla Overworld biome generator.
 *
 * <p>Enables natural generation of Mediterranean Coast, Israeli Agriculture,
 * Urban Area, Jerusalem, Judean Desert, and Dead Sea biomes in survival worlds.</p>
 */
@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

    @Inject(method = "addBiomes", at = @At("RETURN"))
    private void injectIsraelBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, CallbackInfo ci) {
        // Skip injection during VanillaRegistries bootstrap validation, where only vanilla biomes exist
        boolean isVanillaBootstrap = StackWalker.getInstance().walk(frames ->
                frames.anyMatch(f -> f.getClassName().contains("VanillaRegistries"))
        );
        if (isVanillaBootstrap) {
            return;
        }

        // Mediterranean Coast: Warm, coastal moisture, shores and coastal shelf
        addIsraelSurfaceBiome(
                biomes,
                ModBiomes.MEDITERRANEAN_COAST,
                Climate.Parameter.span(0.40F, 0.70F),
                Climate.Parameter.span(0.00F, 0.35F),
                Climate.Parameter.span(-0.25F, -0.05F),
                Climate.Parameter.span(-0.40F, 0.20F),
                Climate.Parameter.span(-0.30F, 0.30F),
                0.0F
        );

        // Israeli Agriculture: Warm-temperate, fertile valley inland
        addIsraelSurfaceBiome(
                biomes,
                ModBiomes.ISRAELI_AGRICULTURE,
                Climate.Parameter.span(0.30F, 0.60F),
                Climate.Parameter.span(0.10F, 0.40F),
                Climate.Parameter.span(0.05F, 0.45F),
                Climate.Parameter.span(0.10F, 0.50F),
                Climate.Parameter.span(-0.40F, 0.20F),
                0.0F
        );

        // Urban Area: Mediterranean plains suited for cities and districts
        addIsraelSurfaceBiome(
                biomes,
                ModBiomes.URBAN_AREA,
                Climate.Parameter.span(0.45F, 0.75F),
                Climate.Parameter.span(-0.15F, 0.20F),
                Climate.Parameter.span(0.00F, 0.35F),
                Climate.Parameter.span(-0.20F, 0.25F),
                Climate.Parameter.span(0.00F, 0.35F),
                0.0F
        );

        // Jerusalem: Inland plateau / hill country
        addIsraelSurfaceBiome(
                biomes,
                ModBiomes.JERUSALEM,
                Climate.Parameter.span(0.35F, 0.65F),
                Climate.Parameter.span(-0.35F, 0.05F),
                Climate.Parameter.span(0.20F, 0.65F),
                Climate.Parameter.span(-0.60F, -0.10F),
                Climate.Parameter.span(0.15F, 0.55F),
                0.0F
        );

        // Judean Desert: Arid inland desert canyons
        addIsraelSurfaceBiome(
                biomes,
                ModBiomes.JUDEAN_DESERT,
                Climate.Parameter.span(0.65F, 1.00F),
                Climate.Parameter.span(-1.00F, -0.45F),
                Climate.Parameter.span(0.10F, 0.60F),
                Climate.Parameter.span(-0.30F, 0.40F),
                Climate.Parameter.span(-0.30F, 0.30F),
                0.0F
        );

        // Dead Sea: Deep basin depression, hyper-arid and mineral-rich
        addIsraelSurfaceBiome(
                biomes,
                ModBiomes.DEAD_SEA,
                Climate.Parameter.span(0.80F, 1.00F),
                Climate.Parameter.span(-0.80F, -0.20F),
                Climate.Parameter.span(0.25F, 0.70F),
                Climate.Parameter.span(0.40F, 0.90F),
                Climate.Parameter.span(0.30F, 0.80F),
                0.0F
        );
    }

    private static void addIsraelSurfaceBiome(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes,
            ResourceKey<Biome> biome,
            Climate.Parameter temperature,
            Climate.Parameter humidity,
            Climate.Parameter continentalness,
            Climate.Parameter erosion,
            Climate.Parameter weirdness,
            float offset
    ) {
        // Flat surface / low elevation
        biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(0.0F), weirdness, offset), biome));
        // Elevated / hill surface
        biomes.accept(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(1.0F), weirdness, offset), biome));
    }
}
