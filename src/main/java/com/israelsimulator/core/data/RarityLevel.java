package com.israelsimulator.core.data;

import com.israelsimulator.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Objects;

/**
 * Domain rarity levels for Israel-Simulator, per GAME_DESIGN.md §47 and TODO.md §51.
 *
 * <p>Tiers defined:</p>
 * <ul>
 *   <li><b>COMMON</b> - Standard everyday items (Shekels, basic food, stone/wood tools).</li>
 *   <li><b>UNCOMMON</b> - Cultural foods, crafted tools, basic religious garments.</li>
 *   <li><b>RARE</b> - Dead Sea mud products, refined tech components, specialized items.</li>
 *   <li><b>EPIC</b> - Laptops, drones, Tefillin, ancient coins, high-tech devices.</li>
 *   <li><b>LEGENDARY</b> - First Amendment parchment, Hava Nagila music disc, unique regional relics.</li>
 *   <li><b>MYTHIC</b> - Rabbi's Crown (unique ceremonial high relic, non-craftable).</li>
 * </ul>
 */
public enum RarityLevel {
    COMMON(Rarity.COMMON, ChatFormatting.WHITE, 100, 0),
    UNCOMMON(Rarity.UNCOMMON, ChatFormatting.YELLOW, 50, 1),
    RARE(Rarity.RARE, ChatFormatting.AQUA, 20, 2),
    EPIC(Rarity.EPIC, ChatFormatting.DARK_PURPLE, 8, 3),
    LEGENDARY(Rarity.EPIC, ChatFormatting.GOLD, 2, 4),
    MYTHIC(Rarity.EPIC, ChatFormatting.LIGHT_PURPLE, 1, 5);

    private final Rarity vanilla;
    private final ChatFormatting formatting;
    private final int defaultLootWeight;
    private final int tierIndex;

    RarityLevel(Rarity vanilla, ChatFormatting formatting, int defaultLootWeight, int tierIndex) {
        this.vanilla = vanilla;
        this.formatting = formatting;
        this.defaultLootWeight = defaultLootWeight;
        this.tierIndex = tierIndex;
    }

    /** Vanilla rarity used for item properties (name color, glint). */
    public Rarity vanilla() {
        return vanilla;
    }

    public ChatFormatting formatting() {
        return formatting;
    }

    public int defaultLootWeight() {
        return defaultLootWeight;
    }

    public int tierIndex() {
        return tierIndex;
    }

    public boolean isAtLeast(RarityLevel other) {
        return this.tierIndex >= other.tierIndex;
    }

    /**
     * Formats a tooltip component reflecting the rarity tier.
     */
    public MutableComponent getFormattedBadge() {
        return Component.literal("[" + name() + "]").withStyle(formatting);
    }

    /**
     * Identifies the domain rarity of a mod item or item stack.
     */
    public static RarityLevel getRarity(Item item) {
        Objects.requireNonNull(item, "Item must not be null");
        if (item == ModItems.RABBIS_CROWN.get()) {
            return MYTHIC;
        }
        if (item == ModItems.FIRST_AMENDMENT.get() || item == ModItems.HAVA_NAGILA_DISC.get()) {
            return LEGENDARY;
        }
        if (item == ModItems.TEFILLIN.get() || item == ModItems.LAPTOP.get()
                || item == ModItems.DRONE_PART.get() || item == ModItems.ANCIENT_COIN.get()) {
            return EPIC;
        }
        if (item == ModItems.SMARTPHONE.get() || item == ModItems.DEAD_SEA_MUD.get()
                || item == ModItems.SHOFAR.get() || item == ModItems.DREIDEL.get()
                || item == ModItems.ISRAEL_MAP.get() || item == ModItems.WALKING_SHOES.get()
                || item == ModItems.RAV_KAV.get() || item == ModItems.BICYCLE.get()) {
            return RARE;
        }
        if (item == ModItems.KIPPAH.get() || item == ModItems.TALIT.get()
                || item == ModItems.FALAFEL.get() || item == ModItems.SHAKSHUKA.get()
                || item == ModItems.HUMMUS.get() || item == ModItems.SUFGANIYAH.get()
                || item == ModItems.HAMANTASH.get() || item == ModItems.MATZO.get()
                || item == ModItems.CHALLAH.get() || item == ModItems.SABICH.get()) {
            return UNCOMMON;
        }
        return COMMON;
    }

    public static RarityLevel getRarity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return COMMON;
        }
        return getRarity(stack.getItem());
    }
}
