package com.israelsimulator.entity.mossad;

import com.israelsimulator.mossad.MossadMissions;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Informant escorted during the ESCORT mission: follows its escort, teleports back if left more than
 * {@value #TELEPORT_DISTANCE} blocks behind, and fails the mission if it dies.
 */
public class MossadInformantEntity extends PathfinderMob {
    public static final double TELEPORT_DISTANCE = 24.0;
    private UUID escort;

    public MossadInformantEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCustomName(Component.translatable("entity.israel_simulator.mossad_informant"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public void setEscort(UUID player) {
        this.escort = player;
    }

    public UUID escort() {
        return escort;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level) || escort == null || this.tickCount % 10 != 0) {
            return;
        }
        Player player = level.getPlayerByUUID(escort);
        if (player == null) {
            this.getNavigation().stop();
            return;
        }
        double d = this.distanceTo(player);
        if (d > TELEPORT_DISTANCE) {
            this.teleportTo(player.getX(), player.getY(), player.getZ());
        } else if (d > 3.0) {
            this.getNavigation().moveTo(player, 1.1);
        } else {
            this.getNavigation().stop();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level && escort != null) {
            MossadMissions.onInformantLost(level, escort, this.getUUID());
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (escort != null) {
            output.putString("Escort", escort.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String s = input.getStringOr("Escort", "");
        escort = s.isEmpty() ? null : UUID.fromString(s);
    }
}
