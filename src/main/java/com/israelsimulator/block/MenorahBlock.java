package com.israelsimulator.block;

import com.israelsimulator.festival.FestivalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Traditional eight-branched candelabrum with Shamash helper branch (GAME_DESIGN.md §36, TODO §36).
 * Features candle lighting progression (1–8), light emission scaling, and Hanukkah festival integration.
 */
public class MenorahBlock extends Block {

    public static final IntegerProperty CANDLES = IntegerProperty.create("candles", 0, 8);
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 6.0, 14.0, 15.0, 10.0);

    public MenorahBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CANDLES, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CANDLES);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isIgniter = stack.is(Items.FLINT_AND_STEEL)
                || stack.is(Items.FIRE_CHARGE)
                || stack.is(Items.TORCH);

        int currentCandles = state.getValue(CANDLES);

        if (isIgniter) {
            int targetCandles = currentCandles + 1;
            int hanukkahNight = FestivalManager.getHanukkahNight(level.getGameTime());
            if (hanukkahNight > 0 && targetCandles < hanukkahNight) {
                targetCandles = hanukkahNight; // Advance directly to current festival night
            }

            if (targetCandles > 8) {
                targetCandles = 8;
            }

            if (targetCandles > currentCandles) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(CANDLES, targetCandles), Block.UPDATE_ALL);
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                    if (targetCandles == 8) {
                        level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.2F);
                        player.sendSystemMessage(Component.translatable("message.israel_simulator.menorah_fully_lit"));
                    } else {
                        player.sendSystemMessage(Component.translatable("message.israel_simulator.menorah_lit", targetCandles));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        int currentCandles = state.getValue(CANDLES);

        if (player.isShiftKeyDown() && currentCandles > 0) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(CANDLES, 0), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("message.israel_simulator.menorah_extinguished"));
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.menorah_status", currentCandles));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int candles = state.getValue(CANDLES);
        if (candles <= 0) {
            return;
        }

        // Emit particles above lit branches
        double startX = pos.getX() + 0.2;
        double endX = pos.getX() + 0.8;
        double step = (endX - startX) / 8.0;
        double y = pos.getY() + 0.95;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < candles; i++) {
            if (random.nextFloat() < 0.4F) {
                double x = startX + (i * step);
                level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.005, 0.0);
                if (random.nextFloat() < 0.2F) {
                    level.addParticle(ParticleTypes.SMOKE, x, y + 0.05, z, 0.0, 0.005, 0.0);
                }
            }
        }
    }
}
