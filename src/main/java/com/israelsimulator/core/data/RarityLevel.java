package com.israelsimulator.core.data;

import net.minecraft.world.item.Rarity;

/**
 * Domain rarity levels for Israel-Simulator, per GAME_DESIGN.md §47.
 *
 * <p>Minecraft's vanilla {@link Rarity} enum only provides COMMON/UNCOMMON/RARE/EPIC.
 * The mod's custom tiers (MYTHIC, LEGENDARY) are represented here as domain concepts
 * that map onto the closest vanilla rarity for rendering/tooltip purposes; the richer
 * domain tier is preserved for drop rules, loot gating, and future tooltip styling.</p>
 *
 * <p>Mapping:</p>
 * <ul>
 *   <li>MYTHIC   → {@link Rarity#EPIC} (plus fire-resistant + not craftable by default)</li>
 *   <li>LEGENDARY → {@link Rarity#EPIC}</li>
 *   <li>RARE      → {@link Rarity#RARE}</li>
 *   <li>UNCOMMON  → {@link Rarity#UNCOMMON}</li>
 *   <li>COMMON    → {@link Rarity#COMMON}</li>
 * </ul>
 */
public enum RarityLevel {
    COMMON(Rarity.COMMON),
    UNCOMMON(Rarity.UNCOMMON),
    RARE(Rarity.RARE),
    LEGENDARY(Rarity.EPIC),
    MYTHIC(Rarity.EPIC);

    private final Rarity vanilla;

    RarityLevel(Rarity vanilla) {
        this.vanilla = vanilla;
    }

    /** Vanilla rarity used for item properties (name color, glint). */
    public Rarity vanilla() {
        return vanilla;
    }
}
