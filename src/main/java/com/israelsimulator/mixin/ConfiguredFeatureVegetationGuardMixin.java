package com.israelsimulator.mixin;

import com.israelsimulator.world.structure.StructureVegetationGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Every (also nested) vegetation feature placement is refused next to a mod structure piece. */
@Mixin(ConfiguredFeature.class)
public abstract class ConfiguredFeatureVegetationGuardMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void israelSimulator$skipNearStructures(WorldGenLevel level, ChunkGenerator generator, RandomSource random,
                                                    BlockPos origin, CallbackInfoReturnable<Boolean> cir) {
        if (StructureVegetationGuard.blocks(origin)) {
            cir.setReturnValue(false);
        }
    }
}
