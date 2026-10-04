package com.israelsimulator.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Leaves block that produces harvestable fruit (olives, dates, citrus).
 *
 * <p>Supports right-click harvesting and block breaking drops per GAME_DESIGN.md §19.</p>
 */
public class FruitingLeavesBlock extends TintedParticleLeavesBlock {
    private final Supplier<? extends Item> fruitSupplier;

    public FruitingLeavesBlock(float leafParticleChance, Supplier<? extends Item> fruitSupplier, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
        this.fruitSupplier = fruitSupplier;
    }

    public Item getFruit() {
        return fruitSupplier.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            Item fruit = getFruit();
            if (fruit != null) {
                int count = 1 + level.getRandom().nextInt(2);
                Block.popResource(level, pos, new ItemStack(fruit, count));
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.SUCCESS;
    }
}

