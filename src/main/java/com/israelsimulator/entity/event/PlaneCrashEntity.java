package com.israelsimulator.entity.event;

import com.israelsimulator.event.world.plane.TwinTowersPlaneEvent;
import com.israelsimulator.registry.ModSoundEvents;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Commercial airliner entity flying towards the target tower (GAME_DESIGN.md §41–44).
 * Spawns during the countdown so players can visibly see it approaching in the sky!
 * Detects 25% penetration into the tower facade before triggering TNT detonator in elevator shaft.
 */
public class PlaneCrashEntity extends Entity {

    public static final double TOTAL_PLANE_LENGTH = 42.0;
    public static final double PENETRATION_THRESHOLD = TOTAL_PLANE_LENGTH * 0.25; // ~10.5 blocks

    private static final EntityDataAccessor<Boolean> DATA_EMBEDDED =
            SynchedEntityData.defineId(PlaneCrashEntity.class, EntityDataSerializers.BOOLEAN);

    private Vec3 targetPos = Vec3.ZERO;
    private Vec3 flightVelocity = Vec3.ZERO;
    private BlockPos towerMin = BlockPos.ZERO;
    private BlockPos towerMax = BlockPos.ZERO;
    private BlockPos elevatorShaft = BlockPos.ZERO;

    private boolean hitDetected = false;
    private double distancePenetrated = 0.0;
    private int flightTicks = 0;
    private int embeddedTicks = 0;

    public PlaneCrashEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_EMBEDDED, false);
    }

    public void initFlight(Vec3 spawnPos, Vec3 targetPos, BlockPos towerMin, BlockPos towerMax, BlockPos elevatorShaft, double speed) {
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.xo = spawnPos.x;
        this.yo = spawnPos.y;
        this.zo = spawnPos.z;

        this.targetPos = targetPos;
        this.towerMin = towerMin;
        this.towerMax = towerMax;
        this.elevatorShaft = elevatorShaft;

        Vec3 diff = targetPos.subtract(spawnPos);
        double dist = diff.length();
        if (dist > 0.001) {
            this.flightVelocity = diff.scale(speed / dist);
        } else {
            this.flightVelocity = new Vec3(speed, 0, 0);
        }

        this.setDeltaMovement(this.flightVelocity);

        double horizDist = Math.sqrt(flightVelocity.x * flightVelocity.x + flightVelocity.z * flightVelocity.z);
        float yRot = (float) (Math.atan2(-flightVelocity.x, flightVelocity.z) * (180.0 / Math.PI));
        float xRot = (float) (Math.atan2(-flightVelocity.y, horizDist) * (180.0 / Math.PI));
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.yRotO = yRot;
        this.xRotO = xRot;
    }

    public boolean isEmbedded() {
        return this.entityData.get(DATA_EMBEDDED);
    }

    @Override
    public void tick() {
        super.tick();

        // Always apply flight velocity directly to avoid client/server desync
        if (flightVelocity.lengthSqr() > 0.0001) {
            this.setDeltaMovement(flightVelocity);
        }

        if (this.level().isClientSide()) {
            spawnClientParticles();
            // Client-side smooth movement
            this.setPos(this.getX() + this.getDeltaMovement().x,
                    this.getY() + this.getDeltaMovement().y,
                    this.getZ() + this.getDeltaMovement().z);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        flightTicks++;

        if (isEmbedded()) {
            embeddedTicks++;
            if (embeddedTicks > 40) {
                this.discard();
            }
            return;
        }

        // Sound feedback every 20 ticks
        if (flightTicks % 20 == 0) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSoundEvents.PLANE_JET.get(), SoundSource.NEUTRAL, 5.0F, 0.8F);
        }

        // Move entity
        this.move(MoverType.SELF, flightVelocity);

        // Calculate distance to target impact point
        Vec3 currentPos = this.position();
        Vec3 nosePos = getNosePosition();
        double distToTarget = currentPos.distanceTo(targetPos);

        // Check if nose is inside tower bounding box or reached target facade
        boolean noseInside = isInsideTower(nosePos) || distToTarget < 8.0;

        if (!hitDetected && noseInside) {
            hitDetected = true;
            serverLevel.playSound(null, nosePos.x, nosePos.y, nosePos.z,
                    SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 5.0F, 0.7F);
            serverLevel.playSound(null, nosePos.x, nosePos.y, nosePos.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 3.0F, 1.2F);
        }

        if (hitDetected) {
            distancePenetrated += flightVelocity.length();

            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    nosePos.x, nosePos.y, nosePos.z, 10, 0.5, 0.5, 0.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    nosePos.x, nosePos.y, nosePos.z, 6, 0.3, 0.3, 0.3, 0.02);

            // Reached 25% penetration!
            if (distancePenetrated >= PENETRATION_THRESHOLD) {
                onTwentyFivePercentPenetration(serverLevel);
                return;
            }
        }

        // Failsafe: if plane has flown past target or timeout (> 300 ticks = 15 seconds)
        if (flightTicks > 260) {
            onTwentyFivePercentPenetration(serverLevel);
        }
    }

    private Vec3 getNosePosition() {
        Vec3 dir = flightVelocity.lengthSqr() > 0.001 ? flightVelocity.normalize() : new Vec3(1, 0, 0);
        return this.position().add(dir.scale(TOTAL_PLANE_LENGTH * 0.5));
    }

    private boolean isInsideTower(Vec3 pos) {
        if (towerMin.equals(BlockPos.ZERO) && towerMax.equals(BlockPos.ZERO)) {
            return false;
        }
        return pos.x >= towerMin.getX() - 2 && pos.x <= towerMax.getX() + 2
                && pos.y >= towerMin.getY() - 2 && pos.y <= towerMax.getY() + 2
                && pos.z >= towerMin.getZ() - 2 && pos.z <= towerMax.getZ() + 2;
    }

    private void onTwentyFivePercentPenetration(ServerLevel serverLevel) {
        if (isEmbedded()) return;

        this.entityData.set(DATA_EMBEDDED, true);
        this.flightVelocity = Vec3.ZERO;
        this.setDeltaMovement(Vec3.ZERO);

        Vec3 nosePos = getNosePosition();

        // 1. Structural impact blast at the breach point
        serverLevel.explode(this, nosePos.x, nosePos.y, nosePos.z, 5.0F, Level.ExplosionInteraction.BLOCK);

        // 2. Embed wreckage blocks permanently into the tower
        placeWreckageBlocks(serverLevel);

        // 3. Trigger cascading TNT chain reaction inside the elevator shaft
        TwinTowersPlaneEvent.detonateElevatorShaft(serverLevel, elevatorShaft);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        return false;
    }

    private void placeWreckageBlocks(ServerLevel level) {
        List<PlaneModelData.BlockEntry> blocks = PlaneModelData.getBlocks();
        BlockPos center = this.blockPosition();
        int count = 0;
        for (PlaneModelData.BlockEntry entry : blocks) {
            if (entry.dx() <= 0 && count++ < 300) {
                BlockPos target = center.offset(entry.dx(), entry.dy(), entry.dz());
                BlockState current = level.getBlockState(target);
                if (current.isAir() || current.is(Blocks.GLASS) || current.is(Blocks.STAINED_GLASS.gray())
                        || current.is(Blocks.CONCRETE.lightGray())) {
                    level.setBlock(target, entry.state(), 3);
                }
            }
        }
    }

    private void spawnClientParticles() {
        Vec3 vel = this.getDeltaMovement();
        if (vel.lengthSqr() > 0.01) {
            Vec3 back = this.position().subtract(vel.normalize().scale(8.0));
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    back.x, back.y + 0.5, back.z, 0, 0.02, 0);
            this.level().addParticle(ParticleTypes.FLAME,
                    back.x, back.y, back.z, 0, 0, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}
}
