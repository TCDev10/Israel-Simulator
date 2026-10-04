package com.israelsimulator.world.structure;

import com.israelsimulator.IsraelSimulator;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * Registry keys for structures and structure sets in Israel-Simulator world generation.
 */
public final class ModStructures {
    // Structures
    public static final ResourceKey<Structure> MEDITERRANEAN_VILLAGE =
            structureKey("mediterranean_village");
    public static final ResourceKey<Structure> AGRICULTURAL_FARM =
            structureKey("agricultural_farm");
    public static final ResourceKey<Structure> DEAD_SEA_RESORT =
            structureKey("dead_sea_resort");
    public static final ResourceKey<Structure> DESERT_RUINS =
            structureKey("desert_ruins");
    public static final ResourceKey<Structure> SYNAGOGUE =
            structureKey("synagogue");
    public static final ResourceKey<Structure> TEL_AVIV_CITY =
            structureKey("tel_aviv_city");
    public static final ResourceKey<Structure> EIN_GEDI_OASIS =
            structureKey("ein_gedi_oasis");

    // Structure Sets
    public static final ResourceKey<StructureSet> MEDITERRANEAN_VILLAGES =
            structureSetKey("mediterranean_villages");
    public static final ResourceKey<StructureSet> AGRICULTURAL_FARMS =
            structureSetKey("agricultural_farms");
    public static final ResourceKey<StructureSet> DEAD_SEA_RESORTS =
            structureSetKey("dead_sea_resorts");
    public static final ResourceKey<StructureSet> DESERT_RUINS_SET =
            structureSetKey("desert_ruins");
    public static final ResourceKey<StructureSet> SYNAGOGUES =
            structureSetKey("synagogues");
    public static final ResourceKey<StructureSet> TEL_AVIV_CITIES =
            structureSetKey("tel_aviv_cities");
    public static final ResourceKey<StructureSet> EIN_GEDI_OASES =
            structureSetKey("ein_gedi_oases");

    // Processor Lists
    public static final ResourceKey<net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList> ANCIENT_RUINS_WEATHERING =
            processorListKey("ancient_ruins_weathering");

    private static final List<ResourceKey<Structure>> ALL_STRUCTURES = List.of(
            MEDITERRANEAN_VILLAGE,
            AGRICULTURAL_FARM,
            DEAD_SEA_RESORT,
            DESERT_RUINS,
            SYNAGOGUE,
            TEL_AVIV_CITY,
            EIN_GEDI_OASIS
    );

    private ModStructures() {}

    private static ResourceKey<Structure> structureKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name));
    }

    private static ResourceKey<StructureSet> structureSetKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name));
    }

    private static ResourceKey<net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList> processorListKey(String name) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name));
    }

    public static List<ResourceKey<Structure>> allStructures() {
        return ALL_STRUCTURES;
    }
}
