package com.israelsimulator.client;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.client.renderer.BibiBossRenderer;
import com.israelsimulator.client.renderer.BibiGuardRenderer;
import com.israelsimulator.client.westernwall.ClientWesternWallPrayer;
import com.israelsimulator.network.WesternWallPrayingPayload;
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
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = IsraelSimulator.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID, value = Dist.CLIENT)
public class IsraelSimulatorClient {
    public IsraelSimulatorClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(IsraelSimulatorClient::registerClientPayloads);
        modEventBus.addListener(com.israelsimulator.client.westernwall.WesternWallPrayerClientEvents::registerRenderStateModifiers);
    }

    private static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(WesternWallPrayingPayload.TYPE, (payload, context) -> ClientWesternWallPrayer.handle(payload));
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
        event.registerEntityRenderer(ModEntities.BIBI_MISSILE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.TRUMP_MINIBOSS.get(), com.israelsimulator.client.renderer.TrumpMinibossRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_AGENT.get(), com.israelsimulator.client.renderer.IceAgentRenderer::new);
        event.registerEntityRenderer(ModEntities.JEFFREY_EPSTEIN.get(), com.israelsimulator.client.renderer.JeffreyEpsteinRenderer::new);
        event.registerEntityRenderer(ModEntities.CHILD_ZOMBIE_MINION.get(), com.israelsimulator.client.renderer.ChildZombieMinionRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterParticleProviders(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(com.israelsimulator.registry.ModParticles.DOLLAR_BILL.get(),
                com.israelsimulator.client.particle.DollarBillParticle.Provider::new);
    }
}
