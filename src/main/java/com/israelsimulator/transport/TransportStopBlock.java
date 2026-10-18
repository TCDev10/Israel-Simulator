package com.israelsimulator.transport;

import com.israelsimulator.audio.ModAudioManager;
import com.israelsimulator.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import java.util.UUID;

/**
 * Interactive transport stop block (Bus Stop, Train Station, Taxi Stand, Boat Pier).
 * Handles passenger transit, fare checking, and multiplayer server-side teleportation (GAME_DESIGN.md §32, TODO §47).
 *
 * <p>A stop in the Urban Area or Jerusalem biome is a station of the Tel Aviv – Jerusalem train
 * ({@link TrainLine}); right-click it to ride to the other end (Rav-Kav or {@link TrainLine#FARE} Shekels).</p>
 */
public class TransportStopBlock extends Block {

    /** Three seconds between player transits, to stop spam and repeated fare charges. */
    public static final long TRANSIT_COOLDOWN_TICKS = 60L;
    /** Villagers only trigger the stop sound again after this interval. */
    public static final long NPC_SOUND_INTERVAL_TICKS = 600L;

    public TransportStopBlock(Properties properties) {
        super(properties);
    }

    public static boolean isPlayerOnCooldown(TransitCooldowns cooldowns, UUID entityId, long currentGameTime) {
        if (cooldowns == null || entityId == null) {
            return false;
        }
        Long last = cooldowns.getLastUseTime(entityId);
        return last != null && (currentGameTime - last) < TRANSIT_COOLDOWN_TICKS;
    }

    public static boolean isNpcSoundOnInterval(TransitCooldowns cooldowns, UUID entityId, long currentGameTime) {
        if (cooldowns == null || entityId == null) {
            return false;
        }
        Long last = cooldowns.getLastUseTime(entityId);
        return last != null && (currentGameTime - last) <= NPC_SOUND_INTERVAL_TICKS;
    }

    public static void recordTransit(TransitCooldowns cooldowns, UUID entityId, long time) {
        if (cooldowns != null) {
            cooldowns.setLastUseTime(entityId, time);
        }
    }


    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return handleInteraction(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return handleInteraction(level, pos, player);
    }

    private InteractionResult handleInteraction(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        TransitCooldowns cooldowns = TransitCooldowns.get(serverLevel);
        long now = serverLevel.getGameTime();
        if (isPlayerOnCooldown(cooldowns, player.getUUID(), now)) {
            Long lastTransit = cooldowns.getLastUseTime(player.getUUID());
            long remaining = (TRANSIT_COOLDOWN_TICKS - (now - lastTransit)) / 20L + 1;
            player.sendSystemMessage(
                    Component.translatable("message.israel_simulator.transit_cooldown", remaining)
                            .withStyle(ChatFormatting.YELLOW)
            );
            return InteractionResult.CONSUME;
        }

        // Tel Aviv – Jerusalem train: this stop is a station at one end of the line
        TransitStations.get(serverLevel).add(pos);
        if (TrainLine.terminalAt(serverLevel, pos).isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.train_no_line").withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }
        TrainLine.Trip trip = TrainLine.plan(serverLevel, pos);
        if (trip == null) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.no_route").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        Component destination = Component.translatable(trip.to().nameKey());

        // Check fare: Rav-Kav pass vs Shekels
        boolean hasRavKav = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (!invStack.isEmpty() && invStack.getItem() instanceof RavKavItem) {
                hasRavKav = true;
                break;
            }
        }

        int fare = TrainLine.FARE;
        if (!hasRavKav && fare > 0) {
            int shekelCount = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.is(ModItems.SHEKEL.get())) {
                    shekelCount += invStack.getCount();
                }
            }

            if (shekelCount < fare) {
                player.sendSystemMessage(
                        Component.translatable("message.israel_simulator.insufficient_fare", destination, fare)
                                .withStyle(ChatFormatting.RED)
                );
                return InteractionResult.CONSUME;
            }

            // Deduct fare
            int remainingToDeduct = fare;
            for (int i = 0; i < player.getInventory().getContainerSize() && remainingToDeduct > 0; i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack.is(ModItems.SHEKEL.get())) {
                    int take = Math.min(remainingToDeduct, invStack.getCount());
                    invStack.shrink(take);
                    remainingToDeduct -= take;
                }
            }
        }

        recordTransit(cooldowns, player.getUUID(), now);

        // Departure: whistle at the departing platform, a short dark "tunnel" while travelling
        ModAudioManager.playTransitSound(level, pos, TransportType.TRAIN.name());
        serverPlayer.sendSystemMessage(Component.translatable("message.israel_simulator.train_departure", destination)
                .withStyle(ChatFormatting.AQUA));
        serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 30, 0, false, false));
        BlockPos dest = trip.arrival();
        serverPlayer.teleportTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
        serverPlayer.fallDistance = 0.0F;
        ModAudioManager.playTransitSound(level, dest, TransportType.TRAIN.name());

        String paymentInfo = hasRavKav ? "(Rav-Kav)" : "(-" + fare + " ₪)";
        serverPlayer.sendSystemMessage(
                Component.translatable(trip.toStation() ? "message.israel_simulator.train_arrival_station"
                                : "message.israel_simulator.train_arrival_city", destination, paymentInfo)
                        .withStyle(ChatFormatting.GREEN)
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !oldState.is(state.getBlock())) {
            TransitStations.get(serverLevel).add(pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (!(level.getBlockState(pos).getBlock() instanceof TransportStopBlock)) {
            TransitStations.get(level).remove(pos);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        // NPC transport usage: nearby villagers can trigger route simulation
        if (!level.isClientSide() && entity instanceof Villager villager && level instanceof ServerLevel serverLevel) {
            TransitCooldowns cooldowns = TransitCooldowns.get(serverLevel);
            long now = serverLevel.getGameTime();
            if (!isNpcSoundOnInterval(cooldowns, villager.getUUID(), now)) {
                recordTransit(cooldowns, villager.getUUID(), now);
                ModAudioManager.playTransitSound(level, pos, "BUS");
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
