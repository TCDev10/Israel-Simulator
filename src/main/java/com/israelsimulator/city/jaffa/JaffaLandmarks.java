package com.israelsimulator.city.jaffa;

import java.util.List;

/**
 * Recognized historical landmarks and areas of Jaffa (GAME_DESIGN.md §8).
 */
public record JaffaLandmarks(String id, String displayName, String historicalPeriod, String description) {
    public static final JaffaLandmarks CLOCK_TOWER = new JaffaLandmarks(
            "jaffa_clock_tower",
            "Jaffa Clock Tower",
            "Ottoman Era (1903)",
            "Limestone monumental tower standing at the entrance to the Old City."
    );

    public static final JaffaLandmarks OLD_PORT = new JaffaLandmarks(
            "jaffa_port",
            "Ancient Port of Jaffa",
            "Biblical / Mediterranean Antiquity",
            "One of the oldest known seaports in the world, exporting citrus and maritime goods."
    );

    public static final JaffaLandmarks FLEA_MARKET = new JaffaLandmarks(
            "shuk_hapishpeshim",
            "Shuk HaPishpeshim",
            "Historical Bazaar",
            "Bustling flea market full of antiques, coins, vintage Judaica, and brass wares."
    );

    public static final List<JaffaLandmarks> ALL = List.of(
            CLOCK_TOWER,
            OLD_PORT,
            FLEA_MARKET
    );
}
