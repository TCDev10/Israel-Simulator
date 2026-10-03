package com.israelsimulator.item.collectible;

import com.israelsimulator.core.data.RarityLevel;
import net.minecraft.world.item.Item;

/**
 * Collectible exploration/discovery items (GAME_DESIGN.md §2.4).
 *
 * <p>Purely collectible/trade content at this milestone. Special interactions
 * (e.g., Mezuzah placement, Ancient Coin trading) come later.</p>
 */
public final class CollectibleItems {
    private CollectibleItems() {}

    public static Item.Properties mezuzah(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.UNCOMMON.vanilla());
    }

    public static Item.Properties starOfDavid(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.UNCOMMON.vanilla());
    }

    public static Item.Properties oliveWoodCarving(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties ancientCoin(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.RARE.vanilla());
    }

    public static Item.Properties deadSeaScrollFragment(Item.Properties p) {
        return p.stacksTo(8).rarity(RarityLevel.RARE.vanilla());
    }
}
