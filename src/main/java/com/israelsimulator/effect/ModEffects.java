package com.israelsimulator.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

/**
 * Central access point for effect Holders registered in {@link com.israelsimulator.registry.ModMobEffects}.
 */
public final class ModEffects {
    public static Holder<MobEffect> BLESSED;
    public static Holder<MobEffect> BLESSED_TRADER;
    public static Holder<MobEffect> FREEDOM;
    public static Holder<MobEffect> MEAT_DIGESTION;
    public static Holder<MobEffect> DAIRY_DIGESTION;

    private ModEffects() {}
}
