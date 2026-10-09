package com.israelsimulator.network;

import com.israelsimulator.IsraelSimulator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    public static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // Client handler registered via RegisterClientPayloadHandlersEvent (client-only class)
        registrar.playToClient(WesternWallPrayingPayload.TYPE, WesternWallPrayingPayload.STREAM_CODEC);
        registrar.playToServer(ReloadWeaponPayload.TYPE, ReloadWeaponPayload.STREAM_CODEC, ReloadWeaponPayload::handle);
        IsraelSimulator.LOGGER.debug("Israel-Simulator network protocol {} registered", PROTOCOL_VERSION);
    }
}
