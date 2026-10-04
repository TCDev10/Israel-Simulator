package com.israelsimulator.client;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.client.renderer.BibiBossRenderer;
import com.israelsimulator.client.renderer.BibiGuardRenderer;
import com.israelsimulator.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only entrypoint. This class is never loaded on a dedicated server, so it is
 * safe to reference client-side code here.
 */
@Mod(value = IsraelSimulator.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID, value = Dist.CLIENT)
public class IsraelSimulatorClient {
    public IsraelSimulatorClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        IsraelSimulator.LOGGER.info("Israel-Simulator client setup");
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BIBI_BOSS.get(), BibiBossRenderer::new);
        event.registerEntityRenderer(ModEntities.BIBI_GUARD.get(), BibiGuardRenderer::new);
        event.registerEntityRenderer(ModEntities.BICYCLE.get(), com.israelsimulator.client.renderer.BicycleRenderer::new);
    }
}
