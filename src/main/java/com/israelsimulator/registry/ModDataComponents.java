package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item data components added by Israel-Simulator.
 */
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, IsraelSimulator.MOD_ID);

    /** Rounds currently loaded in a firearm's magazine (saved with the item and synced to the client). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAGAZINE =
            DATA_COMPONENTS.registerComponentType("magazine",
                    b -> b.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private ModDataComponents() {}

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
