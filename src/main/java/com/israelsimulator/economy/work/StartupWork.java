package com.israelsimulator.economy.work;

/**
 * Startup work rules (roadmap R5), pure so they can be unit-tested.
 *
 * <p>A laptop work session takes {@link #WORK_TICKS}, pays {@link #BASE_PAY} Shekels (double in a
 * tech hub: Tel Aviv's Urban Area or a Startup Office) and then the laptop needs
 * {@link #LAPTOP_COOLDOWN} ticks of rest. Every {@link #EXIT_EVERY} sessions the startup "exits"
 * and pays a bonus. The smartphone does small delivery gigs in the cities. No work on Shabbat.</p>
 */
public final class StartupWork {
    public static final int WORK_TICKS = 100;
    public static final int LAPTOP_COOLDOWN = 1200;
    public static final int BASE_PAY = 3;
    public static final int EXIT_EVERY = 10;
    public static final int EXIT_BONUS = 25;
    public static final int GIG_COOLDOWN = 600;
    public static final int GIG_PAY = 1;
    public static final float WORK_EXHAUSTION = 2.0F;

    private StartupWork() {}

    /** Shekels for the session that brings the laptop to {@code sessions} completed sessions. */
    public static int sessionPay(int sessions, boolean techHub) {
        int pay = techHub ? BASE_PAY * 2 : BASE_PAY;
        if (isExit(sessions)) pay += EXIT_BONUS;
        return pay;
    }

    public static boolean isExit(int sessions) {
        return sessions > 0 && sessions % EXIT_EVERY == 0;
    }

    public static int sessionsToExit(int sessions) {
        return EXIT_EVERY - Math.floorMod(sessions, EXIT_EVERY);
    }
}
