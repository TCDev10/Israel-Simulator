package com.israelsimulator.network;

import com.israelsimulator.IsraelSimulator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Payload protocol registration. Individual payloads are added when a system needs them.
 */
public final class ModNetworking {
    public static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION);
        IsraelSimulator.LOGGER.debug("Israel-Simulator network protocol {} registered", PROTOCOL_VERSION);
    }
}
