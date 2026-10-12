package com.israelsimulator.entity.npc;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;

/**
 * Goyim NPC replaces vanilla villagers visually and by name while preserving
 * vanilla villager trades and professions (GAME_DESIGN.md §26-29).
 * Server-authoritative; client only renders.
 */
public class GoyimEntity extends AbstractVillager {

    public GoyimEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createVillagerAttributes();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 8.0F));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        // Ensure custom name is set for Goyim
        if (getCustomName() == null) {
            setCustomName(Component.literal("Goy"));
            setCustomNameVisible(true);
        }
    }
}
