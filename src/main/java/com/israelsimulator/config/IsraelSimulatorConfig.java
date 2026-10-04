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

        SPEC = builder.build();
    }

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
}
