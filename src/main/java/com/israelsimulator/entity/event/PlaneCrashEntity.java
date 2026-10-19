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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Commercial airliner entity flying towards the target tower (GAME_DESIGN.md §41–44).
 * Spawns during the countdown and renders the exact blocks from plane1-e1160.schem!
 * Detects impact when hitting actual solid blocks of the tower structure, then penetrates
 * ~25% (10.5 blocks) before triggering the cascading TNT explosion in the elevator shaft.
 */
public class PlaneCrashEntity extends Entity {

    public static final double TOTAL_PLANE_LENGTH = 42.0;
    public static final double PENETRATION_THRESHOLD = TOTAL_PLANE_LENGTH * 0.25; // ~10.5 blocks
    public static final int MAX_FLIGHT_TICKS = 30 * 20; // 30 seconds (600 ticks) max flight duration before despawn

    private static final EntityDataAccessor<Boolean> DATA_EMBEDDED =
            SynchedEntityData.defineId(PlaneCrashEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_VEL_X =
            SynchedEntityData.defineId(PlaneCrashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_VEL_Y =
            SynchedEntityData.defineId(PlaneCrashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_VEL_Z =
            SynchedEntityData.defineId(PlaneCrashEntity.class, EntityDataSerializers.FLOAT);

    private Vec3 targetPos = Vec3.ZERO;
    private Vec3 flightVelocity = Vec3.ZERO;
    private BlockPos towerMin = BlockPos.ZERO;
    private BlockPos towerMax = BlockPos.ZERO;
    private BlockPos elevatorShaft = BlockPos.ZERO;

    private boolean hitDetected = false;
    private double distancePenetrated = 0.0;
    private int flightTicks = 0;
    private int embeddedTicks = 0;
    private Vec3 previousNosePos = Vec3.ZERO;
    private Vec3 blockImpactPos = Vec3.ZERO;

    public PlaneCrashEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_EMBEDDED, false);
        builder.define(DATA_VEL_X, 0.0F);
        builder.define(DATA_VEL_Y, 0.0F);
        builder.define(DATA_VEL_Z, 0.0F);
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
        this.entityData.set(DATA_VEL_X, (float) this.flightVelocity.x);
        this.entityData.set(DATA_VEL_Y, (float) this.flightVelocity.y);
        this.entityData.set(DATA_VEL_Z, (float) this.flightVelocity.z);

        double horizDist = Math.sqrt(flightVelocity.x * flightVelocity.x + flightVelocity.z * flightVelocity.z);
        float yRot = (float) (Math.atan2(-flightVelocity.x, flightVelocity.z) * (180.0 / Math.PI));
        float xRot = (float) (Math.atan2(-flightVelocity.y, horizDist) * (180.0 / Math.PI));
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.yRotO = yRot;
        this.xRotO = xRot;
        this.previousNosePos = getNosePosition();
    }

    public boolean isEmbedded() {
        return this.entityData.get(DATA_EMBEDDED);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            float vx = this.entityData.get(DATA_VEL_X);
            float vy = this.entityData.get(DATA_VEL_Y);
            float vz = this.entityData.get(DATA_VEL_Z);
            Vec3 vel = new Vec3(vx, vy, vz);
            this.setDeltaMovement(vel);

            if (vel.lengthSqr() > 0.0001) {
                double horizDist = Math.sqrt(vx * vx + vz * vz);
                float yRot = (float) (Math.atan2(-vx, vz) * (180.0 / Math.PI));
                float xRot = (float) (Math.atan2(-vy, horizDist) * (180.0 / Math.PI));
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
                this.setYRot(yRot);
                this.setXRot(xRot);
            }

            if (!isEmbedded()) {
                this.setPos(this.getX() + vx, this.getY() + vy, this.getZ() + vz);
                spawnClientParticles();
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        flightTicks++;

        if (isEmbedded()) {
            embeddedTicks++;
            if (embeddedTicks > 60) {
                this.discard();
            }
            return;
        }

        // Sound feedback
        if (flightTicks % 20 == 0) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSoundEvents.PLANE_JET.get(), SoundSource.NEUTRAL, 5.0F, 0.8F);
        }

        // Move entity directly along velocity vector
        this.setPos(this.getX() + flightVelocity.x, this.getY() + flightVelocity.y, this.getZ() + flightVelocity.z);
        this.setDeltaMovement(flightVelocity);

        Vec3 nosePos = getNosePosition();

        // Check if ANY part of the plane (nose, wings, engines, cockpit, belly) touches an ACTUAL solid block
        Vec3 touchedPoint = checkAnyPlanePartTouchesSolidBlock(serverLevel);

        if (!hitDetected && touchedPoint != null) {
            hitDetected = true;
            this.blockImpactPos = touchedPoint;
            serverLevel.playSound(null, touchedPoint.x, touchedPoint.y, touchedPoint.z,
                    SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 6.0F, 0.7F);
            serverLevel.playSound(null, touchedPoint.x, touchedPoint.y, touchedPoint.z,
                    SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 6.0F, 0.6F);
        }

        if (hitDetected) {
            distancePenetrated += flightVelocity.length();

            Vec3 fxPos = blockImpactPos.lengthSqr() > 0.001 ? blockImpactPos : nosePos;
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    fxPos.x, fxPos.y, fxPos.z, 25, 1.0, 1.0, 1.0, 0.08);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    fxPos.x, fxPos.y, fxPos.z, 15, 0.6, 0.6, 0.6, 0.05);

            // Penetrate into the building facade, then detonate the massive explosion!
            if (distancePenetrated >= 5.0) {
                detonatePlaneCrash(serverLevel);
                return;
            }
        }

        // Timeout: if plane doesn't hit any solid block after 30 seconds (600 ticks), despawn cleanly
        if (flightTicks >= MAX_FLIGHT_TICKS) {
            TwinTowersPlaneEvent.onPlaneGone(this.getUUID());
            this.discard();
        }
    }

    private Vec3 checkAnyPlanePartTouchesSolidBlock(ServerLevel level) {
        List<Vec3> points = getPlaneCollisionPoints();
        for (Vec3 pt : points) {
            Vec3 prev = pt.subtract(flightVelocity);
            if (checkPointTouchesSolidBlock(level, prev, pt)) {
                return pt;
            }
        }
        return null;
    }

    private List<Vec3> getPlaneCollisionPoints() {
        Vec3 forward = flightVelocity.lengthSqr() > 0.001 ? flightVelocity.normalize() : new Vec3(1, 0, 0);
        double horizLen = Math.sqrt(forward.x * forward.x + forward.z * forward.z);
        Vec3 perp = horizLen > 0.001
                ? new Vec3(-forward.z / horizLen, 0, forward.x / horizLen)
                : new Vec3(0, 0, 1);
        Vec3 pos = this.position();

        return List.of(
                // 1. Nose tip (front)
                pos.add(forward.scale(21.0)),
                // 2. Cockpit (upper front)
                pos.add(forward.scale(15.0)).add(0, 1.5, 0),
                // 3. Left wingtip (far left)
                pos.add(perp.scale(18.0)).add(forward.scale(-2.0)),
                // 4. Right wingtip (far right)
                pos.add(perp.scale(-18.0)).add(forward.scale(-2.0)),
                // 5. Left wing mid
                pos.add(perp.scale(9.0)).add(forward.scale(2.0)),
                // 6. Right wing mid
                pos.add(perp.scale(-9.0)).add(forward.scale(2.0)),
                // 7. Left engine (under left wing)
                pos.add(perp.scale(6.0)).add(forward.scale(3.0)).add(0, -2.0, 0),
                // 8. Right engine (under right wing)
                pos.add(perp.scale(-6.0)).add(forward.scale(3.0)).add(0, -2.0, 0),
                // 9. Fuselage belly / center
                pos.add(0, -1.0, 0)
        );
    }

    private boolean checkPointTouchesSolidBlock(ServerLevel level, Vec3 from, Vec3 to) {
        // 1. Raycast line from previous tick pos to current pos
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockState s = level.getBlockState(hit.getBlockPos());
            if (!s.isAir() && !s.canBeReplaced() && s.blocksMotion()) {
                return true;
            }
        }

        // 2. Direct 3x3x3 block check around current position
        BlockPos center = BlockPos.containing(to);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && !s.canBeReplaced() && s.blocksMotion()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Vec3 getNosePosition() {
        Vec3 dir = flightVelocity.lengthSqr() > 0.001 ? flightVelocity.normalize() : new Vec3(1, 0, 0);
        // Distance from entity center to nose front in schematic is 21 blocks
        return this.position().add(dir.scale(21.0));
    }

    private void detonatePlaneCrash(ServerLevel serverLevel) {
        if (isEmbedded()) return;

        this.entityData.set(DATA_EMBEDDED, true);
        this.flightVelocity = Vec3.ZERO;
        this.setDeltaMovement(Vec3.ZERO);
        this.entityData.set(DATA_VEL_X, 0.0F);
        this.entityData.set(DATA_VEL_Y, 0.0F);
        this.entityData.set(DATA_VEL_Z, 0.0F);

        Vec3 nosePos = getNosePosition();
        Vec3 wallImpact = blockImpactPos.lengthSqr() > 0.001 ? blockImpactPos : nosePos;
        Vec3 midPos = wallImpact.add(nosePos).scale(0.5);
        Vec3 dir = flightVelocity.lengthSqr() > 0.001 ? flightVelocity.normalize() : new Vec3(1, 0, 0);
        Vec3 deepCore = nosePos.add(dir.scale(10.0));

        // 1. Massive 10x multi-point fuel-air airliner explosion tearing through the facade and interior
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y, wallImpact.z, 28.0F);
        explodeInTower(serverLevel, nosePos.x, nosePos.y, nosePos.z, 26.0F);
        explodeInTower(serverLevel, midPos.x, midPos.y, midPos.z, 25.0F);
        explodeInTower(serverLevel, deepCore.x, deepCore.y, deepCore.z, 25.0F);

        // 2. Wing-span fuel tank explosions tearing across the entire facade
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y, wallImpact.z + 7.0, 22.0F);
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y, wallImpact.z - 7.0, 22.0F);
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y, wallImpact.z + 14.0, 20.0F);
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y, wallImpact.z - 14.0, 20.0F);

        // 3. Multi-floor vertical rupture (obliterating 8-10 floors above and below)
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y + 8.0, wallImpact.z, 22.0F);
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y - 7.0, wallImpact.z, 22.0F);
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y + 16.0, wallImpact.z, 20.0F);
        explodeInTower(serverLevel, wallImpact.x, wallImpact.y - 14.0, wallImpact.z, 18.0F);

        // 4. Catastrophic visual effects (massive mushroom cloud, firestorm and lava embers)
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, wallImpact.x, wallImpact.y, wallImpact.z, 15, 6.0, 6.0, 6.0, 0.0);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, nosePos.x, nosePos.y, nosePos.z, 10, 5.0, 5.0, 5.0, 0.0);
        serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, wallImpact.x, wallImpact.y + 5.0, wallImpact.z, 150, 8.0, 10.0, 8.0, 0.15);
        serverLevel.sendParticles(ParticleTypes.FLAME, wallImpact.x, wallImpact.y, wallImpact.z, 120, 10.0, 8.0, 10.0, 0.35);
        serverLevel.sendParticles(ParticleTypes.LAVA, wallImpact.x, wallImpact.y, wallImpact.z, 60, 6.0, 6.0, 6.0, 0.2);

        // 5. Embed burning wreckage blocks into the breached tower structure
        placeWreckageBlocks(serverLevel);

        // 6. Broad fire engulfment across breached floors and facade
        BlockPos centerImpact = BlockPos.containing(wallImpact);
        for (int ox = -10; ox <= 10; ox++) {
            for (int oy = -6; oy <= 12; oy++) {
                for (int oz = -10; oz <= 10; oz++) {
                    if (serverLevel.getRandom().nextFloat() < 0.35F) {
                        BlockPos firePos = centerImpact.offset(ox, oy, oz);
                        if (isInsideTower(firePos) && serverLevel.getBlockState(firePos).isAir() && !serverLevel.getBlockState(firePos.below()).isAir()) {
                            serverLevel.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // 7. Thunderous 4-layer audio blast
        serverLevel.playSound(null, wallImpact.x, wallImpact.y, wallImpact.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 20.0F, 0.4F);
        serverLevel.playSound(null, wallImpact.x, wallImpact.y, wallImpact.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 20.0F, 0.65F);
        serverLevel.playSound(null, wallImpact.x, wallImpact.y, wallImpact.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 20.0F, 0.85F);
        serverLevel.playSound(null, wallImpact.x, wallImpact.y, wallImpact.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 20.0F, 1.1F);

        TwinTowersPlaneEvent.onPlaneCrashImpact(serverLevel, nosePos, this.getUUID());
    }

    /** True when {@code pos} lies inside the targeted tower; nothing outside it may be broken or set on fire. */
    public boolean isInsideTower(BlockPos pos) {
        if (towerMin.equals(towerMax)) {
            return false; // no tower known (e.g. plane reloaded from disk): never touch blocks
        }
        return pos.getX() >= Math.min(towerMin.getX(), towerMax.getX()) && pos.getX() <= Math.max(towerMin.getX(), towerMax.getX())
                && pos.getY() >= Math.min(towerMin.getY(), towerMax.getY()) && pos.getY() <= Math.max(towerMin.getY(), towerMax.getY())
                && pos.getZ() >= Math.min(towerMin.getZ(), towerMax.getZ()) && pos.getZ() <= Math.max(towerMin.getZ(), towerMax.getZ());
    }

    /** Explosion that damages entities normally but only breaks (and ignites) blocks inside the targeted tower. */
    private void explodeInTower(ServerLevel serverLevel, double x, double y, double z, float radius) {
        ExplosionDamageCalculator towerOnly = new ExplosionDamageCalculator() {
            @Override
            public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
                return isInsideTower(pos);
            }
        };
        serverLevel.explode(this, null, towerOnly, x, y, z, radius, true, Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public boolean shouldBeSaved() {
        return false; // a plane reloaded from disk would have lost its flight path
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
            if (count++ < 500) {
                BlockPos target = center.offset(entry.dx(), entry.dy(), entry.dz());
                if (!isInsideTower(target)) {
                    continue;
                }
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
