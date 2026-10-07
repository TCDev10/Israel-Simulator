package com.israelsimulator.entity.boss;

import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Satirical Phase 3 Miniboss summoned by Bibi Boss at 33% HP (GAME_DESIGN.md §41–44).
 * Summons waves of easy-to-defeat child-skinned baby zombies ("Private Island Call").
 */
public class JeffreyEpsteinEntity extends Monster {

    private final ServerBossEvent bossEvent;
    private UUID bibiBossUuid;
    private final List<UUID> aliveMinionUuids = new ArrayList<>();

    private int summonMinionsCooldown = 180;
    private int speechCooldown = 200;

    public JeffreyEpsteinEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.bossEvent = new ServerBossEvent(
                this.getUUID(),
                Component.translatable("entity.israel_simulator.jeffrey_epstein")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.NOTCHED_6
        );
        this.bossEvent.setDarkenScreen(false);
        this.xpReward = 600;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public void setBibiBossUuid(UUID uuid) {
        this.bibiBossUuid = uuid;
    }

    public UUID getBibiBossUuid() {
        return this.bibiBossUuid;
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
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        // Complete explosion immunity
        if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        // Friendly fire protection with Bibi and allies
        if (BibiBossEntity.isAlly(damageSource.getEntity())) {
            return false;
        }
        return super.hurtServer(serverLevel, damageSource, amount);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (BibiBossEntity.isAlly(target)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@org.jspecify.annotations.Nullable LivingEntity target) {
        if (BibiBossEntity.isAlly(target)) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            float progress = Math.max(0.0F, Math.min(1.0F, this.getHealth() / this.getMaxHealth()));
            this.bossEvent.setProgress(progress);

            if (summonMinionsCooldown > 0) summonMinionsCooldown--;
            if (speechCooldown > 0) speechCooldown--;

            // Clean dead minions
            this.aliveMinionUuids.removeIf(uuid -> {
                Entity e = serverLevel.getEntity(uuid);
                return e == null || !e.isAlive();
            });

            // Synchronize target from Bibi boss if Epstein is idle
            if (this.getTarget() == null && this.bibiBossUuid != null) {
                Entity bibi = serverLevel.getEntity(this.bibiBossUuid);
                if (bibi instanceof BibiBossEntity boss && boss.getTarget() != null) {
                    this.setTarget(boss.getTarget());
                }
            }

            // Summon child zombie wave
            if (summonMinionsCooldown <= 0 && this.aliveMinionUuids.size() < 6) {
                summonChildZombies(serverLevel);
                summonMinionsCooldown = 280;
            }

            // Satirical voice line
            if (speechCooldown <= 0) {
                broadcastEpsteinSpeech(serverLevel);
                speechCooldown = 320;
            }
        }
    }

    private void summonChildZombies(ServerLevel serverLevel) {
        if (ModEntities.CHILD_ZOMBIE_MINION == null) return;

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.8F, 1.4F);

        Component shout = Component.translatable("speech.israel_simulator.epstein.summon_kids")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        AABB shoutArea = this.getBoundingBox().inflate(32.0);
        for (Player p : serverLevel.getEntitiesOfClass(Player.class, shoutArea)) {
            p.sendSystemMessage(shout);
        }

        int count = 3 + this.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            ChildZombieMinionEntity minion = ModEntities.CHILD_ZOMBIE_MINION.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (minion != null) {
                double offsetX = (this.random.nextDouble() - 0.5) * 6.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 6.0;
                minion.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                minion.setEpsteinBossUuid(this.getUUID());
                if (this.getTarget() != null) {
                    minion.setTarget(this.getTarget());
                }
                serverLevel.addFreshEntity(minion);
                this.aliveMinionUuids.add(minion.getUUID());

                serverLevel.sendParticles(ParticleTypes.WITCH,
                        minion.getX(), minion.getY() + 0.3, minion.getZ(),
                        15, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    private void broadcastEpsteinSpeech(ServerLevel serverLevel) {
        int quoteIndex = this.random.nextInt(3);
        Component quote = Component.translatable("speech.israel_simulator.epstein.quote_" + quoteIndex)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);
        AABB aabb = this.getBoundingBox().inflate(32.0);
        for (Player p : serverLevel.getEntitiesOfClass(Player.class, aabb)) {
            p.sendSystemMessage(quote);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.bossEvent.removeAllPlayers();

        if (this.level() instanceof ServerLevel serverLevel) {
            // Dismiss remaining active child zombie minions
            for (UUID uuid : this.aliveMinionUuids) {
                Entity minion = serverLevel.getEntity(uuid);
                if (minion != null && minion.isAlive()) {
                    minion.discard();
                }
            }

            // Drops: emeralds, gold, shekels
            if (ModItems.SHEKEL != null) {
                serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(),
                        new ItemStack(ModItems.SHEKEL.get(), 24)));
            }
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(),
                    new ItemStack(Items.EMERALD, 8)));
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(),
                    new ItemStack(Items.GOLD_INGOT, 10)));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("SummonMinionsCooldown", this.summonMinionsCooldown);
        if (this.bibiBossUuid != null) {
            output.putString("BibiBossUUID", this.bibiBossUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.summonMinionsCooldown = input.getIntOr("SummonMinionsCooldown", 180);
        String uuidStr = input.getStringOr("BibiBossUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.bibiBossUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}

