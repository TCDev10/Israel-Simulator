package com.israelsimulator.transport;

import com.israelsimulator.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Item used to place a rideable bicycle onto the world (GAME_DESIGN.md §32, TODO §47).
 */
public class BicycleItem extends Item {

    public BicycleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        BicycleEntity bicycle = ModEntities.BICYCLE.get().create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (bicycle != null) {
            bicycle.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            bicycle.setYRot(context.getRotation());
            serverLevel.addFreshEntity(bicycle);
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
