package com.israelsimulator.transport;

import com.israelsimulator.audio.ModAudioManager;
import com.israelsimulator.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interactive transport stop block (Bus Stop, Train Station, Taxi Stand, Boat Pier).
 * Handles passenger transit, fare checking, and multiplayer server-side teleportation (GAME_DESIGN.md §32, TODO §47).
 */
public class TransportStopBlock extends Block {

    private static final Map<UUID, Long> LAST_TRANSIT_TIME = new ConcurrentHashMap<>();
    private static final long TRANSIT_COOLDOWN_TICKS = 60L; // 3 seconds cooldown to prevent spam

    public TransportStopBlock(Properties properties) {
        super(properties);
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

        long now = level.getGameTime();
        Long lastTransit = LAST_TRANSIT_TIME.get(player.getUUID());
        if (lastTransit != null && now - lastTransit < TRANSIT_COOLDOWN_TICKS) {
            long remaining = (TRANSIT_COOLDOWN_TICKS - (now - lastTransit)) / 20L + 1;
            player.sendSystemMessage(
                    Component.translatable("message.israel_simulator.transit_cooldown", remaining)
                            .withStyle(ChatFormatting.YELLOW)
            );
            return InteractionResult.CONSUME;
        }

        // Determine destination stop
        TransportNetwork.TransportStop nextStop = TransportNetwork.getNextStop("tel_aviv_central");
        if (nextStop == null) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.no_route").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }

        // Check fare: Rav-Kav pass vs Shekels
        boolean hasRavKav = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (!invStack.isEmpty() && invStack.getItem() instanceof RavKavItem) {
                hasRavKav = true;
                break;
            }
        }

        int fare = nextStop.primaryType().getFareInShekels();
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
                        Component.translatable("message.israel_simulator.insufficient_fare", fare, nextStop.displayName())
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

        LAST_TRANSIT_TIME.put(player.getUUID(), now);

        // Sound effect
        ModAudioManager.playTransitSound(level, pos, nextStop.primaryType().name());

        // Perform safe teleportation
        BlockPos dest = nextStop.defaultPos();
        serverPlayer.teleportTo(dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5);

        String paymentInfo = hasRavKav ? "(Rav-Kav Pass)" : "(-" + fare + " Shekels)";
        serverPlayer.sendSystemMessage(
                Component.translatable("message.israel_simulator.transit_success", nextStop.displayName(), paymentInfo)
                        .withStyle(ChatFormatting.GREEN)
        );

        return InteractionResult.SUCCESS;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        // NPC transport usage: nearby villagers can trigger route simulation
        if (!level.isClientSide() && entity instanceof Villager villager) {
            long now = level.getGameTime();
            Long last = LAST_TRANSIT_TIME.get(villager.getUUID());
            if (last == null || now - last > 600L) { // Every 30s
                LAST_TRANSIT_TIME.put(villager.getUUID(), now);
                ModAudioManager.playTransitSound(level, pos, "BUS");
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
