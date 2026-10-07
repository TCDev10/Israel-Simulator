package com.israelsimulator.entity.boss;

import com.israelsimulator.registry.ModItems;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Coalition Elite Guard summoned by the Bibi Boss during combat (GAME_DESIGN.md §43, TODO §44).
 * Armed with tactical security pistols, coordinates defense around the boss,
 * and automatically despawns if the boss is defeated.
 */
public class BibiGuardEntity extends Monster implements RangedAttackMob {

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
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, 35, 55, 18.0F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

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
            if (this.getMainHandItem().isEmpty() && ModItems.PISTOL != null) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.PISTOL.get()));
                this.setDropChance(EquipmentSlot.MAINHAND, 0.015F);
            }

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
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 muzzlePos = this.getEyePosition().add(this.getViewVector(1.0F).scale(0.4));
        Vec3 targetPos = target.getEyePosition();
        Vec3 dir = targetPos.subtract(muzzlePos).normalize();

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.7F, 1.8F);
        serverLevel.sendParticles(ParticleTypes.CRIT,
                muzzlePos.x + dir.x * 0.5, muzzlePos.y + dir.y * 0.5, muzzlePos.z + dir.z * 0.5,
                5, 0.1, 0.1, 0.1, 0.05);
        serverLevel.sendParticles(ParticleTypes.SMOKE,
                muzzlePos.x + dir.x * 0.5, muzzlePos.y + dir.y * 0.5, muzzlePos.z + dir.z * 0.5,
                3, 0.05, 0.05, 0.05, 0.02);

        double dist = muzzlePos.distanceTo(targetPos);
        for (double d = 0.5; d < dist; d += 0.8) {
            Vec3 p = muzzlePos.add(dir.scale(d));
            serverLevel.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.01);
        }

        target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 7.0F);
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
