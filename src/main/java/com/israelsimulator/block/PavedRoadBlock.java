package com.israelsimulator.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Paved road block providing slight speed boost to pedestrians (GAME_DESIGN.md §32, TODO §47 Walking support).
 */
public class PavedRoadBlock extends Block {

    public PavedRoadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            // Provide Speed I for 3 seconds while walking along roads and sidewalks
            living.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, false, false, true));
        }
        super.stepOn(level, pos, state, entity);
    }
}
