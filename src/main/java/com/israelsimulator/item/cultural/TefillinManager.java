package com.israelsimulator.item.cultural;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cooldown tracker and prayer status validator for Tefillin morning prayer ritual (GAME_DESIGN.md §14, TODO §24).
 * Pure server-authoritative logic that can be tested without Minecraft item bootstrap.
 */
public final class TefillinManager {

    public static final long COOLDOWN_TICKS = 12000L; // 10 real minutes (half Minecraft day)
    private static final Map<UUID, Long> LAST_TEFILLIN_USE = new ConcurrentHashMap<>();

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

    public static boolean isOnCooldown(UUID playerId, long currentGameTime) {
        Long last = LAST_TEFILLIN_USE.get(playerId);
        return last != null && (currentGameTime - last) < COOLDOWN_TICKS;
    }

    public static void setLastUseTime(UUID playerId, long time) {
        LAST_TEFILLIN_USE.put(playerId, time);
    }

    public static void clearCooldown(UUID playerId) {
        LAST_TEFILLIN_USE.remove(playerId);
    }
}
