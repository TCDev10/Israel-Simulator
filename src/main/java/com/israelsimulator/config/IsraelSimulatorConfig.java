package com.israelsimulator.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common configuration for Israel-Simulator gameplay values (GAME_DESIGN.md §11, §18, §56, §63).
 */
public final class IsraelSimulatorConfig {
    public static final ModConfigSpec SPEC;

    // Effects
    private static final ModConfigSpec.IntValue BLESSED_DURATION_TICKS;
    private static final ModConfigSpec.IntValue BLESSED_AMPLIFIER;

    // Food & Kosher
    private static final ModConfigSpec.BooleanValue KOSHER_SYSTEM_ENABLED;
    private static final ModConfigSpec.IntValue MEAT_DIGESTION_TICKS;
    private static final ModConfigSpec.IntValue DAIRY_DIGESTION_TICKS;

    // Boss
    private static final ModConfigSpec.DoubleValue BIBI_BOSS_MAX_HEALTH;
    private static final ModConfigSpec.DoubleValue BIBI_BOSS_BASE_DAMAGE;
    private static final ModConfigSpec.IntValue BIBI_BOSS_MAX_GUARDS;
    private static final ModConfigSpec.IntValue BIBI_BOSS_ARENA_RADIUS;

    // Worldgen & Cities (§56)
    private static final ModConfigSpec.IntValue CITY_SPACING_CHUNKS;
    private static final ModConfigSpec.IntValue RARE_STRUCTURE_SPACING_CHUNKS;

    // NPC & Population Limits (§56)
    private static final ModConfigSpec.IntValue MAX_NPC_PER_CHUNK;
    private static final ModConfigSpec.DoubleValue VILLAGER_TRADE_DISCOUNT_MAX;

    // Events (§56)
    private static final ModConfigSpec.IntValue EVENT_DURATION_TICKS;
    private static final ModConfigSpec.IntValue EVENT_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue SPEECH_MIN_PARTICIPATION_TICKS;

    // Cooldowns (§56)
    private static final ModConfigSpec.IntValue PRAYER_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue SHOFAR_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue TRANSIT_COOLDOWN_TICKS;

    // Performance & Particles (§56)
    private static final ModConfigSpec.IntValue MAX_PARTICLES_PER_EVENT;
    private static final ModConfigSpec.BooleanValue ENABLE_PERFORMANCE_THROTTLING;

    // Audio & Ambience (§56)
    private static final ModConfigSpec.DoubleValue MUSIC_VOLUME_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue CITY_AMBIENCE_ENABLED;

    // Debug (§56)
    private static final ModConfigSpec.BooleanValue DEBUG_LOGGING_ENABLED;

    private IsraelSimulatorConfig() {}

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("effects");
        BLESSED_DURATION_TICKS = builder
                .comment("Duration of the Blessed effect in ticks (20 ticks = 1 second, default 1200 = 60s).")
                .translation("israel_simulator.configuration.blessedDurationTicks")
                .defineInRange("blessedDurationTicks", 1200, 1, 72000);
        BLESSED_AMPLIFIER = builder
                .comment("Amplifier of the Blessed effect (0 = level I).")
                .translation("israel_simulator.configuration.blessedAmplifier")
                .defineInRange("blessedAmplifier", 0, 0, 4);
        builder.pop();

        builder.push("food");
        KOSHER_SYSTEM_ENABLED = builder
                .comment("Enable the simplified kosher system (meat/dairy/pareve tags and mixing penalty).")
                .translation("israel_simulator.configuration.kosherSystemEnabled")
                .define("kosherSystemEnabled", true);
        MEAT_DIGESTION_TICKS = builder
                .comment("Duration in ticks of waiting period after consuming meat (default 3600 = 3 minutes).")
                .translation("israel_simulator.configuration.meatDigestionTicks")
                .defineInRange("meatDigestionTicks", 3600, 20, 72000);
        DAIRY_DIGESTION_TICKS = builder
                .comment("Duration in ticks of waiting period after consuming dairy (default 1200 = 1 minute).")
                .translation("israel_simulator.configuration.dairyDigestionTicks")
                .defineInRange("dairyDigestionTicks", 1200, 20, 72000);
        builder.pop();

        builder.push("boss");
        BIBI_BOSS_MAX_HEALTH = builder
                .comment("Maximum health of Bibi Boss (GAME_DESIGN §41.1, default 10,000+).")
                .translation("israel_simulator.configuration.bibiBossMaxHealth")
                .defineInRange("bibiBossMaxHealth", 10000.0, 100.0, 100000.0);
        BIBI_BOSS_BASE_DAMAGE = builder
                .comment("Base attack damage of Bibi Boss.")
                .translation("israel_simulator.configuration.bibiBossBaseDamage")
                .defineInRange("bibiBossBaseDamage", 18.0, 1.0, 200.0);
        BIBI_BOSS_MAX_GUARDS = builder
                .comment("Maximum simultaneous guards summoned by Bibi Boss to avoid mob runaway.")
                .translation("israel_simulator.configuration.bibiBossMaxGuards")
                .defineInRange("bibiBossMaxGuards", 4, 0, 12);
        BIBI_BOSS_ARENA_RADIUS = builder
                .comment("Radius of the boss arena in blocks before leashing/resetting.")
                .translation("israel_simulator.configuration.bibiBossArenaRadius")
                .defineInRange("bibiBossArenaRadius", 48, 16, 128);
        builder.pop();

        builder.push("worldgen");
        CITY_SPACING_CHUNKS = builder
                .comment("Average spacing between major cities in chunks.")
                .translation("israel_simulator.configuration.citySpacingChunks")
                .defineInRange("citySpacingChunks", 34, 16, 128);
        RARE_STRUCTURE_SPACING_CHUNKS = builder
                .comment("Average spacing between rare historical/cultural structures in chunks.")
                .translation("israel_simulator.configuration.rareStructureSpacingChunks")
                .defineInRange("rareStructureSpacingChunks", 48, 24, 128);
        builder.pop();

        builder.push("npc");
        MAX_NPC_PER_CHUNK = builder
                .comment("Maximum density limit of cultural NPCs per chunk to preserve server performance.")
                .translation("israel_simulator.configuration.maxNpcPerChunk")
                .defineInRange("maxNpcPerChunk", 12, 2, 64);
        VILLAGER_TRADE_DISCOUNT_MAX = builder
                .comment("Maximum percentage discount applied for high reputation or Blessed Trader standing.")
                .translation("israel_simulator.configuration.villagerTradeDiscountMax")
                .defineInRange("villagerTradeDiscountMax", 0.20, 0.0, 0.50);
        builder.pop();

        builder.push("events");
        EVENT_DURATION_TICKS = builder
                .comment("Default duration of world events in ticks (default 6000 = 5 minutes).")
                .translation("israel_simulator.configuration.eventDurationTicks")
                .defineInRange("eventDurationTicks", 6000, 1200, 24000);
        EVENT_COOLDOWN_TICKS = builder
                .comment("Cooldown period between consecutive world events in ticks (default 12000 = 10 minutes).")
                .translation("israel_simulator.configuration.eventCooldownTicks")
                .defineInRange("eventCooldownTicks", 12000, 2400, 72000);
        SPEECH_MIN_PARTICIPATION_TICKS = builder
                .comment("Minimum active participation duration required for Public Speech rewards (default 1200 = 60s).")
                .translation("israel_simulator.configuration.speechMinParticipationTicks")
                .defineInRange("speechMinParticipationTicks", 1200, 200, 6000);
        builder.pop();

        builder.push("cooldowns");
        PRAYER_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks between Western Wall prayers (default 24000 = 1 in-game day).")
                .translation("israel_simulator.configuration.prayerCooldownTicks")
                .defineInRange("prayerCooldownTicks", 24000, 1200, 72000);
        SHOFAR_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks between sounding the Shofar (default 600 = 30s).")
                .translation("israel_simulator.configuration.shofarCooldownTicks")
                .defineInRange("shofarCooldownTicks", 600, 60, 6000);
        TRANSIT_COOLDOWN_TICKS = builder
                .comment("Cooldown in ticks between public transit rides (default 100 = 5s).")
                .translation("israel_simulator.configuration.transitCooldownTicks")
                .defineInRange("transitCooldownTicks", 100, 20, 1200);
        builder.pop();

        builder.push("performance");
        MAX_PARTICLES_PER_EVENT = builder
                .comment("Maximum particles emitted per event tick to avoid FPS lag.")
                .translation("israel_simulator.configuration.maxParticlesPerEvent")
                .defineInRange("maxParticlesPerEvent", 30, 5, 100);
        ENABLE_PERFORMANCE_THROTTLING = builder
                .comment("Enable tick and entity throttling during heavy server load.")
                .translation("israel_simulator.configuration.enablePerformanceThrottling")
                .define("enablePerformanceThrottling", true);
        builder.pop();

        builder.push("audio");
        MUSIC_VOLUME_MULTIPLIER = builder
                .comment("Volume multiplier for mod soundtracks and regional instruments.")
                .translation("israel_simulator.configuration.musicVolumeMultiplier")
                .defineInRange("musicVolumeMultiplier", 1.0, 0.0, 2.0);
        CITY_AMBIENCE_ENABLED = builder
                .comment("Enable regional ambient city soundscapes.")
                .translation("israel_simulator.configuration.cityAmbienceEnabled")
                .define("cityAmbienceEnabled", true);
        builder.pop();

        builder.push("debug");
        DEBUG_LOGGING_ENABLED = builder
                .comment("Enable verbose debug logging for events, economy, and AI state.")
                .translation("israel_simulator.configuration.debugLoggingEnabled")
                .define("debugLoggingEnabled", false);
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

    public static int citySpacingChunks() {
        try {
            return CITY_SPACING_CHUNKS != null ? CITY_SPACING_CHUNKS.get() : 34;
        } catch (Exception ignored) {
            return 34;
        }
    }

    public static int rareStructureSpacingChunks() {
        try {
            return RARE_STRUCTURE_SPACING_CHUNKS != null ? RARE_STRUCTURE_SPACING_CHUNKS.get() : 48;
        } catch (Exception ignored) {
            return 48;
        }
    }

    public static int maxNpcPerChunk() {
        try {
            return MAX_NPC_PER_CHUNK != null ? MAX_NPC_PER_CHUNK.get() : 12;
        } catch (Exception ignored) {
            return 12;
        }
    }

    public static double villagerTradeDiscountMax() {
        try {
            return VILLAGER_TRADE_DISCOUNT_MAX != null ? VILLAGER_TRADE_DISCOUNT_MAX.get() : 0.20;
        } catch (Exception ignored) {
            return 0.20;
        }
    }

    public static int eventDurationTicks() {
        try {
            return EVENT_DURATION_TICKS != null ? EVENT_DURATION_TICKS.get() : 6000;
        } catch (Exception ignored) {
            return 6000;
        }
    }

    public static int eventCooldownTicks() {
        try {
            return EVENT_COOLDOWN_TICKS != null ? EVENT_COOLDOWN_TICKS.get() : 12000;
        } catch (Exception ignored) {
            return 12000;
        }
    }

    public static int speechMinParticipationTicks() {
        try {
            return SPEECH_MIN_PARTICIPATION_TICKS != null ? SPEECH_MIN_PARTICIPATION_TICKS.get() : 1200;
        } catch (Exception ignored) {
            return 1200;
        }
    }

    public static int prayerCooldownTicks() {
        try {
            return PRAYER_COOLDOWN_TICKS != null ? PRAYER_COOLDOWN_TICKS.get() : 24000;
        } catch (Exception ignored) {
            return 24000;
        }
    }

    public static int shofarCooldownTicks() {
        try {
            return SHOFAR_COOLDOWN_TICKS != null ? SHOFAR_COOLDOWN_TICKS.get() : 600;
        } catch (Exception ignored) {
            return 600;
        }
    }

    public static int transitCooldownTicks() {
        try {
            return TRANSIT_COOLDOWN_TICKS != null ? TRANSIT_COOLDOWN_TICKS.get() : 100;
        } catch (Exception ignored) {
            return 100;
        }
    }

    public static int maxParticlesPerEvent() {
        try {
            return MAX_PARTICLES_PER_EVENT != null ? MAX_PARTICLES_PER_EVENT.get() : 30;
        } catch (Exception ignored) {
            return 30;
        }
    }

    public static boolean enablePerformanceThrottling() {
        try {
            return ENABLE_PERFORMANCE_THROTTLING != null && ENABLE_PERFORMANCE_THROTTLING.get();
        } catch (Exception ignored) {
            return true;
        }
    }

    public static double musicVolumeMultiplier() {
        try {
            return MUSIC_VOLUME_MULTIPLIER != null ? MUSIC_VOLUME_MULTIPLIER.get() : 1.0;
        } catch (Exception ignored) {
            return 1.0;
        }
    }

    public static boolean cityAmbienceEnabled() {
        try {
            return CITY_AMBIENCE_ENABLED != null && CITY_AMBIENCE_ENABLED.get();
        } catch (Exception ignored) {
            return true;
        }
    }

    public static boolean debugLoggingEnabled() {
        try {
            return DEBUG_LOGGING_ENABLED != null && DEBUG_LOGGING_ENABLED.get();
        } catch (Exception ignored) {
            return false;
        }
    }
}
