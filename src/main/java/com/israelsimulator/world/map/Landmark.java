package com.israelsimulator.world.map;

import com.israelsimulator.world.structure.ModStructures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

/**
 * Landmarks that exist in the world as real generated structures (GAME_DESIGN.md §8–10, TODO §48).
 *
 * <p>Each landmark is bound to a structure, and optionally to one template piece of a jigsaw
 * structure; {@link PlayerLandmarkTracker} discovers it when the player stands inside that
 * structure/piece. There are no hard-coded coordinates.
 */
public enum Landmark {
    WESTERN_WALL(
            "western_wall",
            "Western Wall (Kotel)",
            "Ancient sacred limestone retaining wall of the Temple Mount",
            IsraelRegion.JERUSALEM,
            ModStructures.WESTERN_WALL,
            null
    ),
    JAFFA_CLOCK_TOWER(
            "jaffa_clock_tower",
            "Jaffa Clock Tower",
            "Iconic Ottoman limestone clock tower at the historic entrance to Old Jaffa",
            IsraelRegion.JAFFA,
            ModStructures.JAFFA_PORT,
            "israel_simulator:jaffa/clock_square"
    ),
    FLEA_MARKET(
            "flea_market",
            "Old Jaffa Flea Market (Shuk HaPishpeshim)",
            "Charming historic bazaar of antique shops, cafes, and copper crafts",
            IsraelRegion.JAFFA,
            ModStructures.JAFFA_PORT,
            "israel_simulator:jaffa/flea_market"
    ),
    TEL_AVIV_PROMENADE(
            "tel_aviv_promenade",
            "Tayelet Coastal Promenade",
            "Scenic pedestrian boardwalk along the Mediterranean shoreline",
            IsraelRegion.TEL_AVIV,
            ModStructures.TEL_AVIV_CITY,
            "israel_simulator:tel_aviv/tayelet"
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final IsraelRegion region;
    private final ResourceKey<Structure> structure;
    private final @Nullable String piece;

    Landmark(String id, String displayName, String description, IsraelRegion region,
             ResourceKey<Structure> structure, @Nullable String piece) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.region = region;
        this.structure = structure;
        this.piece = piece;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public IsraelRegion getRegion() {
        return region;
    }

    /** The generated structure this landmark belongs to. */
    public ResourceKey<Structure> getStructure() {
        return structure;
    }

    /** Template id of the jigsaw piece that is the landmark, or null for the whole structure. */
    public @Nullable String getPiece() {
        return piece;
    }
}
