package com.israelsimulator.deadsea;

import com.israelsimulator.world.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative mechanics for the Dead Sea region (GAME_DESIGN.md §20).
 * Handles hyper-saline high-buoyancy physics and environmental water properties.
 */
public final class DeadSeaMechanics {
    public static final double BUOYANCY_LIFT = 0.045;
    public static final double MAX_BUOYANT_VELOCITY = 0.08;

    private DeadSeaMechanics() {}

    /**
     * Checks if the position resides within the Dead Sea biome.
     */
    public static boolean isDeadSeaBiome(Level level, BlockPos pos) {
        return level.getBiome(pos).is(ModBiomes.DEAD_SEA);
    }

    /**
     * Pure calculation of vertical velocity under Dead Sea hyper-buoyancy.
     * When crouching, the entity may swim downwards; otherwise, extreme salinity pushes them up.
     */
    public static double calculateBuoyancyVelocity(double currentVy, boolean isCrouching) {
        if (isCrouching) {
            return Math.max(-0.12, currentVy - 0.01);
        }
        if (currentVy < MAX_BUOYANT_VELOCITY) {
            return Math.min(MAX_BUOYANT_VELOCITY, currentVy + BUOYANCY_LIFT);
        }
        return currentVy;
    }

    /**
     * Applies high-buoyancy upward force to entities swimming in the Dead Sea.
     * Returns true if buoyancy was applied.
     */
    public static boolean applyBuoyancy(LivingEntity entity) {
        if (!entity.isInWater()) {
            return false;
        }

        Level level = entity.level();
        if (level.isClientSide()) {
            return false;
        }

        if (!isDeadSeaBiome(level, entity.blockPosition())) {
            return false;
        }

        Vec3 movement = entity.getDeltaMovement();
        boolean crouching = entity.isCrouching();
        double newVy = calculateBuoyancyVelocity(movement.y, crouching);

        entity.setDeltaMovement(movement.x, newVy, movement.z);
        entity.resetFallDistance();

        // Saline eye-stinging check: prolonged submergence without goggles or helmet
        if (entity.isEyeInFluid(FluidTags.WATER) && entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            if (entity.tickCount % 40 == 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, false, false, true));
            }
        }

        return true;
    }
}
