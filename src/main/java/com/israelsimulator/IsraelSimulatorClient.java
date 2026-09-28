package com.israelsimulator;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only entrypoint. This class is never loaded on a dedicated server, so it is
 * safe to reference client-side code here.
 */
@Mod(value = IsraelSimulator.MOD_ID, dist = Dist.CLIENT)
public class IsraelSimulatorClient {
    public IsraelSimulatorClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on the mod > clicking on config.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        IsraelSimulator.LOGGER.info("Israel-Simulator client setup");
    }
}
