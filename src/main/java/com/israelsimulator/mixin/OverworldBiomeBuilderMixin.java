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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects Israel-Simulator biomes into the Overworld multi-noise generator as a
 * hot/dry West→East strip (coast → urban → agriculture → jerusalem → judean /
 * dead_sea weirdness pocket). Surface depth 0.0 only.
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
        IsraelBiomeClimateParams.addAll(biomes);
    }
}
