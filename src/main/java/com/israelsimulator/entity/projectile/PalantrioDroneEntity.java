package com.israelsimulator.entity.projectile;

import com.israelsimulator.config.IsraelSimulatorConfig;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Palantrio Drone: a small kamikaze quadcopter launched by its owner. It seeks the nearest hostile mob
 * within {@value #SEEK_RANGE} blocks (never its owner or tamed/owned animals), flies to it and explodes
 * on contact. Without a target it self-destructs harmlessly after {@value #LIFETIME_TICKS} ticks.
 */
public class PalantrioDroneEntity extends Mob {
    public static final double SEEK_RANGE = 32.0;
    public static final int LIFETIME_TICKS = 400;
    public static final double SPEED = 0.55;

    private UUID owner;
    private LivingEntity target;
    private int life;

    public PalantrioDroneEntity(EntityType<? extends PalantrioDroneEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.FLYING_SPEED, SPEED);
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner.getUUID();
    }

    /** Whether {@code e} is a valid drone target for a drone owned by {@code ownerId}. */
    public static boolean isValidTarget(Entity e, UUID ownerId) {
        if (!(e instanceof LivingEntity living) || !living.isAlive() || !(e instanceof Enemy)) {
            return false;
        }
        if (ownerId != null && ownerId.equals(e.getUUID())) {
            return false;
        }
        if (e instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {
            return false;
        }
        return !(e instanceof PalantrioDroneEntity);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (this.level().isClientSide()) {
            if (this.tickCount % 3 == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() - 0.1, this.getZ(), 0, -0.02, 0);
            }
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        if (++life > LIFETIME_TICKS) {
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.6F, 1.6F);
            this.discard();
            return;
        }
        if (target == null || !target.isAlive() || target.distanceToSqr(this) > SEEK_RANGE * SEEK_RANGE * 1.5 || this.tickCount % 20 == 0) {
            target = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SEEK_RANGE),
                            e -> isValidTarget(e, owner) && e.distanceToSqr(this) <= SEEK_RANGE * SEEK_RANGE)
                    .stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(this))).orElse(null);
        }
        Vec3 velocity;
        if (target != null) {
            Vec3 aim = target.position().add(0, target.getBbHeight() * 0.6, 0).subtract(this.position());
            velocity = aim.normalize().scale(SPEED);
            if (this.getBoundingBox().inflate(0.4).intersects(target.getBoundingBox())) {
                explode(level);
                return;
            }
        } else {
            // hover, bobbing slightly
            velocity = new Vec3(0, Math.sin(this.tickCount * 0.15) * 0.03, 0);
        }
        this.setDeltaMovement(velocity);
        if (velocity.horizontalDistanceSqr() > 1.0E-4) {
            this.setYRot((float) (Math.atan2(-velocity.x, velocity.z) * (180.0 / Math.PI)));
            this.yBodyRot = this.getYRot();
        }
        if (this.tickCount % 8 == 0) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEE_LOOP, SoundSource.NEUTRAL, 0.5F, 1.8F);
        }
    }

    private void explode(ServerLevel level) {
        Level.ExplosionInteraction interaction = IsraelSimulatorConfig.droneBreaksBlocks()
                ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
        level.explode(this, this.getX(), this.getY(), this.getZ(),
                (float) IsraelSimulatorConfig.droneExplosionPower(), false, interaction);
        this.discard();
    }

    @Override
    public void travel(Vec3 input) {
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Life", life);
        if (owner != null) {
            output.putString("Owner", owner.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        life = input.getIntOr("Life", 0);
        input.getString("Owner").ifPresent(s -> {
            try {
                owner = UUID.fromString(s);
            } catch (IllegalArgumentException ignored) {
                owner = null;
            }
        });
    }
}
