package com.israelsimulator.item.currency;

import com.israelsimulator.core.data.RarityLevel;
import net.minecraft.world.item.Item;

/**
 * Economic currency items (GAME_DESIGN.md §27).
 *
 * <p>Stackable, common-tier. Value/exchange rates and shop integration are defined
 * with the economy system.</p>
 */
public final class CurrencyItems {
    private CurrencyItems() {}

    public static Item.Properties shekel(Item.Properties p) {
        return p.stacksTo(64).rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties agora(Item.Properties p) {
        return p.stacksTo(64).rarity(RarityLevel.COMMON.vanilla());
    }
}
