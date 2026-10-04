package com.israelsimulator.agriculture;

import com.israelsimulator.registry.ModItems;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Supported agricultural commodities and their standard rural economy valuation.
 *
 * <p>Values connect agricultural production directly to the Shekel currency
 * as specified in GAME_DESIGN.md (§19, §27, §28, §29).</p>
 */
public enum AgriculturalCommodity {
    OLIVE("Olives", 8, 1, 0),
    DATE("Dates", 6, 1, 0),
    CITRUS("Citrus", 6, 1, 0),
    WHEAT("Wheat", 16, 1, 0),
    CARROT("Carrots", 12, 1, 0),
    POTATO("Potatoes", 12, 1, 0),
    BEETROOT("Beetroots", 12, 1, 0);

    private final String displayName;
    private final int itemsPerShekel;
    private final int shekelPayout;
    private final int agoraPayout;

    AgriculturalCommodity(String displayName, int itemsPerShekel, int shekelPayout, int agoraPayout) {
        this.displayName = displayName;
        this.itemsPerShekel = itemsPerShekel;
        this.shekelPayout = shekelPayout;
        this.agoraPayout = agoraPayout;
    }

    public String displayName() {
        return displayName;
    }

    public int itemsPerShekel() {
        return itemsPerShekel;
    }

    public int shekelPayout() {
        return shekelPayout;
    }

    public int agoraPayout() {
        return agoraPayout;
    }

    public static Optional<AgriculturalCommodity> fromItem(Item item) {
        if (item == ModItems.OLIVES.get()) return Optional.of(OLIVE);
        if (item == ModItems.DATES.get()) return Optional.of(DATE);
        if (item == ModItems.CITRUS.get()) return Optional.of(CITRUS);
        if (item == Items.WHEAT) return Optional.of(WHEAT);
        if (item == Items.CARROT) return Optional.of(CARROT);
        if (item == Items.POTATO) return Optional.of(POTATO);
        if (item == Items.BEETROOT) return Optional.of(BEETROOT);
        return Optional.empty();
    }

    public static Optional<AgriculturalCommodity> fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        return fromItem(stack.getItem());
    }
}

