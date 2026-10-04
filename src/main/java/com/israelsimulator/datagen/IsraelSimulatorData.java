package com.israelsimulator.datagen;

import com.israelsimulator.IsraelSimulator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data-generation entry. Uses explicit GatherDataEvent.Client and GatherDataEvent.Server
 * listeners since GatherDataEvent is abstract in NeoForge 26.2.
 */
public final class IsraelSimulatorData {
    private IsraelSimulatorData() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(IsraelSimulatorData::gatherClientData);
        modEventBus.addListener(IsraelSimulatorData::gatherServerData);
    }

    private static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ModLangProvider::new);
        IsraelSimulator.LOGGER.info("Israel-Simulator client data generation registered");
    }

    private static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider((output, lookup) -> new ModItemTagsProvider(output, lookup));
        IsraelSimulator.LOGGER.info("Israel-Simulator server data generation registered");
    }
}

