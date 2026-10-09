package com.israelsimulator.entity.mossad;

import com.israelsimulator.item.combat.Ballistics;
import com.israelsimulator.item.combat.FirearmItem;
import com.israelsimulator.item.combat.FirearmStats;
import com.israelsimulator.mossad.MossadData;
import com.israelsimulator.mossad.MossadRules;
import com.israelsimulator.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Hostile Mossad Agent (fictional): a suited operative armed with the Uzi or the Security Pistol.
 * While stalking a target further than {@value #REVEAL_DISTANCE} blocks away it is invisible and
 * crouched; it reveals itself when it gets close, fires, or is hurt. Players with high Mossad
 * reputation are left alone unless they attack first.
 */
public class MossadAgentEntity extends Monster implements RangedAttackMob {
    public static final double REVEAL_DISTANCE = 10.0;
    private int revealTicks;

    public MossadAgentEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, 20, 35, 16.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false,
                (target, level) -> !MossadRules.isTrusted(MossadData.get(level.getServer()).reputation(target.getUUID()))));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        boolean smg = level.getRandom().nextFloat() < 0.6F;
        ItemStack weapon = new ItemStack(smg ? ModItems.SMG.get() : ModItems.PISTOL.get());
        if (smg) {
            FirearmItem.setLoadedRounds(weapon, FirearmStats.SMG.magazineSize());
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.04F);
        return result;
    }

    public boolean hasSmg() {
        return this.getMainHandItem().is(ModItems.SMG.get());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        LivingEntity target = this.getTarget();
        this.setAggressive(target != null && target.isAlive());
        if (revealTicks > 0) {
            revealTicks--;
        }
        boolean stalking = target != null && target.isAlive() && revealTicks == 0
                && this.distanceTo(target) > REVEAL_DISTANCE;
        this.setInvisible(stalking);
        this.setShiftKeyDown(stalking);
        this.setPose(stalking ? Pose.CROUCHING : Pose.STANDING);
    }

    /** Ticks the agent stays visible; used by tests and missions. */
    public void reveal(int ticks) {
        revealTicks = Math.max(revealTicks, ticks);
        this.setInvisible(false);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        reveal(100);
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        reveal(60);
        boolean smg = hasSmg();
        FirearmStats sound = smg ? FirearmStats.SMG : FirearmStats.ASSAULT_RIFLE;
        int shots = smg ? 2 : 1;
        float damage = smg ? 2.5F : 5.0F;
        float spread = smg ? 5.0F : 2.5F;
        for (int i = 0; i < shots; i++) {
            Vec3 dir = Ballistics.applySpread(Ballistics.aimAt(this, target), spread, this.getRandom());
            Ballistics.fire(level, this, dir, 24.0, damage);
        }
        FirearmItem.playShot(level, this, sound);
        if (!smg) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.HOSTILE, 0.4F, 1.9F);
        }
    }
}
