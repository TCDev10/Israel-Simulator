package com.israelsimulator.festival;

import com.israelsimulator.config.IsraelSimulatorConfig;
import java.util.UUID;

/**
 * Cooldown tracker and ritual manager for the Shofar blast (GAME_DESIGN.md §35, TODO §35).
 *
 * <p>Cooldown state is not kept in a static map. The server world stores it in
 * {@link ShofarCooldowns}. The length comes from {@code shofarCooldownTicks}
 * (default 600), not a hardcoded blast interval.</p>
 */
public final class ShofarManager {

    private ShofarManager() {}

    /**
     * Ticks between blasts. Reads {@link IsraelSimulatorConfig#shofarCooldownTicks()},
     * whose default is 600. If the spec cannot be read, that getter returns 600.
     */
    public static long cooldownTicks() {
        return IsraelSimulatorConfig.shofarCooldownTicks();
    }

    public static boolean isOnCooldown(ShofarCooldowns cooldowns, UUID playerId, long currentGameTime) {
        if (cooldowns == null || playerId == null) {
            return false;
        }
        Long last = cooldowns.getLastUseTime(playerId);
        return last != null && (currentGameTime - last) < cooldownTicks();
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
