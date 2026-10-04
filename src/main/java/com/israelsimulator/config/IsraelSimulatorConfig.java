package com.israelsimulator.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common configuration for Israel-Simulator gameplay values (GAME_DESIGN.md §11, §18, §63).
 */
public final class IsraelSimulatorConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue BLESSED_DURATION_TICKS;
    private static final ModConfigSpec.IntValue BLESSED_AMPLIFIER;
    private static final ModConfigSpec.BooleanValue KOSHER_SYSTEM_ENABLED;
    private static final ModConfigSpec.IntValue MEAT_DIGESTION_TICKS;
    private static final ModConfigSpec.IntValue DAIRY_DIGESTION_TICKS;

    private IsraelSimulatorConfig() {}

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("effects");
        BLESSED_DURATION_TICKS = builder
                .comment("Duration of the Blessed effect in ticks (20 ticks = 1 second, default 1200 = 60s).")
                .defineInRange("blessedDurationTicks", 1200, 1, 72000);
        BLESSED_AMPLIFIER = builder
                .comment("Amplifier of the Blessed effect (0 = level I).")
                .defineInRange("blessedAmplifier", 0, 0, 4);
        builder.pop();

        builder.push("food");
        KOSHER_SYSTEM_ENABLED = builder
                .comment("Enable the simplified kosher system (meat/dairy/pareve tags and mixing penalty).")
                .define("kosherSystemEnabled", true);
        MEAT_DIGESTION_TICKS = builder
                .comment("Duration in ticks of waiting period after consuming meat (default 3600 = 3 minutes).")
                .defineInRange("meatDigestionTicks", 3600, 20, 72000);
        DAIRY_DIGESTION_TICKS = builder
                .comment("Duration in ticks of waiting period after consuming dairy (default 1200 = 1 minute).")
                .defineInRange("dairyDigestionTicks", 1200, 20, 72000);
        builder.pop();

        builder.push("boss");
        BIBI_BOSS_MAX_HEALTH = builder
                .comment("Maximum health of Bibi Boss (GAME_DESIGN §41.1, default 10,000+).")
                .defineInRange("bibiBossMaxHealth", 10000.0, 100.0, 100000.0);
        BIBI_BOSS_BASE_DAMAGE = builder
                .comment("Base attack damage of Bibi Boss.")
                .defineInRange("bibiBossBaseDamage", 18.0, 1.0, 200.0);
        BIBI_BOSS_MAX_GUARDS = builder
                .comment("Maximum simultaneous guards summoned by Bibi Boss to avoid mob runaway.")
                .defineInRange("bibiBossMaxGuards", 4, 0, 12);
        BIBI_BOSS_ARENA_RADIUS = builder
                .comment("Radius of the boss arena in blocks before leashing/resetting.")
                .defineInRange("bibiBossArenaRadius", 48, 16, 128);
        builder.pop();

        SPEC = builder.build();
    }

    private static final ModConfigSpec.DoubleValue BIBI_BOSS_MAX_HEALTH;
    private static final ModConfigSpec.DoubleValue BIBI_BOSS_BASE_DAMAGE;
    private static final ModConfigSpec.IntValue BIBI_BOSS_MAX_GUARDS;
    private static final ModConfigSpec.IntValue BIBI_BOSS_ARENA_RADIUS;

    public static int blessedDurationTicks() {
        return BLESSED_DURATION_TICKS.get();
    }

    public static int blessedAmplifier() {
        return BLESSED_AMPLIFIER.get();
    }

    public static boolean kosherSystemEnabled() {
        return KOSHER_SYSTEM_ENABLED.get();
    }

    public static int meatDigestionTicks() {
        return MEAT_DIGESTION_TICKS.get();
    }

    public static int dairyDigestionTicks() {
        return DAIRY_DIGESTION_TICKS.get();
    }

    public static double bibiBossMaxHealth() {
        try {
            return BIBI_BOSS_MAX_HEALTH != null ? BIBI_BOSS_MAX_HEALTH.get() : 10000.0;
        } catch (Exception ignored) {
            return 10000.0;
        }
    }

    public static double bibiBossBaseDamage() {
        try {
            return BIBI_BOSS_BASE_DAMAGE != null ? BIBI_BOSS_BASE_DAMAGE.get() : 18.0;
        } catch (Exception ignored) {
            return 18.0;
        }
    }

    public static int bibiBossMaxGuards() {
        try {
            return BIBI_BOSS_MAX_GUARDS != null ? BIBI_BOSS_MAX_GUARDS.get() : 4;
        } catch (Exception ignored) {
            return 4;
        }
    }

    public static int bibiBossArenaRadius() {
        try {
            return BIBI_BOSS_ARENA_RADIUS != null ? BIBI_BOSS_ARENA_RADIUS.get() : 48;
        } catch (Exception ignored) {
            return 48;
        }
    }
}
