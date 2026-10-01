package com.israelsimulator.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common configuration for gameplay values that will be introduced by later milestones.
 *
 * <p>Kept intentionally minimal: it proves the configuration infrastructure loads with
 * safe defaults and is validated, without declaring gameplay values for systems that do
 * not exist yet. Entries are added together with the systems they configure.</p>
 */
public final class IsraelSimulatorConfig {
    public static final ModConfigSpec SPEC;

    private IsraelSimulatorConfig() {}

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SPEC = builder.build();
    }
}
