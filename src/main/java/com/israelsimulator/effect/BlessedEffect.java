package com.israelsimulator.effect;

import com.israelsimulator.config.IsraelSimulatorConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Blessed effect: a beneficial, server-authoritative effect granted by spiritual systems
 * (e.g. Western Wall interaction). Applies periodic regeneration.
 *
 * <p>Duration/amplifier and removal conditions follow vanilla MobEffectInstance semantics:
 * the server owns the state, syncs it to clients, clears it on death, and persists it
 * across relog. Prevention of unintended stacking is handled by {@link #applyTo}.</p>
 */
public class BlessedEffect extends MobEffect {
    public BlessedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF5D76E);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 50 >> amplifier;
        if (interval <= 0) {
            interval = 1;
        }
        return duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
        return true;
    }

    /**
     * Grants Blessed to the target with configured duration/amplifier, refreshing an
     * existing instance instead of creating a stronger stacked one (anti-farming:
     * re-applying never exceeds the configured amplifier and does not accumulate).
     */
    public static void applyTo(LivingEntity entity) {
        int duration = IsraelSimulatorConfig.blessedDurationTicks();
        int amplifier = IsraelSimulatorConfig.blessedAmplifier();
        MobEffectInstance existing = entity.getEffect(ModEffects.BLESSED);
        if (existing != null) {
            int remaining = existing.getDuration();
            entity.addEffect(new MobEffectInstance(
                    ModEffects.BLESSED,
                    Math.max(remaining, duration),
                    Math.min(existing.getAmplifier(), amplifier)));
        } else {
            entity.addEffect(new MobEffectInstance(ModEffects.BLESSED, duration, amplifier));
        }
    }
}
