package com.israelsimulator.entity.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;

/**
 * Goyim NPC represents non-player villager citizens while preserving
 * vanilla villager trades and professions (GAME_DESIGN.md §23, §26–29).
 * Server-authoritative; client only renders.
 */
public class GoyimEntity extends Villager {

    public GoyimEntity(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createAttributes();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (getCustomName() == null) {
            setCustomName(Component.literal("Goy"));
            setCustomNameVisible(true);
        }
    }
}
