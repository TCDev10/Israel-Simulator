package com.israelsimulator.entity.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Razor-sharp flying document projectile thrown by Donald Trump Miniboss (GAME_DESIGN.md §41–44).
 * Deals paper cut slicing damage when hitting targets.
 * If it misses and strikes the terrain or expires, it drops as a collectible paper item on the ground,
 * exactly like arrows fired that miss their target.
 */
public class TrumpPaperEntity extends Projectile implements ItemSupplier {

    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 80;
    private boolean droppedItem = false;

    public TrumpPaperEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No extra synched data needed beyond Projectile defaults
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.PAPER);
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }
        if (entity == this.getOwner() || entity instanceof TrumpMinibossEntity || entity instanceof IceAgentEntity || entity instanceof BibiBossEntity) {
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }

        Vec3 mov = this.getDeltaMovement();
        this.setPos(this.getX() + mov.x, this.getY() + mov.y, this.getZ() + mov.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);

        // Fluttering paper particles
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.tickCount % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }

        lifeTicks++;
        if (lifeTicks >= MAX_LIFE_TICKS) {
            // Missed in mid-air: drop collectible paper as it gently flutters down
            dropCollectiblePaper();
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();

            DamageSource damageSource = this.damageSources().thrown(this, owner);
            target.hurtServer(serverLevel, damageSource, 10.0F);

            // Paper cut audio and critical hit particle burst
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHEEP_SHEAR, SoundSource.HOSTILE, 1.2F, 1.6F);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.0F, 1.4F);

            serverLevel.sendParticles(ParticleTypes.CRIT,
                    this.getX(), this.getY(), this.getZ(),
                    10, 0.2, 0.2, 0.2, 0.1);
            serverLevel.sendParticles(ParticleTypes.POOF,
                    this.getX(), this.getY(), this.getZ(),
                    4, 0.1, 0.1, 0.1, 0.05);
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // Missed target: hits the environment and drops as a collectible paper item!
        dropCollectiblePaper();
        this.discard();
    }

    /**
     * Drops the flying document onto the ground as a collectible ItemEntity (like missed arrows).
     */
    private void dropCollectiblePaper() {
        if (this.droppedItem || this.isRemoved()) {
            return;
        }
        this.droppedItem = true;

        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            ItemStack paperItem = new ItemStack(Items.PAPER);

            ItemEntity itemEntity = new ItemEntity(serverLevel, pos.x, pos.y + 0.1, pos.z, paperItem);
            itemEntity.setDefaultPickUpDelay();
            itemEntity.setDeltaMovement(
                    (this.random.nextDouble() - 0.5) * 0.05,
                    0.1,
                    (this.random.nextDouble() - 0.5) * 0.05
            );
            serverLevel.addFreshEntity(itemEntity);

            serverLevel.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9F, 1.4F);
            serverLevel.sendParticles(ParticleTypes.POOF,
                    pos.x, pos.y, pos.z,
                    3, 0.1, 0.1, 0.1, 0.02);
        }
    }
}
