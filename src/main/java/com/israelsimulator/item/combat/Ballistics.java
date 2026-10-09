package com.israelsimulator.item.combat;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Hitscan bullets, the same model as the Security Pistol: an instant ray from the shooter's eyes.
 * Unlike the pistol's ray, these bullets stop at the first solid block.
 */
public final class Ballistics {
    private Ballistics() {}

    /** Random direction inside a cone of {@code spreadDegrees} around {@code dir}. */
    public static Vec3 applySpread(Vec3 dir, float spreadDegrees, RandomSource random) {
        if (spreadDegrees <= 0.0F) {
            return dir.normalize();
        }
        double s = Math.tan(Math.toRadians(spreadDegrees));
        Vec3 jitter = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(s * 0.5);
        return dir.normalize().add(jitter).normalize();
    }

    /**
     * Fires one bullet and returns the entity hit, or null.
     */
    public static LivingEntity fire(ServerLevel level, LivingEntity shooter, Vec3 dir, double range, float damage) {
        Vec3 eye = shooter.getEyePosition();
        Vec3 end = eye.add(dir.scale(range));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB traceBox = new AABB(eye, end).inflate(1.0);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, traceBox,
                e -> e != shooter && e.isAlive() && !e.isSpectator() && !shooter.isPassengerOfSameVehicle(e));
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        Vec3 hitPos = end;
        for (LivingEntity e : candidates) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(0.2).clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceToSqr(hit.get());
                if (d < best) {
                    best = d;
                    target = e;
                    hitPos = hit.get();
                }
            }
        }

        double length = eye.distanceTo(hitPos);
        int max = Math.min(48, (int) (length / 1.5));
        for (int i = 1; i <= max; i++) {
            Vec3 p = eye.add(dir.scale(i * 1.5));
            level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        Vec3 muzzle = eye.add(dir.scale(0.6));
        level.sendParticles(ParticleTypes.SMALL_FLAME, muzzle.x, muzzle.y, muzzle.z, 2, 0.02, 0.02, 0.02, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 2, 0.04, 0.04, 0.04, 0.01);

        if (target != null) {
            DamageSource source = shooter instanceof Player p
                    ? level.damageSources().playerAttack(p)
                    : level.damageSources().mobAttack(shooter);
            // Bullets are discrete hits: do not let the 0.5 s hurt cooldown swallow automatic fire.
            target.invulnerableTime = 0;
            target.hurtServer(level, source, damage);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, hitPos.x, hitPos.y, hitPos.z, 2, 0.1, 0.1, 0.1, 0.05);
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            level.sendParticles(ParticleTypes.SMOKE, end.x, end.y, end.z, 3, 0.05, 0.05, 0.05, 0.01);
        }
        return target;
    }

    /** Direction a mob should shoot at {@code target}: towards the centre of its body. */
    public static Vec3 aimAt(LivingEntity shooter, LivingEntity target) {
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.55, 0).subtract(shooter.getEyePosition());
        return to.lengthSqr() < 1.0E-6 ? shooter.getViewVector(1.0F) : to.normalize();
    }
}
