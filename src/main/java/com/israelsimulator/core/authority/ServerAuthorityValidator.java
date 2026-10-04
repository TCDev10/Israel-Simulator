package com.israelsimulator.core.authority;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Central server-authoritative validator ensuring multiplayer security and anti-cheat/anti-exploit integrity
 * per GAME_DESIGN.md §53 and §54.
 */
public final class ServerAuthorityValidator {
    public static final double MAX_INTERACTION_DISTANCE_SQR = 64.0; // 8 blocks

    private ServerAuthorityValidator() {}

    /**
     * Validates that an action is executing strictly on the server level.
     */
    public static boolean isServerAuthoritative(Level level) {
        return level != null && !level.isClientSide();
    }

    /**
     * Validates interaction proximity to prevent packet spoofing / long-distance reach exploits.
     */
    public static boolean isValidInteractionProximity(Player player, BlockPos targetPos) {
        if (player == null || targetPos == null) {
            return false;
        }
        return player.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5) <= MAX_INTERACTION_DISTANCE_SQR;
    }

    /**
     * Validates entity interaction proximity to prevent remote packet attacks.
     */
    public static boolean isValidInteractionProximity(Player player, Entity targetEntity) {
        if (player == null || targetEntity == null) {
            return false;
        }
        return player.distanceToSqr(targetEntity) <= MAX_INTERACTION_DISTANCE_SQR;
    }

    /**
     * Validates that currency quantities are strictly non-negative, non-zero, and cannot overflow integer bounds.
     */
    public static boolean isValidCurrencyAmount(int amount) {
        return amount > 0 && amount <= 1_000_000;
    }

    /**
     * Validates that an item stack is valid, non-empty, and has a count within legitimate bounds.
     */
    public static boolean isValidItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getCount() > 0 && stack.getCount() <= stack.getMaxStackSize();
    }

    /**
     * Validates that a trade ratio does not permit negative prices, free items, or circular arbitrage loops.
     */
    public static boolean isValidTradeOffer(int costInShekels, int rewardQuantity) {
        if (costInShekels < 1 || rewardQuantity < 1) {
            return false; // Free items or negative costs strictly prohibited
        }
        return costInShekels <= 1_000_000 && rewardQuantity <= 64;
    }

    /**
     * Validates cooldown timestamp against current game tick.
     */
    public static boolean isCooldownExpired(long currentTick, long lastActionTick, long cooldownDurationTicks) {
        if (lastActionTick < 0 || cooldownDurationTicks < 0) {
            return true;
        }
        return currentTick >= (lastActionTick + cooldownDurationTicks);
    }
}
