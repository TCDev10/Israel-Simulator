package com.israelsimulator.city.jerusalem;

/**
 * Economy rates for traditional Judaica and Old City Shuk goods in Jerusalem (GAME_DESIGN.md §9, §27).
 */
public final class JerusalemEconomy {
    public static final int PRAYER_NOTE_PRICE = 1;
    public static final int KIPPAH_PRICE = 4;
    public static final int MEZUZAH_PRICE = 6;
    public static final int TALIT_PRICE = 8;
    public static final int TEFILLIN_PRICE = 12;

    public static final int CHALLAH_PRICE = 2;
    public static final int RUGELACH_PRICE = 1;

    private JerusalemEconomy() {}

    public static int getJudaicaBuyPrice(String itemId) {
        return switch (itemId) {
            case "prayer_note" -> PRAYER_NOTE_PRICE;
            case "kippah" -> KIPPAH_PRICE;
            case "mezuzah" -> MEZUZAH_PRICE;
            case "talit" -> TALIT_PRICE;
            case "tefillin" -> TEFILLIN_PRICE;
            default -> 0;
        };
    }
}
