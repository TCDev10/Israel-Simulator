package com.israelsimulator.trading;

import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.reputation.ReputationFaction;
import com.israelsimulator.reputation.ReputationManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

/**
 * Enhanced trading authority providing special trades, reputation modifiers,
 * Blessed Trader interactions, and anti-duplication safeguards (GAME_DESIGN.md §30, TODO §30).
 */
public final class IsraelVillagerTrades {

    public record SpecialTrade(
            String inputItemId,
            int inputCount,
            String outputItemId,
            int outputCount,
            int maxUses,
            boolean isRare,
            boolean isBlessedOnly
    ) {}

    private static final List<SpecialTrade> CULTURAL_TRADES = new ArrayList<>();
    private static final List<SpecialTrade> AGRICULTURAL_TRADES = new ArrayList<>();
    private static final List<SpecialTrade> TECH_TRADES = new ArrayList<>();
    private static final List<SpecialTrade> FOOD_TRADES = new ArrayList<>();

    static {
        // Cultural / Judaica Trades
        CULTURAL_TRADES.add(new SpecialTrade("shekel", 35, "kippah", 1, 12, false, false));
        CULTURAL_TRADES.add(new SpecialTrade("shekel", 60, "mezuzah", 1, 8, false, false));
        CULTURAL_TRADES.add(new SpecialTrade("shekel", 120, "talit", 1, 6, false, false));
        CULTURAL_TRADES.add(new SpecialTrade("shekel", 350, "tefillin", 1, 4, true, false));
        // Blessed Trader exclusives
        CULTURAL_TRADES.add(new SpecialTrade("shekel", 100, "ancient_coin", 1, 3, true, true));
        CULTURAL_TRADES.add(new SpecialTrade("shekel", 150, "shofar", 1, 3, true, true));
        CULTURAL_TRADES.add(new SpecialTrade("shekel", 40, "star_of_david", 1, 6, true, true));

        // Agricultural Trades
        AGRICULTURAL_TRADES.add(new SpecialTrade("shekel", 6, "olives", 8, 16, false, false));
        AGRICULTURAL_TRADES.add(new SpecialTrade("shekel", 9, "dates", 8, 16, false, false));
        AGRICULTURAL_TRADES.add(new SpecialTrade("shekel", 5, "citrus", 8, 16, false, false));

        // Food Trades
        FOOD_TRADES.add(new SpecialTrade("shekel", 18, "falafel", 4, 16, false, false));
        FOOD_TRADES.add(new SpecialTrade("shekel", 25, "hummus", 2, 16, false, false));
        FOOD_TRADES.add(new SpecialTrade("shekel", 32, "shakshuka", 2, 12, false, false));
        FOOD_TRADES.add(new SpecialTrade("shekel", 15, "challah", 2, 12, false, false));

        // High-Tech Trades
        TECH_TRADES.add(new SpecialTrade("shekel", 450, "drone_part", 1, 4, true, false));
        TECH_TRADES.add(new SpecialTrade("shekel", 1500, "smartphone", 1, 2, true, false));
        TECH_TRADES.add(new SpecialTrade("shekel", 3500, "laptop", 1, 1, true, false));
    }

    private IsraelVillagerTrades() {}

    public static List<SpecialTrade> getCulturalTrades() {
        return List.copyOf(CULTURAL_TRADES);
    }

    public static List<SpecialTrade> getAgriculturalTrades() {
        return List.copyOf(AGRICULTURAL_TRADES);
    }

    public static List<SpecialTrade> getFoodTrades() {
        return List.copyOf(FOOD_TRADES);
    }

    public static List<SpecialTrade> getTechTrades() {
        return List.copyOf(TECH_TRADES);
    }

    /**
     * Checks if player has the Blessed effect active to qualify for the Blessed Trader interaction.
     */
    public static boolean isBlessedTraderEligible(Player player) {
        if (player == null) {
            return false;
        }
        return player.hasEffect(com.israelsimulator.effect.ModEffects.BLESSED);
    }

    /**
     * Pure validation logic for Blessed Trader status, testable offline.
     */
    public static boolean isBlessedTraderEligible(boolean hasBlessedEffect) {
        return hasBlessedEffect;
    }

    /**
     * Calculates the adjusted price in Shekels taking reputation and Blessed Trader status into account.
     */
    public static int calculateAdjustedPrice(int baseShekels, UUID playerId, ReputationFaction faction, boolean isBlessed) {
        double modifier = ReputationManager.getPriceModifier(playerId, faction);
        double finalMultiplier = 1.0 + modifier;

        // Blessed Trader grants an additional 15% holy discount
        if (isBlessed) {
            finalMultiplier *= 0.85;
        }

        int adjusted = (int) Math.round(baseShekels * finalMultiplier);
        return Math.max(1, adjusted);
    }

    /**
     * Validates a trade execution server-side to prevent item duplication or exploit loops.
     */
    public static boolean validateTrade(SpecialTrade trade, int heldCurrency, int currentUses,
                                         boolean hasRareAccess, boolean isBlessed) {
        if (trade == null) {
            return false;
        }
        if (currentUses >= trade.maxUses()) {
            return false; // Out of stock
        }
        if (trade.isBlessedOnly() && !isBlessed) {
            return false; // Only blessed players can see/buy blessed items
        }
        if (trade.isRare() && !hasRareAccess && !isBlessed) {
            return false; // Requires honored reputation or divine blessing
        }
        return heldCurrency >= trade.inputCount();
    }
}
