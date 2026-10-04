package com.israelsimulator.world.biome;

import com.israelsimulator.IsraelSimulator;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Registry keys and identifiers for Israel-Simulator custom biomes.
 *
 * <p>Biomes represent the major geographical and environmental regions defined in
 * GAME_DESIGN.md (§5, §6, §19, §20, §21):
 * <ul>
 *   <li>{@link #MEDITERRANEAN_COAST}: Coastal terrain, azure waters, beaches and sea life</li>
 *   <li>{@link #ISRAELI_AGRICULTURE}: Fertile agricultural valley, olive and date trees, farmlands</li>
 *   <li>{@link #JUDEAN_DESERT}: Arid desert, sandstone canyons, rocky outcrops and desert life</li>
 *   <li>{@link #DEAD_SEA}: Hypersaline mineral lake, unique turquoise water, salt formations</li>
 *   <li>{@link #URBAN_AREA}: Mediterranean urban foundation, prepared for cities and districts</li>
 * </ul>
 * </p>
 */
public final class ModBiomes {
    public static final ResourceKey<Biome> MEDITERRANEAN_COAST =
            key("mediterranean_coast");
    public static final ResourceKey<Biome> ISRAELI_AGRICULTURE =
            key("israeli_agriculture");
    public static final ResourceKey<Biome> JUDEAN_DESERT =
            key("judean_desert");
    public static final ResourceKey<Biome> DEAD_SEA =
            key("dead_sea");
    public static final ResourceKey<Biome> URBAN_AREA =
            key("urban_area");
    public static final ResourceKey<Biome> JERUSALEM =
            key("jerusalem");

    private static final List<ResourceKey<Biome>> ALL_BIOMES = List.of(
            MEDITERRANEAN_COAST,
            ISRAELI_AGRICULTURE,
            JUDEAN_DESERT,
            DEAD_SEA,
            URBAN_AREA,
            JERUSALEM
    );

    private ModBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name));
    }

    public static List<ResourceKey<Biome>> all() {
        return ALL_BIOMES;
    }
}
