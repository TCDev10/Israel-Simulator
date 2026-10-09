package com.israelsimulator.event.world.speech;

/**
 * Pure rules for Public Speech participation: the timer only counts while the player stays
 * near the stage and resets as soon as they leave; the reward is given once per event.
 */
public final class SpeechParticipation {
    /** Players within this many blocks of the stage centre are listening. */
    public static final double RADIUS = 16.0;

    private SpeechParticipation() {}

    public static boolean inRange(double distSqr) {
        return distSqr <= RADIUS * RADIUS;
    }

    /** Continuous timer: adds {@code delta} while in range, back to 0 when the player walks away. */
    public static int nextTicks(int current, boolean inRange, int delta) {
        return inRange ? current + delta : 0;
    }

    public static boolean shouldReward(int ticks, int required, boolean alreadyRewarded) {
        return !alreadyRewarded && ticks >= required;
    }

    /** Progress in [0,1] for the action bar. */
    public static float progress(int ticks, int required) {
        if (required <= 0) return 1.0F;
        return Math.min(1.0F, Math.max(0.0F, ticks / (float) required));
    }

    /** Scheduler gate: enough time since the last speech, and no speech running. */
    public static boolean canSchedule(long now, long lastEnd, long cooldown, boolean active) {
        return !active && (lastEnd < 0 || now - lastEnd >= cooldown);
    }
}
