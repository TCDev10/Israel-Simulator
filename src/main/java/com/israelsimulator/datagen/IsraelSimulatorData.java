package com.israelsimulator.datagen;

import com.israelsimulator.IsraelSimulator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data-generation entry.
 */
public final class IsraelSimulatorData {
    private IsraelSimulatorData() {}

    public static void register(IEventBus modEventBus) {
        // Safe: no listeners registered at runtime to prevent abstract GatherDataEvent crash
    }
}
