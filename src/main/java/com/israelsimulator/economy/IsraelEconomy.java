package com.israelsimulator.economy;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Unified economy and pricing authority for Israel-Simulator (GAME_DESIGN.md §29, TODO §29).
 * Features authoritative price curves, regional specializations, reputation discounts,
 * and robust anti-arbitrage / anti-duplication safeguards.
 */
public final class IsraelEconomy {

    public static final double MIN_SPREAD = 0.20; // 20% minimum bid-ask spread
    public static final int MAX_REPUTATION = 100;
    public static final int MIN_REPUTATION = -100;
    public static final double MAX_REPUTATION_DISCOUNT = 0.20; // Up to 20% discount
    public static final double MAX_REPUTATION_BONUS = 0.10; // Up to 10% sale bonus

    // Base pricing map in Agorot (1 Shekel = 100 Agorot)
    private static final Map<String, PriceEntry> BASE_PRICES = new HashMap<>();

    public record PriceEntry(long baseAgorot, ProductCategory category) {}

    static {
        // Food items
        registerPrice("falafel", 1800, ProductCategory.FOOD); // 18.00 ILS
        registerPrice("hummus", 2500, ProductCategory.FOOD); // 25.00 ILS
        registerPrice("shakshuka", 3200, ProductCategory.FOOD); // 32.00 ILS
        registerPrice("sabich", 2200, ProductCategory.FOOD); // 22.00 ILS
        registerPrice("challah", 1500, ProductCategory.FOOD); // 15.00 ILS
        registerPrice("rugelach", 800, ProductCategory.FOOD); // 8.00 ILS
        registerPrice("sufganiyah", 1200, ProductCategory.FOOD); // 12.00 ILS
        registerPrice("matzo", 1000, ProductCategory.FOOD); // 10.00 ILS
        registerPrice("tahini", 1400, ProductCategory.FOOD); // 14.00 ILS

        // Agriculture
        registerPrice("olives", 600, ProductCategory.AGRICULTURE); // 6.00 ILS
        registerPrice("dates", 900, ProductCategory.AGRICULTURE); // 9.00 ILS
        registerPrice("citrus", 500, ProductCategory.AGRICULTURE); // 5.00 ILS

        // Cultural & Judaica
        registerPrice("kippah", 3500, ProductCategory.JUDAICA_CULTURAL); // 35.00 ILS
        registerPrice("talit", 12000, ProductCategory.JUDAICA_CULTURAL); // 120.00 ILS
        registerPrice("tefillin", 35000, ProductCategory.JUDAICA_CULTURAL); // 350.00 ILS
        registerPrice("mezuzah", 6000, ProductCategory.JUDAICA_CULTURAL); // 60.00 ILS
        registerPrice("prayer_note", 500, ProductCategory.JUDAICA_CULTURAL); // 5.00 ILS
        registerPrice("star_of_david", 4500, ProductCategory.JUDAICA_CULTURAL); // 45.00 ILS
        registerPrice("olive_wood_carving", 7500, ProductCategory.JUDAICA_CULTURAL); // 75.00 ILS
        registerPrice("ancient_coin", 15000, ProductCategory.JUDAICA_CULTURAL); // 150.00 ILS
        registerPrice("shofar", 18000, ProductCategory.JUDAICA_CULTURAL); // 180.00 ILS
        registerPrice("dreidel", 1000, ProductCategory.JUDAICA_CULTURAL); // 10.00 ILS

        // Technology
        registerPrice("smartphone", 150000, ProductCategory.TECHNOLOGY); // 1500.00 ILS
        registerPrice("laptop", 350000, ProductCategory.TECHNOLOGY); // 3500.00 ILS
        registerPrice("drone_part", 45000, ProductCategory.TECHNOLOGY); // 450.00 ILS

        // Minerals & Commodities
        registerPrice("salt_block", 1200, ProductCategory.COMMODITIES_MINERALS); // 12.00 ILS
        registerPrice("dead_sea_mud", 2000, ProductCategory.COMMODITIES_MINERALS); // 20.00 ILS
    }

    private IsraelEconomy() {}

    public static void registerPrice(String itemId, long baseAgorot, ProductCategory category) {
        BASE_PRICES.put(itemId, new PriceEntry(baseAgorot, category));
    }

    public static PriceEntry getPriceEntry(String itemId) {
        return BASE_PRICES.getOrDefault(itemId, new PriceEntry(1000, ProductCategory.COMMODITIES_MINERALS));
    }

    public static PriceEntry getPriceEntry(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return getPriceEntry(id.getPath());
    }

    /**
     * Calculates the price a player pays to BUY an item from an NPC or shop.
     * Takes regional multipliers and reputation discount into account.
     */
    public static long calculateBuyPrice(String itemId, CityRegion region, int reputation) {
        PriceEntry entry = getPriceEntry(itemId);
        double regionMult = region != null ? region.getCategoryMultiplier(entry.category()) : 1.0;
        double base = entry.baseAgorot() * regionMult;

        // Reputation discount: +100 reputation gives 20% discount; -100 gives 20% surcharge
        double repFactor = Math.clamp(reputation, MIN_REPUTATION, MAX_REPUTATION) / (double) MAX_REPUTATION;
        double discount = repFactor * MAX_REPUTATION_DISCOUNT;
        double finalPrice = base * (1.0 - discount);

        return Math.max(1L, Math.round(finalPrice));
    }

    /**
     * Calculates the price an NPC pays to BUY an item from a player (player SELLS item).
     * Enforces strict bid-ask spread and reputation bonus.
     */
    public static long calculateSellPrice(String itemId, CityRegion region, int reputation) {
        long buyPrice = calculateBuyPrice(itemId, region, reputation);

        // Player sells at (1.0 - MIN_SPREAD) of buy price, with slight bonus for high reputation
        double repFactor = Math.max(0, Math.clamp(reputation, MIN_REPUTATION, MAX_REPUTATION)) / (double) MAX_REPUTATION;
        double repBonus = repFactor * MAX_REPUTATION_BONUS;

        double sellRatio = (1.0 - MIN_SPREAD) + (repBonus * 0.5); // Never exceeds 1 - (MIN_SPREAD/2)
        sellRatio = Math.min(1.0 - (MIN_SPREAD / 2.0), sellRatio);

        long sellPrice = Math.round(buyPrice * sellRatio);
        return Math.max(1L, Math.min(sellPrice, buyPrice - 1L)); // Sell price strictly less than buy price
    }

    /**
     * Validates that an inter-city arbitrage loop is non-exploitative.
     * Player cannot buy at regionA and immediately sell at regionB for free profit without legitimate trade margin.
     */
    public static boolean isArbitrageSafe(String itemId, CityRegion sourceRegion, CityRegion targetRegion) {
        long buyAtSource = calculateBuyPrice(itemId, sourceRegion, 0);
        long sellAtTarget = calculateSellPrice(itemId, targetRegion, 0);
        // Profit cannot exceed 20% even in optimal regional paths
        return sellAtTarget <= (long) (buyAtSource * 1.20);
    }
}
