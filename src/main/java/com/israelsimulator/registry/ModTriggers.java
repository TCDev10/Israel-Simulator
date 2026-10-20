package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.advancement.IsraelEventTrigger;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Advancement criterion triggers of the mod. */
public final class ModTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, IsraelSimulator.MOD_ID);

    /** {@code israel_simulator:mod_event}, see {@link IsraelEventTrigger}. */
    public static final DeferredHolder<CriterionTrigger<?>, IsraelEventTrigger> EVENT =
            TRIGGERS.register("mod_event", IsraelEventTrigger::new);

    private ModTriggers() {}

    public static void register(IEventBus modEventBus) {
        TRIGGERS.register(modEventBus);
    }
}
