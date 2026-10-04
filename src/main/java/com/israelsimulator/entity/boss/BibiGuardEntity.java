package com.israelsimulator.entity.boss;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
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
 * Coalition Elite Guard summoned by the Bibi Boss during combat (GAME_DESIGN.md §43, TODO §44).
 * Coordinates defense around the boss and automatically despawns if the boss is defeated.
 */
public class BibiGuardEntity extends Monster {

    private UUID bossUuid;
    private int lifeTicksRemaining = 6000; // 5 minute max lifetime to prevent runaway mob population

    public BibiGuardEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public void setBossUuid(UUID bossUuid) {
        this.bossUuid = bossUuid;
    }

    public UUID getBossUuid() {
        return this.bossUuid;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            lifeTicksRemaining--;
            if (lifeTicksRemaining <= 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            this.getX(), this.getY() + 0.5, this.getZ(),
                            10, 0.2, 0.2, 0.2, 0.05);
                }
                this.discard();
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("LifeTicksRemaining", this.lifeTicksRemaining);
        if (this.bossUuid != null) {
            output.putString("BossUUID", this.bossUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifeTicksRemaining = input.getIntOr("LifeTicksRemaining", 6000);
        String uuidStr = input.getStringOr("BossUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.bossUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
