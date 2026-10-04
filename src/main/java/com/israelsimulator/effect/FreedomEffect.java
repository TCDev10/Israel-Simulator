package com.israelsimulator.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Freedom effect: granted by the First Amendment collectible (GAME_DESIGN.md §35).
 * Server-authoritative effect that removes negative movement restrictions (slowness,
 * mining fatigue).
 */
public class FreedomEffect extends MobEffect {
    public FreedomEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4A90E2);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.hasEffect(MobEffects.SLOWNESS)) {
            entity.removeEffect(MobEffects.SLOWNESS);
        }
        if (entity.hasEffect(MobEffects.MINING_FATIGUE)) {
            entity.removeEffect(MobEffects.MINING_FATIGUE);
        }
        return true;
    }
}
