package com.israelsimulator.mixin;

import com.israelsimulator.world.structure.StructureVegetationGuard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hooks {@link StructureVegetationGuard} into per-chunk biome decoration. */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorVegetationGuardMixin {

    @Shadow @Final private Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;

    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
    private void israelSimulator$beginGuard(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager,
                                            CallbackInfo ci) {
        StructureVegetationGuard.begin(level, chunk);
    }

    @Inject(method = "applyBiomeDecoration", at = @At("RETURN"))
    private void israelSimulator$endGuard(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager,
                                          CallbackInfo ci) {
        StructureVegetationGuard.end(level, chunk);
    }

    @WrapOperation(method = "applyBiomeDecoration", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean israelSimulator$markVegetationStep(PlacedFeature feature, WorldGenLevel level, ChunkGenerator generator,
                                                       RandomSource random, BlockPos origin, Operation<Boolean> original) {
        List<FeatureSorter.StepFeatureData> steps = this.featuresPerStep.get();
        int vegetal = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();
        boolean vegetation = vegetal < steps.size() && steps.get(vegetal).features().contains(feature);
        StructureVegetationGuard.setVegetationStep(vegetation);
        try {
            return original.call(feature, level, generator, random, origin);
        } finally {
            StructureVegetationGuard.setVegetationStep(false);
        }
    }
}
