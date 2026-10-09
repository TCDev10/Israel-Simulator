package com.israelsimulator.entity.projectile;

import com.israelsimulator.entity.boss.BibiBossEntity;
import com.israelsimulator.entity.boss.BibiPoisonBreathRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Bibi's late-fight attack: a dragon-fireball-like orb dropped from above. On impact it leaves a
 * dragon-breath-style cloud that applies Poison II instead of instant damage. It never breaks blocks.
 */
public class BibiPoisonBreathEntity extends DragonFireball {
    private static final int MAX_LIFETIME_TICKS = 200;

    public BibiPoisonBreathEntity(EntityType<? extends BibiPoisonBreathEntity> type, Level level) {
        super(type, level);
    }

    /** Builds the cloud this projectile leaves on impact (also used by tests via the rules constants). */
    public static AreaEffectCloud createPoisonCloud(Level level, double x, double y, double z, LivingEntity owner) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, x, y, z);
        if (owner != null) {
            cloud.setOwner(owner);
        }
        cloud.setCustomParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F));
        cloud.setRadius(BibiPoisonBreathRules.CLOUD_RADIUS);
        cloud.setDuration(BibiPoisonBreathRules.CLOUD_DURATION_TICKS);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(0.0F);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON,
                BibiPoisonBreathRules.POISON_DURATION_TICKS, BibiPoisonBreathRules.POISON_AMPLIFIER));
        return cloud;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !BibiBossEntity.isAlly(entity);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        // Intentionally no block interaction: the attack must never break or alter blocks.
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) hitResult);
        }
        if (this.level() instanceof ServerLevel serverLevel && !this.isRemoved()) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
            AreaEffectCloud cloud = createPoisonCloud(serverLevel, this.getX(), this.getY(), this.getZ(), owner);
            serverLevel.addFreshEntity(cloud);
            serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.3, this.getZ(),
                    40, 1.5, 0.4, 1.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.SNEEZE, this.getX(), this.getY() + 0.3, this.getZ(),
                    30, 1.5, 0.3, 1.5, 0.02);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.2F, 1.1F);
            this.discard();
        }
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SNEEZE;
    }
}
