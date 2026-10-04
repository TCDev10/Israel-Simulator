package com.israelsimulator.item.cultural;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.core.data.RarityLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Property builders for cultural/religious items (GAME_DESIGN.md §12–16, §34–35).
 *
 * <p>These only build {@link Item.Properties}. Equip behavior, interactions,
 * animations, and rewards are wired in later milestones (Western Wall, Kippah/Talit/Tefillin
 * systems, boss drops, etc.).</p>
 */
public final class CulturalItems {
    public static final ResourceKey<EquipmentAsset> KIPPAH_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "kippah"));

    private CulturalItems() {}

    /** Kippah — wearable head item, UNCOMMON, unstackable, swappable, no damage on hurt. */
    public static Item.Properties kippah(Item.Properties p) {
        Equippable equippable = Equippable.builder(EquipmentSlot.HEAD)
                .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                .setAsset(KIPPAH_ASSET)
                .setSwappable(true)
                .setDamageOnHurt(false)
                .build();

        return p.stacksTo(1)
                .rarity(RarityLevel.UNCOMMON.vanilla())
                .component(DataComponents.EQUIPPABLE, equippable);
    }

    /** Talit — wearable cultural item, RARE, unstackable. */
    public static Item.Properties talit(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.RARE.vanilla());
    }

    /** Tefillin — contextual interaction item, RARE, unstackable. */
    public static Item.Properties tefillin(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.RARE.vanilla());
    }

    /** Rabbi's Crown — MYTHIC. Fire-resistant, unstackable. Not craftable, not common loot. */
    public static Item.Properties rabbisCrown(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.MYTHIC.vanilla()).fireResistant();
    }

    /** Prayer Note — consumable interaction resource for the Western Wall ritual. */
    public static Item.Properties prayerNote(Item.Properties p) {
        return p.stacksTo(16).rarity(RarityLevel.COMMON.vanilla());
    }

    /** First Amendment — LEGENDARY collectible. Unstackable, fire-resistant. */
    public static Item.Properties firstAmendment(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.LEGENDARY.vanilla()).fireResistant();
    }

    /** Hava Nagila music disc — LEGENDARY boss drop. Unstackable. */
    public static Item.Properties havaNagilaDisc(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.LEGENDARY.vanilla()).fireResistant();
    }
}
