package com.israelsimulator;

import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.registry.ModRegistries;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Main entrypoint of the Israel-Simulator mod.
 *
 * <p>Registers content DeferredRegisters and common lifecycle hooks. Gameplay entries
 * are added to those registers in later milestones.</p>
 */
@Mod(IsraelSimulator.MOD_ID)
public class IsraelSimulator {
    public static final String MOD_ID = "israel_simulator";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IsraelSimulator(IEventBus modEventBus, ModContainer modContainer) {
        ModRegistries.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        // Gameplay configuration categories are introduced with the systems they configure
        // (rewards, cooldowns, event frequency, ...); no gameplay config exists yet.
        modContainer.registerConfig(ModConfig.Type.COMMON, IsraelSimulatorConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Israel-Simulator common setup");
    }
}
