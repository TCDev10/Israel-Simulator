package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.effect.DairyDigestionEffect;
import com.israelsimulator.effect.FreedomEffect;
import com.israelsimulator.effect.MeatDigestionEffect;
import com.israelsimulator.effect.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Mob-effect registry for Israel-Simulator effects (GAME_DESIGN.md §11, §18, §35).
 */
public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, IsraelSimulator.MOD_ID);

    private ModMobEffects() {}

    public static void register(IEventBus modEventBus) {
        ModEffects.BLESSED = MOB_EFFECTS.register("blessed", BlessedEffect::new);
        ModEffects.BLESSED_TRADER = MOB_EFFECTS.register("blessed_trader", com.israelsimulator.effect.BlessedTraderEffect::new);
        ModEffects.FREEDOM = MOB_EFFECTS.register("freedom", FreedomEffect::new);
        ModEffects.MEAT_DIGESTION = MOB_EFFECTS.register("meat_digestion", MeatDigestionEffect::new);
        ModEffects.DAIRY_DIGESTION = MOB_EFFECTS.register("dairy_digestion", DairyDigestionEffect::new);
        MOB_EFFECTS.register(modEventBus);
    }
}
