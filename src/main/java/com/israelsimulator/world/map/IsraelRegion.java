package com.israelsimulator.world.map;

/**
 * Major geographical regions of Israel modeled in Israel-Simulator (GAME_DESIGN.md §8–10, §32, TODO §48).
 */
public enum IsraelRegion {
    TEL_AVIV("Tel Aviv", "Mediterranean metropolis, financial center and coastal promenade", 0, 0),
    JAFFA("Old Jaffa", "Ancient port city with historic clock tower, flea market and stone alleys", 150, 500),
    JERUSALEM("Jerusalem", "Holy City, Western Wall, Old City quarters and the Mahane Yehuda market", 800, 800),
    DEAD_SEA("Dead Sea", "Lowest point on Earth, mineral mud and spa resorts", 1500, 1200),
    NEGEV_DESERT("Negev Desert", "Southern desert expanse, rocky craters, dunes and camel tracks", 1000, 2000),
    MEDITERRANEAN_COAST("Mediterranean Coast", "Sandy beaches, dunes, and coastal trade routes", -200, 200),
    GALILEE_GOLAN("Galilee & Golan", "Fertile northern hills, olive groves, vineyards and orchards", -600, -800),
    RURAL_SETTLEMENTS("Kibbutzim & Moshavim", "Agricultural cooperatives and rural kibbutz communities", -400, -200);

    private final String displayName;
    private final String description;
    private final int centerX;
    private final int centerZ;

    IsraelRegion(String displayName, String description, int centerX, int centerZ) {
        this.displayName = displayName;
        this.description = description;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }
}
