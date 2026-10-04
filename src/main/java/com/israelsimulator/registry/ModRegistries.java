package com.israelsimulator.registry;

import com.israelsimulator.network.ModNetworking;
import com.israelsimulator.world.ModWorldGen;
import net.neoforged.bus.api.IEventBus;

/**
 * Deterministic registration of DeferredRegisters and mod-bus infrastructure.
 *
 * <p>Order is fixed and must not depend on class-initialization side effects.</p>
 */
public final class ModRegistries {
    private ModRegistries() {}

    public static void register(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModSoundEvents.register(modEventBus);
        ModWorldGen.register(modEventBus);
        ModNetworking.register(modEventBus);
        // Note: Data generators are triggered via the runData task, not during game runtime.
    }
}
