package com.israelsimulator.entity.boss;

import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.entity.projectile.BibiPoisonBreathEntity;
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

    public static final float PHASE_2_HEALTH_FRACTION = 0.66F; // Below 66% HP = Phase 2 ENRAGED (Trump)
    public static final float PHASE_3_HEALTH_FRACTION = 0.33F; // Below 33% HP = Phase 3 DESPERATE (Epstein)
    public static final float ENRAGE_HEALTH_FRACTION = 0.66F;  // Backwards compatibility with tests
    public static final float MAX_SINGLE_HIT_DAMAGE = 500.0F; // Anti-one-shot exploit cap

    private final ServerBossEvent bossEvent;
    private BibiBossState bossState = BibiBossState.IDLE;
    private BlockPos arenaCenter;
    private boolean rewardDropped = false;

    private int guardSummonCooldown = 120;
    private int specialAttackCooldown = 120;
    private int missileBarrageCooldown = 180;
    private int speechCooldown = 160;

    private UUID trumpMinibossUuid = null;
    private UUID epsteinBossUuid = null;
    /** Set when the Epstein / Palm Beach Pete summoned by this Bibi dies; unlocks the poison breath. Persisted. */
    private boolean epsteinDefeated = false;
    private int poisonBreathCooldown = BibiPoisonBreathRules.MIN_INTERVAL_TICKS;
    private UUID poisonBreathTargetUuid = null;
    private int poisonBreathWarningTicks = 0;
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
            this.bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
            this.bossEvent.setName(Component.translatable("entity.israel_simulator.bibi_boss.enraged")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            if (!this.level().isClientSide() && ModSoundEvents.BIBI_ENRAGE != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSoundEvents.BIBI_ENRAGE.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
            }
        } else if (newState == BibiBossState.DESPERATE) {
            this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
            this.bossEvent.setName(Component.translatable("entity.israel_simulator.bibi_boss.desperate")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            if (!this.level().isClientSide()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.7F);
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

            // Water immunity and buoyancy: float out of water
                        if (!this.level().isClientSide() && this.isInWater()) {
                            this.setDeltaMovement(this.getDeltaMovement().x, 0.08, this.getDeltaMovement().z);
                        }

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

            // State Machine evaluation (3-phase boss encounter)
            if (this.bossState != BibiBossState.DEFEATED) {
                if (progress <= PHASE_3_HEALTH_FRACTION && this.bossState != BibiBossState.DESPERATE) {
                    setBossState(BibiBossState.DESPERATE);
                    serverLevel.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 1.2, this.getZ(),
                            60, 0.8, 0.8, 0.8, 0.2);
                    summonEpsteinMiniboss(serverLevel);
                } else if (progress <= PHASE_2_HEALTH_FRACTION && this.bossState != BibiBossState.ENRAGED && this.bossState != BibiBossState.DESPERATE) {
                    setBossState(BibiBossState.ENRAGED);
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.2, this.getZ(),
                            50, 0.8, 0.8, 0.8, 0.15);
                    summonTrumpMiniboss(serverLevel);
                } else if (this.getTarget() != null && this.bossState == BibiBossState.IDLE) {
                    setBossState(BibiBossState.COMBAT);
                }
            }

            // Combat timers and special attacks
            if (this.bossState.isAggressive()) {
                handleCombatRoutines(serverLevel);
            }

            // Late-fight poison breath (only after Epstein / Palm Beach Pete has died)
            tickPoisonBreath(serverLevel);

            // Visual effects for ENRAGED and DESPERATE phases
            if (this.bossState == BibiBossState.ENRAGED && this.tickCount % 5 == 0) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5),
                        this.getY() + this.random.nextDouble() * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5),
                        2, 0.1, 0.1, 0.1, 0.02);
            } else if (this.bossState == BibiBossState.DESPERATE && this.tickCount % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.WITCH,
                        this.getX() + (this.random.nextDouble() - 0.5),
                        this.getY() + this.random.nextDouble() * 2.0,
                        this.getZ() + (this.random.nextDouble() - 0.5),
                        3, 0.1, 0.1, 0.1, 0.03);
            }
        }
    }

    /** True for fire, lava, magma and other fire-tagged damage that Bibi ignores. */
    public static boolean isFireOrLavaDamage(DamageSource source) {
        return source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                || source.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                || source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR);
    }

    public boolean isEpsteinDefeated() {
        return this.epsteinDefeated;
    }

    /** Called by the Epstein summoned by this Bibi when it dies. */
    public void onEpsteinDefeated() {
        if (!this.epsteinDefeated) {
            this.epsteinDefeated = true;
            this.epsteinBossUuid = null;
            this.poisonBreathCooldown = BibiPoisonBreathRules.nextInterval(new java.util.Random(this.random.nextLong()));
        }
    }

    private void tickPoisonBreath(ServerLevel serverLevel) {
        boolean bibiDefeated = this.bossState == BibiBossState.DEFEATED || !this.isAlive();
        if (!BibiPoisonBreathRules.isUnlocked(this.epsteinDefeated, bibiDefeated)) {
            this.poisonBreathTargetUuid = null;
            return;
        }
        if (this.poisonBreathTargetUuid != null) {
            net.minecraft.world.entity.Entity e = serverLevel.getEntity(this.poisonBreathTargetUuid);
            if (!(e instanceof Player target) || !target.isAlive() || target.isSpectator()) {
                this.poisonBreathTargetUuid = null;
                return;
            }
            double topY = target.getY() + BibiPoisonBreathRules.DROP_HEIGHT;
            if (this.poisonBreathWarningTicks % 3 == 0) {
                serverLevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 3.0, target.getZ(),
                        6, 0.6, 0.2, 0.6, 0.01);
                serverLevel.sendParticles(ParticleTypes.SNEEZE, target.getX(), topY, target.getZ(),
                        8, 0.5, 0.5, 0.5, 0.02);
            }
            if (--this.poisonBreathWarningTicks <= 0) {
                BibiPoisonBreathEntity orb = ModEntities.BIBI_POISON_BREATH.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
                if (orb != null) {
                    orb.setOwner(this);
                    orb.setPos(target.getX(), topY, target.getZ());
                    orb.accelerationPower = 0.05;
                    orb.setDeltaMovement(0.0, -0.6, 0.0);
                    serverLevel.addFreshEntity(orb);
                    serverLevel.playSound(null, target.getX(), topY, target.getZ(),
                            SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 2.0F, 0.9F);
                }
                this.poisonBreathTargetUuid = null;
            }
            return;
        }
        if (this.poisonBreathCooldown > 0) this.poisonBreathCooldown--;
        if (!BibiPoisonBreathRules.shouldLaunch(this.epsteinDefeated, bibiDefeated, this.poisonBreathCooldown)) {
            return;
        }
        List<Player> candidates = serverLevel.getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(BibiPoisonBreathRules.TARGET_RANGE),
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative());
        this.poisonBreathCooldown = BibiPoisonBreathRules.nextInterval(new java.util.Random(this.random.nextLong()));
        if (candidates.isEmpty()) {
            return;
        }
        Player target = candidates.get(this.random.nextInt(candidates.size()));
        this.poisonBreathTargetUuid = target.getUUID();
        this.poisonBreathWarningTicks = BibiPoisonBreathRules.WARNING_TICKS;
        serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.2F, 1.4F);
    }

    private void handleCombatRoutines(ServerLevel serverLevel) {
        // Cooldown decrements
        if (guardSummonCooldown > 0) guardSummonCooldown--;
        if (specialAttackCooldown > 0) specialAttackCooldown--;
        if (missileBarrageCooldown > 0) missileBarrageCooldown--;
        if (speechCooldown > 0) speechCooldown--;

        // 1. "Coalition Call" — summon guards up to limit (Phase 1 only, spawn less frequently)
        if (this.bossState != BibiBossState.ENRAGED) {
            int maxGuards = IsraelSimulatorConfig.bibiBossMaxGuards();
            cleanActiveGuards(serverLevel);
            if (guardSummonCooldown <= 0 && this.aliveGuardUuids.size() < maxGuards) {
                summonCoalitionGuards(serverLevel, Math.min(2, maxGuards - this.aliveGuardUuids.size()));
                guardSummonCooldown = 450; // Guard spawn frequency reduced
            }
        }

        // 2. Aerial missile barrage attack bombarding environment
        if (missileBarrageCooldown <= 0) {
            performMissileBarrage(serverLevel);
            missileBarrageCooldown = this.bossState == BibiBossState.ENRAGED ? 90 : 180;
        }

        // 3. "Filibuster Shockwave" — area knockback and debuff (blindness removed)
        if (specialAttackCooldown <= 0) {
            performFilibusterShockwave(serverLevel);
            specialAttackCooldown = this.bossState == BibiBossState.ENRAGED ? 120 : 200;
        }

        // 4. Satirical speech quote broadcast
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
            // Blindness debuff intentionally removed per player request
        }
    }

    private void performMissileBarrage(ServerLevel serverLevel) {
        if (ModEntities.BIBI_MISSILE == null) return;

        int missileCount = this.bossState == BibiBossState.ENRAGED ? 16 : 8;
        double spawnRadius = this.bossState == BibiBossState.ENRAGED ? 16.0 : 10.0;

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 2.0F, 0.6F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.5F, 1.5F);

        Component warning = Component.translatable("speech.israel_simulator.bibi_boss.missile_barrage")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        AABB aabb = this.getBoundingBox().inflate(48.0);
        for (Player p : serverLevel.getEntitiesOfClass(Player.class, aabb)) {
            p.sendSystemMessage(warning);
        }

        for (int i = 0; i < missileCount; i++) {
            double angle = this.random.nextDouble() * 2 * Math.PI;
            double dist = 2.0 + this.random.nextDouble() * spawnRadius;
            double mx = this.getX() + Math.cos(angle) * dist;
            double mz = this.getZ() + Math.sin(angle) * dist;
            double my = this.getY() + 16.0 + this.random.nextDouble() * 8.0;

            BibiMissileEntity missile = new BibiMissileEntity(ModEntities.BIBI_MISSILE.get(), serverLevel);
            missile.setOwner(this);
            missile.setPos(mx, my, mz);
            missile.setBarrageMode(true);
            double vx = (this.random.nextDouble() - 0.5) * 0.2;
            double vz = (this.random.nextDouble() - 0.5) * 0.2;
            double vy = -0.9 - this.random.nextDouble() * 0.4;
            missile.setDeltaMovement(vx, vy, vz);
            serverLevel.addFreshEntity(missile);
        }
    }

    private void summonTrumpMiniboss(ServerLevel serverLevel) {
        if (ModEntities.TRUMP_MINIBOSS == null) return;

        TrumpMinibossEntity trump = ModEntities.TRUMP_MINIBOSS.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (trump != null) {
            double offsetX = (this.random.nextDouble() - 0.5) * 8.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 8.0;
            trump.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
            trump.setBibiBossUuid(this.getUUID());
            if (this.getTarget() != null) {
                trump.setTarget(this.getTarget());
            }
            serverLevel.addFreshEntity(trump);
            this.trumpMinibossUuid = trump.getUUID();

            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    trump.getX(), trump.getY() + 1.0, trump.getZ(), 1, 0, 0, 0, 0);
            serverLevel.playSound(null, trump.getX(), trump.getY(), trump.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 1.0F);

            Component arrivalMsg = Component.translatable("message.israel_simulator.trump_arrival")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            AABB aabb = this.getBoundingBox().inflate(64.0);
            for (Player p : serverLevel.getEntitiesOfClass(Player.class, aabb)) {
                p.sendSystemMessage(arrivalMsg);
            }
        }
    }

    private void summonEpsteinMiniboss(ServerLevel serverLevel) {
        if (ModEntities.JEFFREY_EPSTEIN == null) return;

        JeffreyEpsteinEntity epstein = ModEntities.JEFFREY_EPSTEIN.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (epstein != null) {
            double offsetX = (this.random.nextDouble() - 0.5) * 8.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 8.0;
            epstein.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
            epstein.setBibiBossUuid(this.getUUID());
            if (this.getTarget() != null) {
                epstein.setTarget(this.getTarget());
            }
            serverLevel.addFreshEntity(epstein);
            this.epsteinBossUuid = epstein.getUUID();

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    epstein.getX(), epstein.getY() + 1.0, epstein.getZ(), 40, 0.5, 0.8, 0.5, 0.2);
            serverLevel.playSound(null, epstein.getX(), epstein.getY(), epstein.getZ(),
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.8F, 0.8F);

            Component arrivalMsg = Component.translatable("message.israel_simulator.epstein_arrival")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
            AABB aabb = this.getBoundingBox().inflate(64.0);
            for (Player p : serverLevel.getEntitiesOfClass(Player.class, aabb)) {
                p.sendSystemMessage(arrivalMsg);
            }
        }
    }

    public boolean isTrumpShieldActive(ServerLevel serverLevel) {
        if (this.trumpMinibossUuid == null) {
            return false;
        }
        net.minecraft.world.entity.Entity entity = serverLevel.getEntity(this.trumpMinibossUuid);
        if (entity instanceof TrumpMinibossEntity trump && trump.isAlive()) {
            return true;
        }
        this.trumpMinibossUuid = null;
        return false;
    }

    public boolean isEpsteinShieldActive(ServerLevel serverLevel) {
            if (this.epsteinBossUuid == null) {
            return false;
        }
            net.minecraft.world.entity.Entity entity = serverLevel.getEntity(this.epsteinBossUuid);
            if (entity instanceof JeffreyEpsteinEntity epstein && epstein.isAlive()) {
            return true;
        }
            this.epsteinBossUuid = null;
        return false;
    }

    public void onTrumpDefeated(ServerLevel serverLevel) {
        this.trumpMinibossUuid = null;
        serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.05);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SHIELD_BREAK.value(), SoundSource.HOSTILE, 2.0F, 0.9F);

        Component shieldBrokenMsg = Component.translatable("message.israel_simulator.trump_defeated")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
        AABB aabb = this.getBoundingBox().inflate(64.0);
        for (Player p : serverLevel.getEntitiesOfClass(Player.class, aabb)) {
            p.sendSystemMessage(shieldBrokenMsg);
        }
    }

    public UUID getTrumpMinibossUuid() {
        return this.trumpMinibossUuid;
    }

    public void setTrumpMinibossUuid(UUID uuid) {
        this.trumpMinibossUuid = uuid;
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
        if (ModEntities.BIBI_MISSILE == null) return;

        // Targeted homing missile directly at the player with tracking and area explosion
        BibiMissileEntity missile = new BibiMissileEntity(ModEntities.BIBI_MISSILE.get(), serverLevel);
        missile.setOwner(this);
        missile.setPos(this.getX(), this.getY() + 1.8, this.getZ());
        missile.setTarget(target);
        missile.setHoming(true);

        Vec3 targetPos = target.getEyePosition();
        Vec3 toTarget = targetPos.subtract(this.getX(), this.getY() + 1.8, this.getZ()).normalize();
        missile.setDeltaMovement(toTarget.scale(0.85));
        serverLevel.addFreshEntity(missile);

        // In Phase 2 (ENRAGED), fire an additional volley missile for intensified rhythm!
        if (this.bossState == BibiBossState.ENRAGED) {
            BibiMissileEntity secondMissile = new BibiMissileEntity(ModEntities.BIBI_MISSILE.get(), serverLevel);
            secondMissile.setOwner(this);
            secondMissile.setPos(this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                    this.getY() + 2.0,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 1.5);
            secondMissile.setTarget(target);
            secondMissile.setHoming(true);
            Vec3 spread = toTarget.add((this.random.nextDouble() - 0.5) * 0.2, 0.1, (this.random.nextDouble() - 0.5) * 0.2).normalize();
            secondMissile.setDeltaMovement(spread.scale(0.9));
            serverLevel.addFreshEntity(secondMissile);
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 2.0F, 0.8F);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isAlly(target)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@org.jspecify.annotations.Nullable LivingEntity target) {
        if (isAlly(target)) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        // Fire and lava immunity (also fireImmune() on the EntityType)
        if (isFireOrLavaDamage(damageSource)) {
            this.clearFire();
            return false;
        }
        // Complete explosion immunity (GAME_DESIGN requirement)
        if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
            // Immune to drowning
            if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_DROWNING)) {
                return false;
            }

        // Friendly fire protection with Trump and coalition allies
        if (isAlly(damageSource.getEntity())) {
            return false;
        }

        // Track player damage for multiplayer reward distribution
        if (damageSource.getEntity() instanceof Player player) {
            this.participatingPlayerUuids.add(player.getUUID());
        }

        // Complete diplomatic immunity while Trump Miniboss is alive
        if (isTrumpShieldActive(serverLevel)) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.2, this.getZ(),
                    20, 0.4, 0.6, 0.4, 0.1);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BLOCK.value(), SoundSource.HOSTILE, 1.5F, 1.2F);
            if (damageSource.getEntity() instanceof Player player) {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.bibi_shielded_by_trump")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }
            return false;
        }

        // Complete diplomatic immunity while Epstein Miniboss is alive
        if (isEpsteinShieldActive(serverLevel)) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.2, this.getZ(),
                    20, 0.4, 0.6, 0.4, 0.1);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BLOCK.value(), SoundSource.HOSTILE, 1.5F, 1.2F);
            if (damageSource.getEntity() instanceof Player player) {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.bibi_shielded_by_epstein")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }
            return false;
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

            // Dismiss active Donald Trump miniboss if still present
            if (this.trumpMinibossUuid != null) {
                net.minecraft.world.entity.Entity trump = serverLevel.getEntity(this.trumpMinibossUuid);
                if (trump != null && trump.isAlive()) {
                    trump.discard();
                }
            }

            // Dismiss active Jeffrey Epstein miniboss if still present
            if (this.epsteinBossUuid != null) {
                net.minecraft.world.entity.Entity epstein = serverLevel.getEntity(this.epsteinBossUuid);
                if (epstein != null && epstein.isAlive()) {
                    epstein.discard();
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
        // Guaranteed Rabbi's Crown (MYTHIC) — exclusive drop
        if (ModItems.RABBIS_CROWN != null) {
            ItemStack crown = new ItemStack(ModItems.RABBIS_CROWN.get());
            ItemEntity entity = new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(), crown);
            entity.setGlowingTag(true);
            serverLevel.addFreshEntity(entity);
        }

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
        output.putInt("MissileBarrageCooldown", this.missileBarrageCooldown);
        output.putBoolean("EpsteinDefeated", this.epsteinDefeated);
        output.putInt("PoisonBreathCooldown", this.poisonBreathCooldown);
        if (this.trumpMinibossUuid != null) {
            output.putString("TrumpMinibossUUID", this.trumpMinibossUuid.toString());
        }
        if (this.epsteinBossUuid != null) {
            output.putString("EpsteinBossUUID", this.epsteinBossUuid.toString());
        }
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
        this.missileBarrageCooldown = input.getIntOr("MissileBarrageCooldown", 180);
        this.epsteinDefeated = input.getBooleanOr("EpsteinDefeated", false);
        this.poisonBreathCooldown = input.getIntOr("PoisonBreathCooldown", BibiPoisonBreathRules.MIN_INTERVAL_TICKS);
        String trumpStr = input.getStringOr("TrumpMinibossUUID", "");
        if (!trumpStr.isEmpty()) {
            try {
                this.trumpMinibossUuid = UUID.fromString(trumpStr);
            } catch (IllegalArgumentException ignored) {}
        }
        String epsteinStr = input.getStringOr("EpsteinBossUUID", "");
        if (!epsteinStr.isEmpty()) {
            try {
                this.epsteinBossUuid = UUID.fromString(epsteinStr);
            } catch (IllegalArgumentException ignored) {}
        }
        int ax = input.getIntOr("ArenaCenterX", 0);
        int ay = input.getIntOr("ArenaCenterY", 0);
        int az = input.getIntOr("ArenaCenterZ", 0);
        if (ay > 0) {
            this.arenaCenter = new BlockPos(ax, ay, az);
        }
    }

    public boolean canDrown() {
        return false;
    }

    public boolean isPushedByFluid() {
        return false;
    }

    public static boolean isAlly(net.minecraft.world.entity.Entity entity) {
        if (entity == null) return false;
        return entity instanceof BibiBossEntity
                || entity instanceof TrumpMinibossEntity
                || entity instanceof IceAgentEntity
                || entity instanceof JeffreyEpsteinEntity
                || entity instanceof ChildZombieMinionEntity
                || entity instanceof BibiGuardEntity;
    }
}
