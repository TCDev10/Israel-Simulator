package com.israelsimulator.festival;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cooldown tracker and ritual manager for the Shofar blast (GAME_DESIGN.md §35, TODO §35).
 */
public final class ShofarManager {

    public static final long COOLDOWN_TICKS = 100L; // 5 seconds between blasts
    private static final Map<UUID, Long> LAST_SHOFAR_USE = new ConcurrentHashMap<>();

    private ShofarManager() {}

    public static boolean isOnCooldown(UUID playerId, long currentGameTime) {
        Long last = LAST_SHOFAR_USE.get(playerId);
        return last != null && (currentGameTime - last) < COOLDOWN_TICKS;
    }

    public static void recordBlow(UUID playerId, long time) {
        LAST_SHOFAR_USE.put(playerId, time);
    }

    public static void clearCooldown(UUID playerId) {
        LAST_SHOFAR_USE.remove(playerId);
    }
}
