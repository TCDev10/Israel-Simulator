package com.israelsimulator.world.feature;

import com.israelsimulator.IsraelSimulator;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Registry keys for placed features in Israel-Simulator world generation.
 */
public final class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> OLIVE_TREE_PLACED =
            key("olive_tree_placed");
    public static final ResourceKey<PlacedFeature> DATE_PALM_PLACED =
            key("date_palm_placed");
    public static final ResourceKey<PlacedFeature> CITRUS_ORCHARD_PLACED =
            key("citrus_orchard_placed");
    public static final ResourceKey<PlacedFeature> DEAD_SEA_SALT_PLACED =
            key("dead_sea_salt_placed");
    public static final ResourceKey<PlacedFeature> DESERT_SCRUB_PLACED =
            key("desert_scrub_placed");
    public static final ResourceKey<PlacedFeature> GRAPEVINE_PATCH_PLACED =
            key("grapevine_patch_placed");
    public static final ResourceKey<PlacedFeature> MEDITERRANEAN_HERBS_PLACED =
            key("mediterranean_herbs_placed");
    public static final ResourceKey<PlacedFeature> DESERT_ROCK_MOUND_PLACED =
            key("desert_rock_mound_placed");

    private static final List<ResourceKey<PlacedFeature>> ALL = List.of(
            OLIVE_TREE_PLACED,
            DATE_PALM_PLACED,
            CITRUS_ORCHARD_PLACED,
            DEAD_SEA_SALT_PLACED,
            DESERT_SCRUB_PLACED,
            GRAPEVINE_PATCH_PLACED,
            MEDITERRANEAN_HERBS_PLACED,
            DESERT_ROCK_MOUND_PLACED
    );

    private ModPlacedFeatures() {}

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name));
    }

    public static List<ResourceKey<PlacedFeature>> all() {
        return ALL;
    }
}
