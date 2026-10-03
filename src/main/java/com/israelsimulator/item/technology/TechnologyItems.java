package com.israelsimulator.item.technology;

import com.israelsimulator.core.data.RarityLevel;
import net.minecraft.world.item.Item;

/**
 * Technology District items (GAME_DESIGN.md §31).
 *
 * <p>Symbolic/crafting components — no behavior yet. Useful behaviors (e.g., summoning
 * drones, opening a tech trading UI) come with the Technology District milestone.</p>
 */
public final class TechnologyItems {
    private TechnologyItems() {}

    public static Item.Properties smartphone(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.UNCOMMON.vanilla());
    }

    public static Item.Properties laptop(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.UNCOMMON.vanilla());
    }

    public static Item.Properties dronePart(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.COMMON.vanilla());
    }
}
