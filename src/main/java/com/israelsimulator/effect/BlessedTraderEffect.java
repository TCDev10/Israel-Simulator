package com.israelsimulator.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Blessed Trader effect (GAME_DESIGN.md §16.3, §47, TODO §41).
 * Bestows holy merchant favor, maximum villager trade discounts, and unlocks divine trades.
 */
public class BlessedTraderEffect extends MobEffect {

    public BlessedTraderEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
    }
}
