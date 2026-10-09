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
    public static final ResourceKey<Structure> GREAT_SYNAGOGUE =
            structureKey("great_synagogue");
    public static final ResourceKey<Structure> TEL_AVIV_CITY =
            structureKey("tel_aviv_city");
    public static final ResourceKey<Structure> EIN_GEDI_OASIS =
            structureKey("ein_gedi_oasis");
    public static final ResourceKey<Structure> JAFFA_PORT =
            structureKey("jaffa_port");
    public static final ResourceKey<Structure> JERUSALEM_CITY =
            structureKey("jerusalem_city");
    public static final ResourceKey<Structure> WESTERN_WALL =
            structureKey("western_wall");
    public static final ResourceKey<Structure> HISTORICAL_HOUSE =
            structureKey("historical_house");
    public static final ResourceKey<Structure> GRAND_MARKET =
            structureKey("grand_market");
    public static final ResourceKey<Structure> STARTUP_OFFICE =
            structureKey("startup_office");
    public static final ResourceKey<Structure> GOVERNMENT_BUILDING =
            structureKey("government_building");
    public static final ResourceKey<Structure> ANCIENT_SANCTUARY =
            structureKey("ancient_sanctuary");
    /** Striped pavilion with a gold dome; also the Epstein boss arena (same template). */
    public static final ResourceKey<Structure> ISLAND_TEMPLE =
            structureKey("island_temple");

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
    public static final ResourceKey<StructureSet> GREAT_SYNAGOGUES =
            structureSetKey("great_synagogues");
    public static final ResourceKey<StructureSet> TEL_AVIV_CITIES =
            structureSetKey("tel_aviv_cities");
    public static final ResourceKey<StructureSet> EIN_GEDI_OASES =
            structureSetKey("ein_gedi_oases");
    public static final ResourceKey<StructureSet> JAFFA_PORTS =
            structureSetKey("jaffa_ports");
    public static final ResourceKey<StructureSet> JERUSALEM_CITIES =
            structureSetKey("jerusalem_cities");
    public static final ResourceKey<StructureSet> WESTERN_WALLS =
            structureSetKey("western_walls");
    public static final ResourceKey<StructureSet> HISTORICAL_HOUSES =
            structureSetKey("historical_houses");
    public static final ResourceKey<StructureSet> GRAND_MARKETS =
            structureSetKey("grand_markets");
    public static final ResourceKey<StructureSet> STARTUP_OFFICES =
            structureSetKey("startup_offices");
    public static final ResourceKey<StructureSet> GOVERNMENT_BUILDINGS =
            structureSetKey("government_buildings");
    public static final ResourceKey<StructureSet> ANCIENT_SANCTUARIES =
            structureSetKey("ancient_sanctuaries");
    public static final ResourceKey<StructureSet> ISLAND_TEMPLES =
            structureSetKey("island_temples");

    // Processor Lists
    public static final ResourceKey<net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList> ANCIENT_RUINS_WEATHERING =
            processorListKey("ancient_ruins_weathering");

    private static final List<ResourceKey<Structure>> ALL_STRUCTURES = List.of(
            MEDITERRANEAN_VILLAGE,
            AGRICULTURAL_FARM,
            DEAD_SEA_RESORT,
            DESERT_RUINS,
            SYNAGOGUE,
            GREAT_SYNAGOGUE,
            TEL_AVIV_CITY,
            EIN_GEDI_OASIS,
            JAFFA_PORT,
            JERUSALEM_CITY,
            WESTERN_WALL,
            HISTORICAL_HOUSE,
            GRAND_MARKET,
            STARTUP_OFFICE,
            GOVERNMENT_BUILDING,
            ANCIENT_SANCTUARY,
            ISLAND_TEMPLE
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
