package com.israelsimulator.entity.projectile;

import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Thrown frag grenade. Bounces off blocks, stops on entities and explodes when the fuse runs out.
 * By default the explosion hurts entities but does not break blocks (config {@code grenadeBreaksBlocks}).
 */
public class FragGrenadeEntity extends ThrowableItemProjectile {
    private int fuse;

    public FragGrenadeEntity(EntityType<? extends FragGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public FragGrenadeEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.FRAG_GRENADE.get(), owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FRAG_GRENADE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().isClientSide()) {
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.15, this.getZ(), 0, 0.02, 0);
            }
            return;
        }
        if (++fuse >= IsraelSimulatorConfig.grenadeFuseTicks()) {
            explode();
        }
    }

    private void explode() {
        Level.ExplosionInteraction interaction = IsraelSimulatorConfig.grenadeBreaksBlocks()
                ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
        this.level().explode(this, this.getX(), this.getY(0.0625), this.getZ(),
                (float) IsraelSimulatorConfig.grenadeExplosionPower(), false, interaction);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        Vec3 v = this.getDeltaMovement();
        Direction.Axis axis = hit.getDirection().getAxis();
        Vec3 bounced = switch (axis) {
            case X -> new Vec3(-v.x * 0.35, v.y * 0.6, v.z * 0.6);
            case Y -> new Vec3(v.x * 0.5, -v.y * 0.3, v.z * 0.5);
            case Z -> new Vec3(v.x * 0.6, v.y * 0.6, -v.z * 0.35);
        };
        if (bounced.lengthSqr() < 0.0025) {
            bounced = Vec3.ZERO;
        } else if (this.level() instanceof ServerLevel server && v.lengthSqr() > 0.04) {
            server.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 0.5F, 1.4F);
        }
        this.setDeltaMovement(bounced);
        Vec3 loc = hit.getLocation();
        Vec3 normal = hit.getDirection().getUnitVec3().scale(0.13);
        this.setPos(loc.add(normal));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Fuse", fuse);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        fuse = input.getIntOr("Fuse", 0);
    }
}
