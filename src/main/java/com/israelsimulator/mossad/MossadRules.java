package com.israelsimulator.mossad;

/**
 * Pure rules for the Mossad faction: reputation bounds, how reputation makes hostile agents rarer,
 * and when agents stop treating the player as a target.
 */
public final class MossadRules {
    public static final int MIN_REPUTATION = -100;
    public static final int MAX_REPUTATION = 100;
    /** At or above this reputation hostile agents leave the player alone unless attacked. */
    public static final int TRUSTED_REPUTATION = 60;
    /** Reputation lost for killing an agent outside an elimination mission. */
    public static final int KILL_PENALTY = 2;
    /** Base chance, per player and spawn check (every 20 s at night in Israeli biomes), of an agent team. */
    public static final double BASE_SPAWN_CHANCE = 0.04;
    public static final int SPAWN_CHECK_INTERVAL = 400;
    public static final int MAX_AGENTS_NEARBY = 3;

    private MossadRules() {}

    public static int clampReputation(int value) {
        return Math.max(MIN_REPUTATION, Math.min(MAX_REPUTATION, value));
    }

    /** 1.0 at reputation 0 or below, falling linearly to 0.1 at 90+. */
    public static double spawnMultiplier(int reputation) {
        if (reputation <= 0) {
            return 1.0;
        }
        return Math.max(0.1, 1.0 - reputation / 100.0);
    }

    public static double spawnChance(int reputation) {
        return BASE_SPAWN_CHANCE * spawnMultiplier(reputation);
    }

    public static boolean isTrusted(int reputation) {
        return reputation >= TRUSTED_REPUTATION;
    }
}
