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
        modEventBus.addListener(IsraelSimulatorData::gatherData);
    }

    private static void gatherData(GatherDataEvent event) {
        event.createProvider(ModLangProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider((output, lookup) -> new ModItemTagsProvider(output, lookup));
        IsraelSimulator.LOGGER.info("Israel-Simulator data generation registered");
    }
}
