package com.israelsimulator.item.cultural;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.core.data.RarityLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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

    public static final ResourceKey<EquipmentAsset> TALIT_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "talit"));

    public static final ResourceKey<net.minecraft.world.item.JukeboxSong> HAVA_NAGILA_SONG =
            ResourceKey.create(net.minecraft.core.registries.Registries.JUKEBOX_SONG, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "hava_nagila"));

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

    /** Talit — wearable cultural chest item, RARE, unstackable, swappable, no damage on hurt. */
    public static Item.Properties talit(Item.Properties p) {
        Equippable equippable = Equippable.builder(EquipmentSlot.CHEST)
                .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                .setAsset(TALIT_ASSET)
                .setSwappable(true)
                .setDamageOnHurt(false)
                .build();

        return p.stacksTo(1)
                .rarity(RarityLevel.RARE.vanilla())
                .component(DataComponents.EQUIPPABLE, equippable);
    }

    /** Tefillin — contextual interaction item, RARE, unstackable. */
    public static Item.Properties tefillin(Item.Properties p) {
        return p.stacksTo(1).rarity(RarityLevel.RARE.vanilla());
    }

    /**
     * Rabbi's Crown — MYTHIC head item, +20 Armor, unstackable, fire-resistant, swappable.
     * Deliberately has no equipment asset: without one, vanilla's CustomHeadLayer renders the
     * 3D item model (models/item/rabbis_crown.json, "head" display) on the wearer's head, so the
     * crown follows head rotation and sneaking on players and on humanoid mobs such as Bibi.
     */
    public static Item.Properties rabbisCrown(Item.Properties p) {
        Equippable equippable = Equippable.builder(EquipmentSlot.HEAD)
                .setEquipSound(SoundEvents.ARMOR_EQUIP_NETHERITE)
                .setSwappable(true)
                .setDamageOnHurt(true)
                .build();

        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "armor_rabbis_crown"),
                                20.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.HEAD
                )
                .build();

        return p.stacksTo(1)
                .durability(500)
                .rarity(RarityLevel.MYTHIC.vanilla())
                .fireResistant()
                .component(DataComponents.EQUIPPABLE, equippable)
                .attributes(modifiers);
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
        return p.stacksTo(1)
                .rarity(RarityLevel.LEGENDARY.vanilla())
                .fireResistant()
                .jukeboxPlayable(HAVA_NAGILA_SONG);
    }
}
