package com.israelsimulator.world;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * World-generation registries (features and structure types).
 * Datapack-driven configured/placed features and structures are added later.
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
    }
}
