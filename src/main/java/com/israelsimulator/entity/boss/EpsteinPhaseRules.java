package com.israelsimulator.entity.boss;

/**
 * One-shot phase triggers of the Epstein fight, free of Minecraft types so they can be unit-tested.
 */
public final class EpsteinPhaseRules {
    /** Health fraction at or below which the glass dome + pavilion arena appears (once). */
    public static final float DOME_HEALTH_FRACTION = 0.5F;
    /** Health fraction at or below which the boss is renamed to "Palm Beach Pete" (once). */
    public static final float RENAME_HEALTH_FRACTION = 0.33F;
    /** Lang key of the late-fight name. */
    public static final String RENAMED_NAME_KEY = "entity.israel_simulator.palm_beach_pete";

    private EpsteinPhaseRules() {}

    /** True when the arena must be built now: not built yet and health at or below 50%. */
    public static boolean shouldSpawnDome(boolean alreadySpawned, float healthFraction) {
        return !alreadySpawned && healthFraction <= DOME_HEALTH_FRACTION;
    }

    /** True when the boss must be renamed now: not renamed yet and health at or below 33%. */
    public static boolean shouldRename(boolean alreadyRenamed, float healthFraction) {
        return !alreadyRenamed && healthFraction <= RENAME_HEALTH_FRACTION;
    }
}
