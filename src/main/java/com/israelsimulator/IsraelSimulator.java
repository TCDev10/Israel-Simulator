package com.israelsimulator;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

/**
 * Main entrypoint of the Israel-Simulator mod.
 *
 * <p>This is the technical foundation milestone: content registries (items, blocks,
 * entities, world generation, ...) are added in later milestones on top of this
 * initialization skeleton.</p>
 */
@Mod(IsraelSimulator.MOD_ID)
public class IsraelSimulator {
    public static final String MOD_ID = "israel_simulator";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IsraelSimulator(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for game events we are interested in.
        // This is only needed because of the @SubscribeEvent handler below; remove it
        // together with onServerStarting if that handler is ever removed.
        NeoForge.EVENT_BUS.register(this);

        // Gameplay configuration categories are introduced with the systems they configure
        // (rewards, cooldowns, event frequency, ...); no gameplay config exists yet.
        modContainer.registerConfig(ModConfig.Type.COMMON, IsraelSimulatorConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Israel-Simulator common setup");
    }

    @SubscribeEvent
    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Israel-Simulator server starting");
    }
}
