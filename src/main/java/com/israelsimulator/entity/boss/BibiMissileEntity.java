package com.israelsimulator.entity.boss;

import com.israelsimulator.registry.ModItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Explosive rocket projectile used by the Bibi Boss (GAME_DESIGN.md §41–43).
 * Supports targeted homing with smooth pursuit tracking and aerial missile barrage bombardments
 * that inflict devastating damage to players, entities, and the surrounding terrain foliage.
 */
public class BibiMissileEntity extends Projectile implements ItemSupplier {

    private static final EntityDataAccessor<Boolean> DATA_HOMING =
            SynchedEntityData.defineId(BibiMissileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BARRAGE =
            SynchedEntityData.defineId(BibiMissileEntity.class, EntityDataSerializers.BOOLEAN);

    private LivingEntity target;
    private UUID targetUuid;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 200;

    public BibiMissileEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HOMING, false);
        builder.define(DATA_BARRAGE, false);
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
        if (target != null) {
            this.targetUuid = target.getUUID();
        }
    }

    public void setHoming(boolean homing) {
        this.entityData.set(DATA_HOMING, homing);
    }

    public boolean isHoming() {
        return this.entityData.get(DATA_HOMING);
    }

    public void setBarrageMode(boolean barrage) {
        this.entityData.set(DATA_BARRAGE, barrage);
    }

    public boolean isBarrageMode() {
        return this.entityData.get(DATA_BARRAGE);
    }

    @Override
    public ItemStack getItem() {
        if (ModItems.MISSILE != null) {
            return new ItemStack(ModItems.MISSILE.get());
        }
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }
        // Missiles do not damage their boss owner or fellow guards/missiles
        if (entity == this.getOwner() || entity instanceof BibiMissileEntity) {
            return false;
        }
        if (this.getOwner() instanceof BibiBossEntity && entity instanceof BibiGuardEntity) {
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        // Target homing calculation (server-authoritative)
        if (!this.level().isClientSide() && isHoming()) {
            if (this.target == null && this.targetUuid != null && this.level() instanceof ServerLevel serverLevel) {
                Entity e = serverLevel.getEntity(this.targetUuid);
                if (e instanceof LivingEntity living) {
                    this.target = living;
                }
            }

            if (this.target != null && this.target.isAlive()) {
                Vec3 targetCenter = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);
                Vec3 toTarget = targetCenter.subtract(this.position()).normalize();
                Vec3 currentVel = this.getDeltaMovement();
                double speed = Math.max(0.75, currentVel.length());

                // Smooth aerodynamic pursuit tracking (allows player evasive maneuvers)
                Vec3 steered = currentVel.normalize().scale(0.86)
                        .add(toTarget.scale(0.14))
                        .normalize()
                        .scale(Math.min(speed + 0.015, 1.25));
                this.setDeltaMovement(steered);

                // Proximity detonation check
                if (this.distanceToSqr(this.target) < 1.44) {
                    explode();
                    return;
                }
            }
        }

        // Projectile trajectory physics & collision detection
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }

        Vec3 mov = this.getDeltaMovement();
        this.setPos(this.getX() + mov.x, this.getY() + mov.y, this.getZ() + mov.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);

        // Rocket exhaust FX (both server broadcast and client rendering)
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.05, 0.05, 0.05, 0.01);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0.02, 0.02, 0.02, 0.01);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0.02, 0.02, 0.02, 0.01);

            if (this.tickCount % 5 == 0) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.HOSTILE, 0.6F, 1.6F);
            }
        } else {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    -mov.x * 0.2, -mov.y * 0.2, -mov.z * 0.2);
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    -mov.x * 0.1, -mov.y * 0.1, -mov.z * 0.1);
        }

        lifeTicks++;
        if (lifeTicks >= MAX_LIFE_TICKS) {
            explode();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        explode();
    }

    private void explode() {
        if (this.isRemoved()) return;

        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            boolean isBarrage = isBarrageMode();

            // Dramatic detonation visual & audio effects
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    pos.x, pos.y, pos.z, 5, 0.4, 0.4, 0.4, 0.1);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x, pos.y, pos.z, 16, 0.8, 0.8, 0.8, 0.08);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z, 20, 0.6, 0.6, 0.6, 0.15);
            serverLevel.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE,
                    2.2F, 0.9F + this.random.nextFloat() * 0.2F);

            // Blast radius and damage scaling
            double blastRadius = isBarrage ? 4.8 : 4.0;
            float maxDamage = isBarrage ? 16.0F : 20.0F;

            AABB blastArea = this.getBoundingBox().inflate(blastRadius);
            List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, blastArea,
                    e -> e != this.getOwner() && !(e instanceof BibiGuardEntity) && e.isAlive());

            for (LivingEntity victim : victims) {
                double dist = victim.position().distanceTo(pos);
                float damage = (float) Math.max(5.0, maxDamage * (1.0 - (dist / blastRadius)));
                victim.hurtServer(serverLevel, this.damageSources().explosion(this, this.getOwner()), damage);

                // High-velocity kinetic explosion shockwave
                Vec3 knockback = victim.position().subtract(pos).normalize().scale(1.3).add(0, 0.35, 0);
                victim.setDeltaMovement(victim.getDeltaMovement().add(knockback));
            }

            // Bombard and scorch the surrounding environment ("bombardando e uccidendo l'ambiente circostante")
            destroyEnvironmentAround(serverLevel, this.blockPosition(), isBarrage ? 3 : 2);
        }

        this.discard();
    }

    private void destroyEnvironmentAround(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius),
                center.offset(radius, 2, radius))) {
            BlockState state = level.getBlockState(pos);

            // Obliterate foliage, vines, leaves, tall grass, crops, and fragile environmental flora
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS)
                    || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                    || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.VINE)
                    || state.is(Blocks.DEAD_BUSH)) {
                level.destroyBlock(pos, false);
            } else if (state.is(Blocks.GRASS_BLOCK) && this.random.nextFloat() < 0.35F) {
                // Scorch fertile ground into coarse dirt / dirt impact cratering
                level.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(), 3);
            }
        }

        // Small fiery crater ignition on solid surface
        BlockPos surfacePos = center;
        if (level.getBlockState(surfacePos).isAir() && level.getBlockState(surfacePos.below()).isSolidRender()) {
            level.setBlock(surfacePos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("LifeTicks", this.lifeTicks);
        output.putBoolean("Homing", isHoming());
        output.putBoolean("Barrage", isBarrageMode());
        if (this.targetUuid != null) {
            output.putString("TargetUUID", this.targetUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifeTicks = input.getIntOr("LifeTicks", 0);
        setHoming(input.getBooleanOr("Homing", false));
        setBarrageMode(input.getBooleanOr("Barrage", false));
        String uuidStr = input.getStringOr("TargetUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.targetUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
