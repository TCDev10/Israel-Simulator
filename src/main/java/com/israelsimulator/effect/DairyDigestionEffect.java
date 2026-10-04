package com.israelsimulator.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Neutral status effect indicating the waiting period after consuming dairy
 * under the kosher system (GAME_DESIGN.md §18).
 */
public class DairyDigestionEffect extends MobEffect {
    public DairyDigestionEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFFFAF0);
    }
}
