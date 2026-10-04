package com.israelsimulator.city.telaviv;

import java.util.List;

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
}
