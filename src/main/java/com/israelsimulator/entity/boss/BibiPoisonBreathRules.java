package com.israelsimulator.entity.boss;

import java.util.random.RandomGenerator;

/**
 * Rules of Bibi's late-fight "poison breath" attack, free of Minecraft types so they can be unit-tested.
 * The attack unlocks only after the Epstein / Palm Beach Pete summoned by this Bibi has died.
 */
public final class BibiPoisonBreathRules {
    /** Minimum ticks between two attacks (15 s). */
    public static final int MIN_INTERVAL_TICKS = 300;
    /** Maximum ticks between two attacks (25 s). */
    public static final int MAX_INTERVAL_TICKS = 500;
    /** Ticks of warning particles above the target before the projectile drops (1.5 s). */
    public static final int WARNING_TICKS = 30;
    /** Height above the target from which the projectile is launched. */
    public static final double DROP_HEIGHT = 14.0;
    /** Max distance from Bibi of a player that can be targeted. */
    public static final double TARGET_RANGE = 32.0;
    /** Cloud radius in blocks. */
    public static final float CLOUD_RADIUS = 3.0F;
    /** Cloud lifetime in ticks (6 s). */
    public static final int CLOUD_DURATION_TICKS = 120;
    /** Poison duration in ticks (5 s). */
    public static final int POISON_DURATION_TICKS = 100;
    /** Poison amplifier: 1 = Poison II. */
    public static final int POISON_AMPLIFIER = 1;

    private BibiPoisonBreathRules() {}

    /** The attack is available only once Epstein is dead and the boss is still fighting. */
    public static boolean isUnlocked(boolean epsteinDefeated, boolean bibiDefeated) {
        return epsteinDefeated && !bibiDefeated;
    }

    /** True when an attack must start this tick. */
    public static boolean shouldLaunch(boolean epsteinDefeated, boolean bibiDefeated, int cooldownTicks) {
        return isUnlocked(epsteinDefeated, bibiDefeated) && cooldownTicks <= 0;
    }

    /** Random interval in [MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS]. */
    public static int nextInterval(RandomGenerator random) {
        return MIN_INTERVAL_TICKS + random.nextInt(MAX_INTERVAL_TICKS - MIN_INTERVAL_TICKS + 1);
    }
}
