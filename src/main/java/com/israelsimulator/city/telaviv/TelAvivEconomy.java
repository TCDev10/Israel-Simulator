package com.israelsimulator.city.telaviv;

/**
 * Economy rates and item valuations in Tel Aviv's urban centers (GAME_DESIGN.md §7, §28).
 */
public final class TelAvivEconomy {
    // Tech item exchange rates (in Shekels)
    public static final int LAPTOP_BUY_PRICE = 32;
    public static final int LAPTOP_SELL_PRICE = 24;

    public static final int SMARTPHONE_BUY_PRICE = 16;
    public static final int SMARTPHONE_SELL_PRICE = 12;

    public static final int DRONE_PART_BUY_PRICE = 8;
    public static final int DRONE_PART_SELL_PRICE = 6;

    // Restaurant / street food prices (in Shekels)
    public static final int FALAFEL_PRICE = 2;
    public static final int HUMMUS_PRICE = 2;
    public static final int SHAKSHUKA_PRICE = 3;
    public static final int SABICH_PRICE = 3;

    private TelAvivEconomy() {}

    public static int getTechBuyPrice(String itemId) {
        return switch (itemId) {
            case "laptop" -> LAPTOP_BUY_PRICE;
            case "smartphone" -> SMARTPHONE_BUY_PRICE;
            case "drone_part" -> DRONE_PART_BUY_PRICE;
            default -> 0;
        };
    }

    public static int getTechSellPrice(String itemId) {
        return switch (itemId) {
            case "laptop" -> LAPTOP_SELL_PRICE;
            case "smartphone" -> SMARTPHONE_SELL_PRICE;
            case "drone_part" -> DRONE_PART_SELL_PRICE;
            default -> 0;
        };
    }
}
