package com.israelsimulator.item.festival;

import com.israelsimulator.core.data.RarityLevel;
import net.minecraft.world.item.Item;

/**
 * Festival-themed items (GAME_DESIGN.md §36–40).
 *
 * <p>Edible entries (Matzo, Sufganiyah, Hamantash) are plain collectibles here;
 * nutrition is provided only for items also listed in the food system to keep the
 * two databases in sync. Interactive items (Dreidel spin, Shofar sound) are wired
 * in the festival milestone.</p>
 */
public final class FestivalItems {
    private FestivalItems() {}

    public static Item.Properties matzo(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties sufganiyah(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties dreidel(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties hamantash(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties shofar(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.UNCOMMON.vanilla());
    }
}
