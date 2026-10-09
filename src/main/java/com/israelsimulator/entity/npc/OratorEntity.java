package com.israelsimulator.entity.npc;

import com.israelsimulator.entity.boss.ArenaRestoreRules;
import com.israelsimulator.entity.boss.EpsteinArenaSnapshots;
import com.israelsimulator.event.world.speech.PublicSpeechEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The Orator: a fictional, generic speaker who hosts the Public Speech event from the stage.
 * Not based on any real person. Stands still, looks at the audience, and is removed (with the
 * stage restored) when the event ends.
 */
public class OratorEntity extends PathfinderMob {

    public OratorEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.setPersistenceRequired();
        this.setCustomName(Component.translatable("entity.israel_simulator.orator"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // Left over from a previous session (event state is not kept across restarts): leave the stage.
        if (this.level() instanceof ServerLevel && this.tickCount > 20 && !PublicSpeechEvent.isOrator(this.getUUID())) {
            this.discard();
        }
    }

    @Override
    public void onRemoval(Entity.RemovalReason reason) {
        super.onRemoval(reason);
        if (ArenaRestoreRules.restoresOnRemoval(reason.shouldDestroy()) && this.level() instanceof ServerLevel serverLevel) {
            EpsteinArenaSnapshots.restore(serverLevel, this.getUUID());
            if (PublicSpeechEvent.isOrator(this.getUUID())) {
                PublicSpeechEvent.end(serverLevel, false);
            }
        }
    }
}
