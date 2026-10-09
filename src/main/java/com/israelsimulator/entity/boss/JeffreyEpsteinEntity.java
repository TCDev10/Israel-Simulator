package com.israelsimulator.entity.boss;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
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
 * At 50% HP the glass dome + pavilion arena appears once; at 33% HP the boss is renamed
 * "Palm Beach Pete" once (custom name and boss bar, persisted across save/load).
 * The arena's replaced blocks are snapshotted ({@link EpsteinArenaSnapshots}) and restored when
 * the fight ends.
 */
public class JeffreyEpsteinEntity extends Monster {

    private final ServerBossEvent bossEvent;
    private UUID bibiBossUuid;
    private final List<UUID> aliveMinionUuids = new ArrayList<>();

    private int summonMinionsCooldown = 180;
    private int speechCooldown = 200;
    private boolean domeSpawned = false;
    private boolean renamed = false;

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
            // Immune to drowning
            if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_DROWNING)) {
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

        // Water immunity and buoyancy: float out of water
                if (!this.level().isClientSide() && this.isInWater()) {
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.08, this.getDeltaMovement().z);
                }

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            float progress = Math.max(0.0F, Math.min(1.0F, this.getHealth() / this.getMaxHealth()));
            this.bossEvent.setProgress(progress);

            // One-shot phase triggers: arena at 50% HP, rename at 33% HP.
            if (EpsteinPhaseRules.shouldSpawnDome(this.domeSpawned, progress)) {
                this.domeSpawned = true;
                spawnEpsteinIslandDome(serverLevel);
            }
            if (EpsteinPhaseRules.shouldRename(this.renamed, progress)) {
                applyRename();
            }

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

    public boolean isRenamed() {
        return this.renamed;
    }

    public boolean isDomeSpawned() {
        return this.domeSpawned;
    }

    private static net.minecraft.network.chat.MutableComponent renamedName() {
        return Component.translatable(EpsteinPhaseRules.RENAMED_NAME_KEY);
    }

    /** Switches the custom name and the boss bar to "Palm Beach Pete". */
    private void applyRename() {
        this.renamed = true;
        this.setCustomName(renamedName());
        this.bossEvent.setName(renamedName().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
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

    /** Pavilion template shared with the island_temple worldgen structure. */
    public static final Identifier ISLAND_TEMPLE_TEMPLATE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "island_temple");
    /** Sandstone footing rows under the template floor (Rigid.FOOTING in the generator). */
    public static final int TEMPLE_FOOTING = 4;
    /** Plaza spot (template x/z) where Epstein stands; players are moved next to it. */
    public static final int ARENA_SPAWN_X = 13;
    public static final int ARENA_SPAWN_Z = 3;
    public static final int ARENA_PLAYER_Z = 1;
    /** Single-layer glass shell: horizontal / vertical radius, big enough for the pavilion. */
    public static final int DOME_RADIUS = 21;
    public static final int DOME_HEIGHT = 27;

    private void spawnEpsteinIslandDome(ServerLevel serverLevel) {
        BlockPos feet = this.blockPosition();
        int groundY = feet.getY() - 1;
        Optional<StructureTemplate> template = serverLevel.getStructureManager().get(ISLAND_TEMPLE_TEMPLATE);
        Vec3i size = template.map(StructureTemplate::getSize).orElse(new Vec3i(27, 28, 28));
        // The plaza floor (template y = footing) lands on the ground Epstein stands on,
        // with Epstein on the plaza in front of the stairs.
        BlockPos origin = new BlockPos(feet.getX() - ARENA_SPAWN_X, groundY - TEMPLE_FOOTING, feet.getZ() - ARENA_SPAWN_Z);
        BlockPos domeCenter = new BlockPos(origin.getX() + size.getX() / 2, groundY, origin.getZ() + size.getZ() / 2);

        // Players inside the future dome, collected before the blocks change.
        AABB arena = new AABB(domeCenter).inflate(DOME_RADIUS + 1, DOME_HEIGHT, DOME_RADIUS + 1);
        List<ServerPlayer> players = serverLevel.getEntitiesOfClass(ServerPlayer.class, arena, p -> p.isAlive() && !p.isSpectator());

        // Snapshot every position the arena can touch (pavilion box + dome shell) so the world
        // can be restored exactly when the fight ends.
        List<BlockPos> touched = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))) {
            touched.add(p.immutable());
        }
        for (int dy = 0; dy <= DOME_HEIGHT; dy++) {
            for (int dx = -DOME_RADIUS; dx <= DOME_RADIUS; dx++) {
                for (int dz = -DOME_RADIUS; dz <= DOME_RADIUS; dz++) {
                    if (isDomeShell(dx, dy, dz)) {
                        touched.add(domeCenter.offset(dx, dy, dz));
                    }
                }
            }
        }
        BlockPos snapMin = new BlockPos(Math.min(origin.getX(), domeCenter.getX() - DOME_RADIUS), origin.getY(),
                Math.min(origin.getZ(), domeCenter.getZ() - DOME_RADIUS));
        BlockPos snapMax = new BlockPos(Math.max(origin.getX() + size.getX() - 1, domeCenter.getX() + DOME_RADIUS),
                Math.max(origin.getY() + size.getY() - 1, domeCenter.getY() + DOME_HEIGHT),
                Math.max(origin.getZ() + size.getZ() - 1, domeCenter.getZ() + DOME_RADIUS));
        List<EpsteinArenaSnapshots.Before> before = EpsteinArenaSnapshots.capture(serverLevel, touched);

        template.ifPresent(t -> t.placeInWorld(serverLevel, origin, origin, new StructurePlaceSettings().setIgnoreEntities(true),
                serverLevel.getRandom(), Block.UPDATE_CLIENTS));

        // Single-layer glass dome (ellipsoid shell) around the pavilion, only in air.
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= DOME_HEIGHT; dy++) {
            for (int dx = -DOME_RADIUS; dx <= DOME_RADIUS; dx++) {
                for (int dz = -DOME_RADIUS; dz <= DOME_RADIUS; dz++) {
                    if (!isDomeShell(dx, dy, dz)) continue;
                    cursor.set(domeCenter.getX() + dx, domeCenter.getY() + dy, domeCenter.getZ() + dz);
                    if (serverLevel.getBlockState(cursor).isAir()) {
                        serverLevel.setBlock(cursor, glass, Block.UPDATE_ALL);
                    }
                }
            }
        }

        EpsteinArenaSnapshots.record(serverLevel, this.getUUID(), before, snapMin, snapMax);

        // Nobody ends up inside a wall: Epstein on the plaza, players facing him.
        double plazaY = origin.getY() + TEMPLE_FOOTING + 1;
        this.teleportTo(origin.getX() + ARENA_SPAWN_X + 0.5, plazaY, origin.getZ() + ARENA_SPAWN_Z + 0.5);
        int i = 0;
        for (ServerPlayer player : players) {
            int dx = (i % 2 == 0 ? -1 : 1) * (2 + 2 * (i / 2 % 4));
            player.teleportTo(serverLevel, origin.getX() + ARENA_SPAWN_X + dx + 0.5, plazaY,
                    origin.getZ() + ARENA_PLAYER_Z + 0.5, Set.of(), 0.0F, 0.0F, true);
            i++;
        }

        // Visual and audio cue
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 0.8F);
        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1, this.getZ(),
                100, 4, 4, 4, 0.1);
    }

    /** True when (dx,dy,dz) is inside the dome ellipsoid and touches the outside. */
    static boolean isDomeShell(int dx, int dy, int dz) {
        if (!insideDome(dx, dy, dz)) return false;
        return !insideDome(dx + 1, dy, dz) || !insideDome(dx - 1, dy, dz)
                || !insideDome(dx, dy + 1, dz) || !insideDome(dx, dy, dz + 1) || !insideDome(dx, dy, dz - 1);
    }

    private static boolean insideDome(int dx, int dy, int dz) {
        double h = (dx * dx + dz * dz) / (double) (DOME_RADIUS * DOME_RADIUS);
        double v = (dy * dy) / (double) (DOME_HEIGHT * DOME_HEIGHT);
        return h + v <= 1.0;
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
            // Fight over: remove the dome + pavilion and put the world back.
            EpsteinArenaSnapshots.restore(serverLevel, this.getUUID());

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
    public void onRemoval(Entity.RemovalReason reason) {
        super.onRemoval(reason);
        // Killed or discarded (despawn, /kill, peaceful): the fight is over. Unloading keeps the arena.
        if (ArenaRestoreRules.restoresOnRemoval(reason.shouldDestroy()) && this.level() instanceof ServerLevel serverLevel) {
            EpsteinArenaSnapshots.restore(serverLevel, this.getUUID());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("SummonMinionsCooldown", this.summonMinionsCooldown);
        output.putBoolean("DomeSpawned", this.domeSpawned);
        output.putBoolean("Renamed", this.renamed);
        if (this.bibiBossUuid != null) {
            output.putString("BibiBossUUID", this.bibiBossUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.summonMinionsCooldown = input.getIntOr("SummonMinionsCooldown", 180);
        this.domeSpawned = input.getBooleanOr("DomeSpawned", false);
        if (input.getBooleanOr("Renamed", false)) {
            // Custom name is restored by vanilla; the boss bar needs it again.
            applyRename();
        }
        String uuidStr = input.getStringOr("BibiBossUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.bibiBossUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}

