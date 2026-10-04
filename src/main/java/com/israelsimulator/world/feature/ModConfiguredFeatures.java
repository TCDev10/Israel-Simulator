package com.israelsimulator.world.feature;

import com.israelsimulator.IsraelSimulator;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Registry keys for configured features in Israel-Simulator world generation.
 */
public final class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> OLIVE_TREE =
            key("olive_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DATE_PALM =
            key("date_palm");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CITRUS_ORCHARD =
            key("citrus_orchard");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DEAD_SEA_SALT_CLUSTER =
            key("dead_sea_salt_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DESERT_SCRUB =
            key("desert_scrub");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRAPEVINE_PATCH =
            key("grapevine_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEDITERRANEAN_HERBS_PATCH =
            key("mediterranean_herbs_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DESERT_ROCK_MOUND =
            key("desert_rock_mound");

    private static final List<ResourceKey<ConfiguredFeature<?, ?>>> ALL = List.of(
            OLIVE_TREE,
            DATE_PALM,
            CITRUS_ORCHARD,
            DEAD_SEA_SALT_CLUSTER,
            DESERT_SCRUB,
            GRAPEVINE_PATCH,
            MEDITERRANEAN_HERBS_PATCH,
            DESERT_ROCK_MOUND
    );

    private ModConfiguredFeatures() {}

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name));
    }

    public static List<ResourceKey<ConfiguredFeature<?, ?>>> all() {
        return ALL;
    }
}
