package com.israelsimulator.festival;

import java.util.UUID;

/**
 * Cooldown tracker and ritual manager for the Shofar blast (GAME_DESIGN.md §35, TODO §35).
 *
 * <p>Cooldown state is not kept in a static map. The server world stores it in
 * {@link ShofarCooldowns}.</p>
 */
public final class ShofarManager {

    public static final long COOLDOWN_TICKS = 100L; // 5 seconds between blasts

    private ShofarManager() {}

    public static boolean isOnCooldown(ShofarCooldowns cooldowns, UUID playerId, long currentGameTime) {
        if (cooldowns == null || playerId == null) {
            return false;
        }
        Long last = cooldowns.getLastUseTime(playerId);
        return last != null && (currentGameTime - last) < COOLDOWN_TICKS;
    }

    public static void recordBlow(ShofarCooldowns cooldowns, UUID playerId, long time) {
        if (cooldowns != null) {
            cooldowns.setLastUseTime(playerId, time);
        }
    }

    public static void clearCooldown(ShofarCooldowns cooldowns, UUID playerId) {
        if (cooldowns != null) {
            cooldowns.clearCooldown(playerId);
        }
    }
}
