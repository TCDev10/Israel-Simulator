package com.israelsimulator.world.map;

import net.minecraft.core.BlockPos;

/**
 * Key historical, cultural, and geographic landmarks in Israel (GAME_DESIGN.md §8–10, §43, TODO §48).
 */
public enum Landmark {
    WESTERN_WALL(
            "western_wall",
            "Western Wall (Kotel)",
            "Ancient sacred limestone retaining wall of the Temple Mount",
            IsraelRegion.JERUSALEM,
            new BlockPos(850, 80, 850)
    ),
    JAFFA_CLOCK_TOWER(
            "jaffa_clock_tower",
            "Jaffa Clock Tower",
            "Iconic Ottoman limestone clock tower at the historic entrance to Old Jaffa",
            IsraelRegion.JAFFA,
            new BlockPos(150, 70, 500)
    ),
    TEL_AVIV_PROMENADE(
            "tel_aviv_promenade",
            "Tayelet Coastal Promenade",
            "Scenic pedestrian boardwalk along the Mediterranean shoreline",
            IsraelRegion.TEL_AVIV,
            new BlockPos(50, 65, 80)
    ),
    DEAD_SEA_SALT_PILLARS(
            "dead_sea_salt_pillars",
            "Dead Sea Salt Formations",
            "Crystalline mineral salt columns on the hyper-saline waters of Ein Gedi",
            IsraelRegion.DEAD_SEA,
            new BlockPos(1500, 40, 1200)
    ),
    KNESSET(
            "knesset",
            "The Knesset Building",
            "Seat of Israel's parliament overlooking the Rose Garden",
            IsraelRegion.JERUSALEM,
            new BlockPos(750, 95, 750)
    ),
    CARMEL_MARKET(
            "carmel_market",
            "Shuk HaCarmel",
            "Famous bustling open-air market filled with spices, street food and halva",
            IsraelRegion.TEL_AVIV,
            new BlockPos(120, 68, 180)
    ),
    FLEA_MARKET(
            "flea_market",
            "Old Jaffa Flea Market (Shuk HaPishpeshim)",
            "Charming historic bazaar of antique shops, cafes, and copper crafts",
            IsraelRegion.JAFFA,
            new BlockPos(180, 68, 520)
    ),
    MASADA_FORTRESS(
            "masada_fortress",
            "Masada Plateau Fortress",
            "Ancient desert stronghold atop an isolated rock plateau",
            IsraelRegion.DEAD_SEA,
            new BlockPos(1650, 120, 1400)
    ),
    BAHAI_GARDENS(
            "bahai_gardens",
            "Hanging Terraces of Mount Carmel",
            "Magnificent symmetrical garden terraces cascading towards the sea",
            IsraelRegion.MEDITERRANEAN_COAST,
            new BlockPos(-300, 90, -400)
    ),
    ANCIENT_SYNAGOGUE(
            "ancient_synagogue",
            "Ancient Galilee Synagogue",
            "Historic stone sanctuary decorated with carved menorahs and pillars",
            IsraelRegion.GALILEE_GOLAN,
            new BlockPos(-700, 85, -900)
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final IsraelRegion region;
    private final BlockPos defaultPos;

    Landmark(String id, String displayName, String description, IsraelRegion region, BlockPos defaultPos) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.region = region;
        this.defaultPos = defaultPos;
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

    public BlockPos getDefaultPos() {
        return defaultPos;
    }
}
