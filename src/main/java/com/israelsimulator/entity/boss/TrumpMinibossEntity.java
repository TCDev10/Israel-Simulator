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
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.phys.Vec3;

/**
 * Satirical Phase 2 Miniboss summoned by Bibi Boss at 50% HP (GAME_DESIGN.md §41–44).
 * Protects Bibi with diplomatic immunity while launching massive "Wall of Money" armor-shredding attacks
 * and calling elite Netherite-clad federal ICE agents.
 */
public class TrumpMinibossEntity extends Monster {

    private final ServerBossEvent bossEvent;
    private UUID bibiBossUuid;
    private final List<UUID> aliveIceAgentUuids = new ArrayList<>();

    private int moneyWallCooldown = 160; // Elevated cooldown attack (~13s)
    private int callIceCooldown = 220;   // Federal backup cooldown (~16s)
    private int speechCooldown = 180;

    public TrumpMinibossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.bossEvent = new ServerBossEvent(
                this.getUUID(),
                Component.translatable("entity.israel_simulator.trump_miniboss")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                BossEvent.BossBarColor.YELLOW,
                BossEvent.BossBarOverlay.NOTCHED_6
        );
        this.bossEvent.setDarkenScreen(false);
        this.xpReward = 800;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
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
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            float progress = Math.max(0.0F, Math.min(1.0F, this.getHealth() / this.getMaxHealth()));
            this.bossEvent.setProgress(progress);

            // Cooldown ticks
            if (moneyWallCooldown > 0) moneyWallCooldown--;
            if (callIceCooldown > 0) callIceCooldown--;
            if (speechCooldown > 0) speechCooldown--;

            // Clean dead ICE agents
            this.aliveIceAgentUuids.removeIf(uuid -> {
                Entity e = serverLevel.getEntity(uuid);
                return e == null || !e.isAlive();
            });

            // 1. "Wall of Money" — Heavy armor-shredding projectile wave
            if (moneyWallCooldown <= 0 && this.getTarget() != null) {
                performMoneyWallAttack(serverLevel, this.getTarget());
                moneyWallCooldown = 260; // Elevated cooldown
            }

            // 2. "Call ICE" — Summons 3 Netherite-clad federal tactical agents
            if (callIceCooldown <= 0 && this.aliveIceAgentUuids.size() < 3) {
                callIceAgents(serverLevel);
                callIceCooldown = 360;
            }

            // Satirical speech cue
            if (speechCooldown <= 0) {
                broadcastTrumpSpeech(serverLevel);
                speechCooldown = 240;
            }
        }
    }

    /**
     * Attack 1: "Wall of Money"
     * Hurls a massive wide wave of flying wealth that inflicts direct armor durability damage and knockback.
     */
    private void performMoneyWallAttack(ServerLevel serverLevel, LivingEntity target) {
        Vec3 origin = this.position().add(0, 1.2, 0);
        Vec3 forward = target.position().subtract(this.position()).normalize();
        Vec3 right = new Vec3(-forward.z, 0, forward.x).normalize();

        // Audio announcement & cash acoustic FX
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.8F, 0.6F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.HOSTILE, 2.0F, 0.5F);

        Component shout = Component.translatable("speech.israel_simulator.trump.wall_of_money")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        AABB shoutArea = this.getBoundingBox().inflate(32.0);
        for (Player p : serverLevel.getEntitiesOfClass(Player.class, shoutArea)) {
            p.sendSystemMessage(shout);
        }

        // Generate the massive wide wavefront of money (spanning 7 columns across and 3 vertical levels)
        for (int step = 1; step <= 12; step += 2) {
            Vec3 waveCenter = origin.add(forward.scale(step));
            for (int col = -3; col <= 3; col++) {
                for (double dy = 0.0; dy <= 2.0; dy += 1.0) {
                    Vec3 particlePos = waveCenter.add(right.scale(col * 0.9)).add(0, dy, 0);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            particlePos.x, particlePos.y, particlePos.z, 2, 0.1, 0.1, 0.1, 0.02);
                    serverLevel.sendParticles(ParticleTypes.WAX_ON,
                            particlePos.x, particlePos.y, particlePos.z, 1, 0.05, 0.05, 0.05, 0.02);
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            particlePos.x, particlePos.y, particlePos.z, 1, 0.05, 0.05, 0.05, 0.05);
                }
            }
        }

        // Damage detection in the wave trajectory
        AABB waveBox = this.getBoundingBox().inflate(14.0);
        List<Player> hitPlayers = serverLevel.getEntitiesOfClass(Player.class, waveBox,
                p -> p.position().subtract(this.position()).dot(forward) > 0.0);

        for (Player p : hitPlayers) {
            // High base impact damage
            p.hurtServer(serverLevel, this.damageSources().mobAttack(this), 16.0F);

            // Directly inflict massive armor durability damage to worn gear!
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack armor = p.getItemBySlot(slot);
                if (!armor.isEmpty()) {
                    armor.hurtAndBreak(35, p, slot);
                }
            }

            // Severe knockback
            Vec3 knockback = forward.scale(1.8).add(0, 0.4, 0);
            p.setDeltaMovement(p.getDeltaMovement().add(knockback));

            serverLevel.sendParticles(ParticleTypes.CRIT,
                    p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.5, 0.5, 0.5, 0.15);
            serverLevel.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.5F, 0.8F);
        }
    }

    /**
     * Attack 2: "Call ICE"
     * Summons 3 federal ICE agents in full Netherite armor with Netherite swords.
     */
    private void callIceAgents(ServerLevel serverLevel) {
        if (ModEntities.ICE_AGENT == null) return;

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 2.0F, 1.2F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 2.0F, 0.6F);

        Component shout = Component.translatable("speech.israel_simulator.trump.call_ice")
                .withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
        AABB shoutArea = this.getBoundingBox().inflate(32.0);
        for (Player p : serverLevel.getEntitiesOfClass(Player.class, shoutArea)) {
            p.sendSystemMessage(shout);
        }

        for (int i = 0; i < 3; i++) {
            IceAgentEntity agent = ModEntities.ICE_AGENT.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (agent != null) {
                double offsetX = (this.random.nextDouble() - 0.5) * 6.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 6.0;
                agent.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                agent.setTrumpMinibossUuid(this.getUUID());
                if (this.getTarget() != null) {
                    agent.setTarget(this.getTarget());
                }
                serverLevel.addFreshEntity(agent);
                this.aliveIceAgentUuids.add(agent.getUUID());

                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        agent.getX(), agent.getY() + 0.5, agent.getZ(),
                        20, 0.4, 0.5, 0.4, 0.05);
            }
        }
    }

    private void broadcastTrumpSpeech(ServerLevel serverLevel) {
        int quoteIndex = this.random.nextInt(3);
        Component quote = Component.translatable("speech.israel_simulator.trump.quote_" + quoteIndex)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
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
            // Dismiss remaining active ICE agents
            for (UUID uuid : this.aliveIceAgentUuids) {
                Entity agent = serverLevel.getEntity(uuid);
                if (agent != null && agent.isAlive()) {
                    agent.discard();
                }
            }

            // Shatter Bibi's shield and notify Bibi boss!
            if (this.bibiBossUuid != null) {
                Entity bibi = serverLevel.getEntity(this.bibiBossUuid);
                if (bibi instanceof BibiBossEntity boss && boss.isAlive()) {
                    boss.onTrumpDefeated(serverLevel);
                }
            }

            // Generous golden drops (Shekels, Gold, Diamonds)
            if (ModItems.SHEKEL != null) {
                serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(),
                        new ItemStack(ModItems.SHEKEL.get(), 32)));
            }
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.getX(), this.getY() + 0.5, this.getZ(),
                    new ItemStack(Items.GOLD_INGOT, 12)));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("MoneyWallCooldown", this.moneyWallCooldown);
        output.putInt("CallIceCooldown", this.callIceCooldown);
        if (this.bibiBossUuid != null) {
            output.putString("BibiBossUUID", this.bibiBossUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.moneyWallCooldown = input.getIntOr("MoneyWallCooldown", 160);
        this.callIceCooldown = input.getIntOr("CallIceCooldown", 220);
        String uuidStr = input.getStringOr("BibiBossUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.bibiBossUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}

