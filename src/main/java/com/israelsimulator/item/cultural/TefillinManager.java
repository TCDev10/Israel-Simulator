package com.israelsimulator.item.cultural;

import java.util.UUID;

/**
 * Cooldown tracker and prayer status validator for Tefillin morning prayer ritual (GAME_DESIGN.md §14, TODO §24).
 * Pure server-authoritative logic that can be tested without Minecraft item bootstrap.
 *
 * <p>Cooldown state is not kept in a static map. The server world stores it in
 * {@link TefillinCooldowns}.</p>
 */
public final class TefillinManager {

    public static final long COOLDOWN_TICKS = 12000L; // 10 real minutes (half Minecraft day)

    public enum TefillinStatus {
        SUCCESS,
        MISSING_KIPPAH,
        NOT_DAYTIME,
        COOLDOWN_ACTIVE
    }

    private TefillinManager() {}

    public static TefillinStatus validatePrayer(boolean hasKippah, boolean isDaytime, boolean onCooldown) {
        if (!hasKippah) {
            return TefillinStatus.MISSING_KIPPAH;
        }
        if (!isDaytime) {
            return TefillinStatus.NOT_DAYTIME;
        }
        if (onCooldown) {
            return TefillinStatus.COOLDOWN_ACTIVE;
        }
        return TefillinStatus.SUCCESS;
    }

    public static boolean isOnCooldown(TefillinCooldowns cooldowns, UUID playerId, long currentGameTime) {
        if (cooldowns == null || playerId == null) {
            return false;
        }
        Long last = cooldowns.getLastUseTime(playerId);
        return last != null && (currentGameTime - last) < COOLDOWN_TICKS;
    }

    public static void setLastUseTime(TefillinCooldowns cooldowns, UUID playerId, long time) {
        if (cooldowns != null) {
            cooldowns.setLastUseTime(playerId, time);
        }
    }

    public static void clearCooldown(TefillinCooldowns cooldowns, UUID playerId) {
        if (cooldowns != null) {
            cooldowns.clearCooldown(playerId);
        }
    }
}
