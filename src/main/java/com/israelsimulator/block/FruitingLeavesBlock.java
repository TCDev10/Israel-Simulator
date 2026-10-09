package com.israelsimulator.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Leaves block that produces harvestable fruit (olives, dates, citrus).
 *
 * <p>Fruit ripens through {@link #AGE} 0-3 (leaves, blossom, unripe, ripe) via random ticks or
 * bone meal. Right-click only harvests ripe leaves, which drop back to age 1 and must regrow,
 * like vanilla sweet berry bushes (GAME_DESIGN.md §19).</p>
 */
public class FruitingLeavesBlock extends TintedParticleLeavesBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int MAX_AGE = HarvestRules.MAX_AGE;

    private final Supplier<? extends Item> fruitSupplier;

    public FruitingLeavesBlock(float leafParticleChance, Supplier<? extends Item> fruitSupplier, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
        this.fruitSupplier = fruitSupplier;
        this.registerDefaultState(this.defaultBlockState().setValue(AGE, 0));
    }

    public Item getFruit() {
        return fruitSupplier.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return super.isRandomlyTicking(state) || HarvestRules.canGrow(state.getValue(AGE));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.decaying(state)) {
            super.randomTick(state, level, pos, random);
            return;
        }
        int age = state.getValue(AGE);
        if (HarvestRules.canGrow(age)
                && net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, random.nextInt(HarvestRules.GROWTH_CHANCE) == 0)) {
            BlockState grown = state.setValue(AGE, HarvestRules.ageAfterGrowth(age));
            level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
            net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grown));
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        // Let bone meal reach performBonemeal instead of being swallowed by the harvest.
        if (!HarvestRules.canHarvest(state.getValue(AGE)) && itemStack.is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        if (!HarvestRules.canHarvest(age)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        if (level instanceof ServerLevel serverLevel) {
            Item fruit = getFruit();
            if (fruit != null) {
                int count = 1 + serverLevel.getRandom().nextInt(2);
                Block.popResource(serverLevel, pos, new ItemStack(fruit, count));
            }
            serverLevel.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                    1.0F, 0.8F + serverLevel.getRandom().nextFloat() * 0.4F);
            BlockState harvested = state.setValue(AGE, HarvestRules.ageAfterHarvest(age));
            serverLevel.setBlock(pos, harvested, Block.UPDATE_CLIENTS);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, harvested));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return HarvestRules.canGrow(state.getValue(AGE));
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, HarvestRules.ageAfterGrowth(state.getValue(AGE))), Block.UPDATE_CLIENTS);
    }
}
