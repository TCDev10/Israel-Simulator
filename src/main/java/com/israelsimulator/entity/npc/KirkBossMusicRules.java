package com.israelsimulator.entity.npc;

/** Pure rules for the Kirk (Orator) boss music (testable without a client). */
public final class KirkBossMusicRules {
    /** Players within this many blocks of Charlie Kirk hear the speech boss song. */
    public static final double MUSIC_RADIUS = 64.0D;
    /** Fade-out length in ticks after Kirk dies or the player leaves. */
    public static final int FADE_TICKS = 30;

    private KirkBossMusicRules() {}

    public static boolean inRange(double distanceSqr) {
        return distanceSqr <= MUSIC_RADIUS * MUSIC_RADIUS;
    }

    public static boolean shouldPlay(boolean oratorAlive, boolean oratorRemoved, double distanceSqr) {
        return oratorAlive && !oratorRemoved && inRange(distanceSqr);
    }

    /** Volume after {@code fadeTicks} ticks of fading out. */
    public static float fadedVolume(int fadeTicks) {
        return Math.max(0.0F, 1.0F - (float) fadeTicks / FADE_TICKS);
    }
}

