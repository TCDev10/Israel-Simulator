package com.israelsimulator.item;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Item tags for the simplified kosher system (GAME_DESIGN.md §18).
 *
 * <p>Categorizes foods into meat, dairy, and pareve (neutral).
 * Enforced by {@link com.israelsimulator.event.KosherEvents} when enabled.</p>
 */
public final class ModItemTags {
    public static final TagKey<Item> MEAT = tag("meat");
    public static final TagKey<Item> DAIRY = tag("dairy");
    public static final TagKey<Item> PAREVE = tag("pareve");
    public static final TagKey<Item> KOSHER = tag("kosher");

    private ModItemTags() {}

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name));
    }
}
