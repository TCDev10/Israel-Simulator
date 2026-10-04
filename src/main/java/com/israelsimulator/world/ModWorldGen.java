package com.israelsimulator.world;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.world.biome.ModBiomes;
import com.israelsimulator.world.feature.ModConfiguredFeatures;
import com.israelsimulator.world.feature.ModPlacedFeatures;
import com.israelsimulator.world.structure.ModStructures;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * World-generation registries (features, structure types, biomes, and placements).
 *
 * <p>Coordinates world generation foundation for Israel-Simulator as specified in
 * GAME_DESIGN.md (§5, §6, §19, §20, §21, §56).</p>
 */
public final class ModWorldGen {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, IsraelSimulator.MOD_ID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, IsraelSimulator.MOD_ID);

    private ModWorldGen() {}

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);

        // Pre-load static keys to guarantee deterministic registry order
        int biomeCount = ModBiomes.all().size();
        int featureCount = ModConfiguredFeatures.all().size();
        int placedCount = ModPlacedFeatures.all().size();
        int structureCount = ModStructures.allStructures().size();
        IsraelSimulator.LOGGER.info("Initialized world-gen keys: {} biomes, {} features, {} placed, {} structures",
                biomeCount, featureCount, placedCount, structureCount);
    }
}
