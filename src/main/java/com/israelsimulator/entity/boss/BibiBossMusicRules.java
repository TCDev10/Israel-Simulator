package com.israelsimulator.entity.boss;

/** Pure rules for the Bibi boss music (testable without a client). */
public final class BibiBossMusicRules {
    /** Players within this many blocks of a living Bibi hear the boss song. */
    public static final double MUSIC_RADIUS = 64.0D;
    /** Fade-out length in ticks after the fight ends or the player leaves. */
    public static final int FADE_TICKS = 40;

    private BibiBossMusicRules() {}

    public static boolean inRange(double distanceSqr) {
        return distanceSqr <= MUSIC_RADIUS * MUSIC_RADIUS;
    }

    public static boolean shouldPlay(boolean bossAlive, boolean bossRemoved, double distanceSqr) {
        return bossAlive && !bossRemoved && inRange(distanceSqr);
    }

    /** Volume after {@code fadeTicks} ticks of fading out. */
    public static float fadedVolume(int fadeTicks) {
        return Math.max(0.0F, 1.0F - (float) fadeTicks / FADE_TICKS);
    }
}
