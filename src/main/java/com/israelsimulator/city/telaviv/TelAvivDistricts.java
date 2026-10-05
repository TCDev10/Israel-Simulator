package com.israelsimulator.city.telaviv;

import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * Recognized districts and architectural identities of Tel Aviv (GAME_DESIGN.md §7).
 */
public record TelAvivDistricts(String id, String displayName, String architecturalStyle, List<String> keyFeatures) {
    public static final TelAvivDistricts WHITE_CITY = new TelAvivDistricts(
            "white_city",
            "White City",
            "Bauhaus / International Style",
            List.of("White concrete curves", "Ribbon windows", "Flat roofs", "Minimalist balconies")
    );

    public static final TelAvivDistricts ROTHSCHILD = new TelAvivDistricts(
            "rothschild_boulevard",
            "Rothschild Boulevard",
            "Urban Promenade",
            List.of("Shaded tree median", "Green coffee kiosks", "Pedestrian park benches", "Bicycle lanes")
    );

    public static final TelAvivDistricts FLORENTIN = new TelAvivDistricts(
            "florentin",
            "Florentin",
            "Bohemian Artisan",
            List.of("Street art murals", "Loft workshops", "Vintage cafes", "Brick & concrete facades")
    );

    public static final TelAvivDistricts SARONA = new TelAvivDistricts(
            "sarona",
            "Sarona",
            "Restored Templar Heritage",
            List.of("German Templar stone houses", "Gourmet culinary markets", "Green public squares")
    );

    public static final TelAvivDistricts STARTUP_DISTRICT = new TelAvivDistricts(
            "startup_district",
            "Startup & Technology District",
            "Modern High-Tech High-Rise",
            List.of("Glass & sea lantern facades", "IT workstations", "Server racks", "Tech venture offices")
    );

    public static final TelAvivDistricts TAYELET_BEACH = new TelAvivDistricts(
            "tayelet_beach",
            "Tayelet & Beachfront",
            "Mediterranean Coastal Boardwalk",
            List.of("Sandy coastline", "Striped lifeguard towers", "Beach umbrellas", "Seaside restaurants")
    );

    public static final List<TelAvivDistricts> ALL = List.of(
            WHITE_CITY,
            ROTHSCHILD,
            FLORENTIN,
            SARONA,
            STARTUP_DISTRICT,
            TAYELET_BEACH
    );

    public static TelAvivDistricts fromId(String id) {
        if (id == null) {
            return null;
        }
        for (TelAvivDistricts district : ALL) {
            if (district.id().equalsIgnoreCase(id)) {
                return district;
            }
        }
        return null;
    }

    /**
     * Determines the district based on block coordinates.
     * Uses a 3x2 modular zoning grid (each cell is 64x64 blocks).
     */
    public static TelAvivDistricts getDistrictAt(int blockX, int blockZ) {
        int col = Math.floorMod(blockX >> 6, 3);
        int row = Math.floorMod(blockZ >> 6, 2);
        if (col == 0) {
            return row == 0 ? WHITE_CITY : ROTHSCHILD;
        } else if (col == 1) {
            return row == 0 ? STARTUP_DISTRICT : SARONA;
        } else {
            return row == 0 ? TAYELET_BEACH : FLORENTIN;
        }
    }

    public static TelAvivDistricts getDistrictAt(BlockPos pos) {
        return getDistrictAt(pos.getX(), pos.getZ());
    }

    /**
     * Economic price multiplier for items traded in this district.
     */
    public double getEconomyMultiplier(String itemId) {
        if (itemId == null) {
            return 1.0;
        }
        return switch (this.id) {
            case "startup_district" -> (itemId.equals("laptop") || itemId.equals("smartphone") || itemId.equals("drone_part")) ? 1.25 : 1.0;
            case "sarona" -> (itemId.equals("falafel") || itemId.equals("hummus") || itemId.equals("shakshuka")
                    || itemId.equals("sabich") || itemId.equals("rugelach") || itemId.equals("challah")) ? 1.20 : 1.0;
            case "florentin" -> (itemId.equals("ancient_coin") || itemId.equals("leather") || itemId.contains("dye")) ? 1.20 : 1.0;
            case "tayelet_beach" -> (itemId.contains("fish") || itemId.equals("emerald")) ? 1.15 : 1.0;
            default -> 1.0;
        };
    }

    public String getRecommendedProfession() {
        return switch (this.id) {
            case "startup_district" -> "developer";
            case "sarona" -> "butcher";
            case "florentin" -> "leatherworker";
            case "tayelet_beach" -> "fisherman";
            case "white_city" -> "mason";
            case "rothschild_boulevard" -> "librarian";
            default -> "none";
        };
    }

    public String getWelcomeMessage() {
        return "Entering " + displayName + " (" + architecturalStyle + ")";
    }
}
