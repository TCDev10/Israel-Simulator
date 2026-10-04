package com.israelsimulator.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Neutral status effect indicating the waiting period after consuming meat
 * under the kosher system (GAME_DESIGN.md §18).
 */
public class MeatDigestionEffect extends MobEffect {
    public MeatDigestionEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8B2500);
    }
}
