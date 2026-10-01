package com.israelsimulator.datagen;

import com.israelsimulator.IsraelSimulator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data-generation entry. Providers are registered when there is content to generate.
 */
public final class IsraelSimulatorData {
    private IsraelSimulatorData() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(IsraelSimulatorData::gatherClientData);
    }

    private static void gatherClientData(GatherDataEvent.Client event) {
        IsraelSimulator.LOGGER.debug("Israel-Simulator data generation (no providers yet)");
    }
}
