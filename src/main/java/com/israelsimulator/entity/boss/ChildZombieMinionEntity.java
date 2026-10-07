package com.israelsimulator.entity.boss;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Child-skinned baby zombie minion summoned by Jeffrey Epstein during Bibi Phase 3 (GAME_DESIGN §41–44).
 * Deliberately designed with low HP (12.0 HP) and low damage (2.0) to be fast and easy to kill.
 */
public class ChildZombieMinionEntity extends Monster {

    private UUID epsteinBossUuid;
    private int lifeTicksRemaining = 3600; // 3 minute maximum lifetime

    public ChildZombieMinionEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)     // Low HP: easy to kill in 1-2 hits
                .add(Attributes.MOVEMENT_SPEED, 0.30) // Quick baby mobility
                .add(Attributes.ATTACK_DAMAGE, 2.0)   // Low damage
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public void setEpsteinBossUuid(UUID uuid) {
        this.epsteinBossUuid = uuid;
    }

    public UUID getEpsteinBossUuid() {
        return this.epsteinBossUuid;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (BibiBossEntity.isAlly(target)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@org.jspecify.annotations.Nullable LivingEntity target) {
        if (BibiBossEntity.isAlly(target)) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            lifeTicksRemaining--;
            if (lifeTicksRemaining <= 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF,
                            this.getX(), this.getY() + 0.3, this.getZ(),
                            8, 0.2, 0.2, 0.2, 0.05);
                }
                this.discard();
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("LifeTicks", this.lifeTicksRemaining);
        if (this.epsteinBossUuid != null) {
            output.putString("EpsteinUUID", this.epsteinBossUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifeTicksRemaining = input.getIntOr("LifeTicks", 3600);
        String uuidStr = input.getStringOr("EpsteinUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.epsteinBossUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}

