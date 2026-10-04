package com.israelsimulator.block;

import com.israelsimulator.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Agricultural grapevine crop block.
 *
 * <p>Grows on farmland across multiple stages and supports persistent vine harvesting:
 * when harvested at maturity, produce is dropped and the vine resets to an intermediate stage.</p>
 */
public class GrapevineBlock extends CropBlock {

    public GrapevineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.DATES.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int age = this.getAge(state);
        if (age >= this.getMaxAge()) {
            if (!level.isClientSide()) {
                int count = 1 + level.getRandom().nextInt(3);
                Block.popResource(level, pos, new ItemStack(ModItems.DATES.get(), count));
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, this.getStateForAge(3), Block.UPDATE_CLIENTS);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}

