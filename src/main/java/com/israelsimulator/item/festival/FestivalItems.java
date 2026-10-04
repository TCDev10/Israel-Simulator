package com.israelsimulator.item.festival;

import com.israelsimulator.core.data.RarityLevel;
import com.israelsimulator.item.food.IsraelFoodProperties;
import net.minecraft.world.item.Item;

/**
 * Festival-themed items (GAME_DESIGN.md §36–40).
 *
 * <p>Matzo, Sufganiyah, and Hamantash use the nutrition already defined in
 * {@link IsraelFoodProperties}. Dreidel and Shofar stay non-food.</p>
 */
public final class FestivalItems {
    private FestivalItems() {}

    public static Item.Properties matzo(Item.Properties p) {
        return p.food(IsraelFoodProperties.MATZO, IsraelFoodProperties.MATZO_CONSUMABLE)
                .rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties sufganiyah(Item.Properties p) {
        return p.food(IsraelFoodProperties.SUFGANIYAH, IsraelFoodProperties.SUFGANIYAH_CONSUMABLE)
                .rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties dreidel(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties hamantash(Item.Properties p) {
        return p.food(IsraelFoodProperties.HAMANTASH, IsraelFoodProperties.HAMANTASH_CONSUMABLE)
                .rarity(RarityLevel.COMMON.vanilla());
    }

    public static Item.Properties shofar(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.UNCOMMON.vanilla());
    }
}
