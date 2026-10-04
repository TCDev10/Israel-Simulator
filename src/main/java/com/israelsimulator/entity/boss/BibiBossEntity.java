package com.israelsimulator.entity.boss;

import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModItems;
import com.israelsimulator.registry.ModSoundEvents;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Fictional/satirical endgame boss inspired by Benjamin Netanyahu (GAME_DESIGN.md §41–44, TODO §43–45).
 * Features a multi-phase state machine (IDLE, ALERT, COMBAT, ENRAGED, DEFEATED), configurable 10,000+ HP,
 * dynamic boss bar, guard summoning, filibuster shockwaves, and the legendary Hava Nagila disc drop.
 */
public class BibiBossEntity extends Monster implements RangedAttackMob {

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(BibiBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHIELD_ACTIVE =
            SynchedEntityData.defineId(BibiBossEntity.class, EntityDataSerializers.BOOLEAN);

    public static final float ENRAGE_HEALTH_FRACTION = 0.30F; // Below 30% HP = ENRAGED
    public static final float MAX_SINGLE_HIT_DAMAGE = 500.0F; // Anti-one-shot exploit cap

    private final ServerBossEvent bossEvent;
    private BibiBossState bossState = BibiBossState.IDLE;
    private BlockPos arenaCenter;
    private boolean rewardDropped = false;

    private int guardSummonCooldown = 100;
    private int specialAttackCooldown = 120;
    private int speechCooldown = 160;

    private final Set<UUID> participatingPlayerUuids = new HashSet<>();
    private final List<UUID> aliveGuardUuids = new ArrayList<>();

    public BibiBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.bossEvent = new ServerBossEvent(
                this.getUUID(),
                Component.translatable("entity.israel_simulator.bibi_boss").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD),
                BossEvent.BossBarColor.BLUE,
                BossEvent.BossBarOverlay.NOTCHED_10
        );
        this.bossEvent.setDarkenScreen(true);
        this.xpReward = 1500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10000.0) // 10,000 HP default (GAME_DESIGN §41.1)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, BibiBossState.IDLE.ordinal());
        builder.define(DATA_SHIELD_ACTIVE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.15, 40, 70, 24.0F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public BibiBossState getBossState() {
        return this.bossState;
    }

    public void setBossState(BibiBossState newState) {
        this.bossState = newState;
        this.entityData.set(DATA_STATE, newState.ordinal());

        if (newState == BibiBossState.ENRAGED) {
            this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            this.bossEvent.setName(Component.translatable("entity.israel_simulator.bibi_boss.enraged")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            if (!this.level().isClientSide() && ModSoundEvents.BIBI_ENRAGE != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSoundEvents.BIBI_ENRAGE.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
            }
        }
    }

    public boolean isShieldActive() {
        return this.entityData.get(DATA_SHIELD_ACTIVE);
    }

    public void setShieldActive(boolean active) {
        this.entityData.set(DATA_SHIELD_ACTIVE, active);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.arenaCenter == null) {
            this.arenaCenter = this.blockPosition();
        }

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            // Update boss bar progress
            float progress = Math.max(0.0F, Math.min(1.0F, this.getHealth() / this.getMaxHealth()));
            this.bossEvent.setProgress(progress);

            // Arena boundary leash enforcement
            int arenaRadius = IsraelSimulatorConfig.bibiBossArenaRadius();
            if (this.distanceToSqr(Vec3.atCenterOf(this.arenaCenter)) > (arenaRadius * arenaRadius)) {
                this.teleportTo(this.arenaCenter.getX(), this.arenaCenter.getY(), this.arenaCenter.getZ());
                this.setDeltaMovement(Vec3.ZERO);
                serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(),
                        30, 0.5, 1.0, 0.5, 0.2);
            }

            // State Machine evaluation
            if (this.bossState != BibiBossState.DEFEATED) {
                if (progress <= ENRAGE_HEALTH_FRACTION && this.bossState != BibiBossState.ENRAGED) {
                    setBossState(BibiBossState.ENRAGED);
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.2, this.getZ(),
                            50, 0.8, 0.8, 0.8, 0.15);
                } else if (this.getTarget() != null && this.bossState == BibiBossState.IDLE) {
                    setBossState(BibiBossState.COMBAT);
                }
            }

            // Combat timers and special attacks
            if (this.bossState.isAggressive()) {
                handleCombatRoutines(serverLevel);
            }

            // Visual effects for ENRAGED phase
            if (this.bossState == BibiBossState.ENRAGED && this.tickCount % 5 == 0) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5),
                        this.getY() + this.random.nextDouble() * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5),
                        2, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    private void handleCombatRoutines(ServerLevel serverLevel) {
        // Cooldown decrements
        if (guardSummonCooldown > 0) guardSummonCooldown--;
        if (specialAttackCooldown > 0) specialAttackCooldown--;
        if (speechCooldown > 0) speechCooldown--;

        // 1. "Coalition Call" — summon guards up to limit
        int maxGuards = IsraelSimulatorConfig.bibiBossMaxGuards();
        cleanActiveGuards(serverLevel);
        if (guardSummonCooldown <= 0 && this.aliveGuardUuids.size() < maxGuards) {
            summonCoalitionGuards(serverLevel, Math.min(2, maxGuards - this.aliveGuardUuids.size()));
            guardSummonCooldown = this.bossState == BibiBossState.ENRAGED ? 200 : 350;
        }

        // 2. "Filibuster Shockwave" — area knockback and debuff
        if (specialAttackCooldown <= 0) {
            performFilibusterShockwave(serverLevel);
            specialAttackCooldown = this.bossState == BibiBossState.ENRAGED ? 120 : 200;
        }

        // 3. Satirical speech quote broadcast
        if (speechCooldown <= 0) {
            broadcastSpeechCue(serverLevel);
            speechCooldown = 280;
        }
    }

    private void cleanActiveGuards(ServerLevel serverLevel) {
        this.aliveGuardUuids.removeIf(uuid -> {
            net.minecraft.world.entity.Entity entity = serverLevel.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });
    }

    private void summonCoalitionGuards(ServerLevel serverLevel, int count) {
        if (ModEntities.BIBI_GUARD == null) return;

        for (int i = 0; i < count; i++) {
            BibiGuardEntity guard = ModEntities.BIBI_GUARD.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (guard != null) {
                double offsetX = (this.random.nextDouble() - 0.5) * 6.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 6.0;
                guard.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                guard.setBossUuid(this.getUUID());
                if (this.getTarget() != null) {
                    guard.setTarget(this.getTarget());
                }
                serverLevel.addFreshEntity(guard);
                this.aliveGuardUuids.add(guard.getUUID());

                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        guard.getX(), guard.getY() + 0.5, guard.getZ(),
                        15, 0.3, 0.3, 0.3, 0.05);
            }
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 1.5F, 0.8F);
    }

    private void performFilibusterShockwave(ServerLevel serverLevel) {
        double radius = 10.0;
        AABB aabb = this.getBoundingBox().inflate(radius);
        List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(Player.class, aabb);

        serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                this.getX(), this.getY() + 1.2, this.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2F, 1.2F);

        for (Player p : nearbyPlayers) {
            Vec3 knockback = p.position().subtract(this.position()).normalize().scale(1.5).add(0, 0.4, 0);
            p.setDeltaMovement(knockback);
            p.hurtServer(serverLevel, this.damageSources().mobAttack(this), 12.0F);
            p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        }
    }

    private void broadcastSpeechCue(ServerLevel serverLevel) {
        if (ModSoundEvents.BIBI_SPEECH != null) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSoundEvents.BIBI_SPEECH.get(), SoundSource.HOSTILE, 1.5F, 1.0F);
        }
        int quoteIndex = this.random.nextInt(3);
        Component quote = Component.translatable("speech.israel_simulator.bibi_boss.quote_" + quoteIndex)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);

        AABB aabb = this.getBoundingBox().inflate(32.0);
        for (Player p : serverLevel.getEntitiesOfClass(Player.class, aabb)) {
            p.sendSystemMessage(quote);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 from = this.getEyePosition();
        Vec3 to = target.getEyePosition();
        Vec3 dir = to.subtract(from).normalize();

        // Beam shockwave attack
        for (double d = 1.0; d < from.distanceTo(to); d += 1.0) {
            Vec3 p = from.add(dir.scale(d));
            serverLevel.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.02);
        }

        target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 14.0F);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0));

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 1.2F);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        // Track player damage for multiplayer reward distribution
        if (damageSource.getEntity() instanceof Player player) {
            this.participatingPlayerUuids.add(player.getUUID());
        }

        // Engage combat on hit
        if (this.bossState == BibiBossState.IDLE || this.bossState == BibiBossState.ALERT) {
            setBossState(BibiBossState.COMBAT);
        }

        // Damage cap per single hit to strictly avoid 1-shot cheese exploits
        float clampedDamage = Math.min(amount, MAX_SINGLE_HIT_DAMAGE);

        // Diplomatic immunity shield: reduces damage by 50%
        if (isShieldActive()) {
            clampedDamage *= 0.5F;
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 1.0, this.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1);
        }

        // Brief shield activation chance on receiving heavy burst damage (> 100)
        if (clampedDamage > 100.0F && !isShieldActive() && this.random.nextFloat() < 0.4F) {
            setShieldActive(true);
        }

        return super.hurtServer(serverLevel, damageSource, clampedDamage);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        setBossState(BibiBossState.DEFEATED);
        this.bossEvent.removeAllPlayers();

        if (this.level() instanceof ServerLevel serverLevel) {
            // Dismiss active guards
            for (UUID uuid : this.aliveGuardUuids) {
                net.minecraft.world.entity.Entity guard = serverLevel.getEntity(uuid);
                if (guard != null && guard.isAlive()) {
                    guard.discard();
                }
            }

            // Distribute boss rewards once (GAME_DESIGN §44, TODO §45)
            if (!this.rewardDropped) {
                this.rewardDropped = true;
                dropBossRewards(serverLevel);
            }
        }
    }

    private void dropBossRewards(ServerLevel serverLevel) {
        // Guaranteed Hava Nagila music disc (LEGENDARY)
        if (ModItems.HAVA_NAGILA_DISC != null) {
            ItemStack disc = new ItemStack(ModItems.HAVA_NAGILA_DISC.get());
            ItemEntity entity = new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(), disc);
            entity.setGlowingTag(true);
            serverLevel.addFreshEntity(entity);
        }

        // Shekels reward (32-64 currency)
        if (ModItems.SHEKEL != null) {
            ItemStack shekels = new ItemStack(ModItems.SHEKEL.get(), 48);
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(), shekels));
        }

        // Ancient Coins (4-8 collectibles)
        if (ModItems.ANCIENT_COIN != null) {
            ItemStack coins = new ItemStack(ModItems.ANCIENT_COIN.get(), 6);
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(), coins));
        }

        // Diamonds bonus
        serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(),
                new ItemStack(Items.DIAMOND, 5)));

        // Broadcast victory message to participating players
        Component victoryMsg = Component.translatable("message.israel_simulator.bibi_boss_defeated")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        for (UUID playerUuid : this.participatingPlayerUuids) {
            Player p = serverLevel.getPlayerByUUID(playerUuid);
            if (p != null) {
                p.sendSystemMessage(victoryMsg);
                serverLevel.playSound(null, p.getX(), p.getY(), p.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public Set<UUID> getParticipatingPlayerUuids() {
        return Set.copyOf(this.participatingPlayerUuids);
    }

    public List<UUID> getActiveGuardUuids() {
        return List.copyOf(this.aliveGuardUuids);
    }

    public boolean isRewardDropped() {
        return this.rewardDropped;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.BIBI_AMBIENT != null ? ModSoundEvents.BIBI_AMBIENT.get() : SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSoundEvents.BIBI_HURT != null ? ModSoundEvents.BIBI_HURT.get() : SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.BIBI_DEATH != null ? ModSoundEvents.BIBI_DEATH.get() : SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("BossState", this.bossState.ordinal());
        output.putBoolean("RewardDropped", this.rewardDropped);
        if (this.arenaCenter != null) {
            output.putInt("ArenaCenterX", this.arenaCenter.getX());
            output.putInt("ArenaCenterY", this.arenaCenter.getY());
            output.putInt("ArenaCenterZ", this.arenaCenter.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int stateOrd = input.getIntOr("BossState", 0);
        if (stateOrd >= 0 && stateOrd < BibiBossState.values().length) {
            setBossState(BibiBossState.values()[stateOrd]);
        }
        this.rewardDropped = input.getBooleanOr("RewardDropped", false);
        int ax = input.getIntOr("ArenaCenterX", 0);
        int ay = input.getIntOr("ArenaCenterY", 0);
        int az = input.getIntOr("ArenaCenterZ", 0);
        if (ay > 0) {
            this.arenaCenter = new BlockPos(ax, ay, az);
        }
    }
}
