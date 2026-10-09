package com.israelsimulator.mixin;

import com.israelsimulator.world.biome.IsraelBiomeClimateParams;
import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Puts the Israel-Simulator biomes into the Overworld multi-noise generator by wrapping the
 * biome consumer: the dry half of vanilla's hot climate (desert/savanna/badlands entries) is
 * re-assigned to the Israeli West→East strip (see {@link IsraelBiomeClimateParams}).
 */
@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

    @ModifyVariable(method = "addBiomes", at = @At("HEAD"), argsOnly = true)
    private Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> israelSimulator$takeOverHotDryClimate(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
        // Skip during VanillaRegistries bootstrap validation, where only vanilla biomes exist
        boolean isVanillaBootstrap = StackWalker.getInstance().walk(frames ->
                frames.anyMatch(f -> f.getClassName().contains("VanillaRegistries"))
        );
        return isVanillaBootstrap ? biomes : IsraelBiomeClimateParams.wrap(biomes);
    }
}
