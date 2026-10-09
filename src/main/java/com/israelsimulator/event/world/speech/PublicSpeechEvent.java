package com.israelsimulator.event.world.speech;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.entity.boss.EpsteinArenaSnapshots;
import com.israelsimulator.entity.npc.OratorEntity;
import com.israelsimulator.event.world.WorldEventManager;
import com.israelsimulator.event.world.WorldEventType;
import com.israelsimulator.quest.DiscoveryQuest;
import com.israelsimulator.quest.QuestManager;
import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModItems;
import com.israelsimulator.world.structure.ModStructures;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Server-side runtime of the Public Speech event (GAME_DESIGN.md §34, PLAN.md phase 33):
 * builds the temporary stage in a city plaza, spawns Charlie Kirk and a small crowd, and after
 * exactly 60 s ends with a harmless explosion effect on Charlie Kirk, who drops one First Amendment.
 * The stage is snapshotted through {@link EpsteinArenaSnapshots} (keyed by Charlie Kirk UUID)
 * and the world is put back when the event ends.
 */
public final class PublicSpeechEvent {
    public static final String CROWD_TAG = "israel_simulator.speech_crowd";
    public static final int SPEECH_LINES = 8;
    public static final int LINE_INTERVAL = 240;
    /** Scheduler: how often to try, and the chance per try when a player is in a city. */
    public static final int SCHEDULE_INTERVAL = 1200;
    public static final float SCHEDULE_CHANCE = 0.35F;
    private static final double ANNOUNCE_RADIUS = 128.0;
    private static final double BAR_RADIUS = 32.0;

    private static UUID oratorId;
    private static ResourceKey<Level> dimension;
    private static BlockPos center;
    private static long endTick;
    private static long nextLineTick;
    private static int lineIndex;
    private static final List<UUID> crowd = new ArrayList<>();
    private static ServerBossEvent bar;
    private static long lastEndTick = -1L;
    private static boolean rewardDropped;

    private PublicSpeechEvent() {}

    public static boolean isActive() {
        return oratorId != null;
    }

    public static boolean isOrator(UUID id) {
        return id != null && id.equals(oratorId);
    }

    public static boolean isCrowdMember(UUID id) {
        return crowd.contains(id);
    }

    public static BlockPos center() {
        return center;
    }

    public static UUID oratorId() {
        return oratorId;
    }

    public static List<UUID> crowdIds() {
        return List.copyOf(crowd);
    }

    public static long remainingTicks(long now) {
        return isActive() ? Math.max(0, endTick - now) : 0;
    }

    /** Starts the event with the plaza ground at {@code ground} (first air block above the floor). */
    public static boolean start(ServerLevel level, BlockPos ground) {
        if (isActive()) return false;
        OratorEntity orator = ModEntities.ORATOR.get().create(level, EntitySpawnReason.EVENT);
        if (orator == null) return false;

        List<BlockPos> touched = new ArrayList<>();
        for (int[] o : SpeechStageLayout.footprint()) touched.add(ground.offset(o[0], o[1], o[2]));
        List<EpsteinArenaSnapshots.Before> before = EpsteinArenaSnapshots.capture(level, touched);
        SpeechStage.place(level, ground);
        EpsteinArenaSnapshots.record(level, orator.getUUID(), before,
                ground.offset(SpeechStageLayout.MIN_X, SpeechStageLayout.MIN_Y, SpeechStageLayout.MIN_Z),
                ground.offset(SpeechStageLayout.MAX_X, SpeechStageLayout.MAX_Y, SpeechStageLayout.MAX_Z));

        oratorId = orator.getUUID();
        dimension = level.dimension();
        center = ground.immutable();
        long now = level.getGameTime();
        endTick = now + WorldEventType.PUBLIC_SPEECH.getDurationTicks();
        nextLineTick = now + 60;
        lineIndex = 0;
        rewardDropped = false;

        orator.snapTo(ground.getX() + SpeechStageLayout.ORATOR_X + 0.5, ground.getY() + SpeechStageLayout.ORATOR_Y,
                ground.getZ() + SpeechStageLayout.ORATOR_Z + 0.5, 0.0F, 0.0F);
        orator.setYHeadRot(0.0F);
        level.addFreshEntity(orator);

        crowd.clear();
        for (int[] spot : SpeechStageLayout.crowdSpots()) {
            Villager v = net.minecraft.world.entity.EntityTypes.VILLAGER.create(level, EntitySpawnReason.EVENT);
            if (v == null) continue;
            v.snapTo(ground.getX() + spot[0] + 0.5, ground.getY() + spot[1], ground.getZ() + spot[2] + 0.5,
                    180.0F, 0.0F);
            v.addTag(CROWD_TAG);
            v.setPersistenceRequired();
            crowd.add(v.getUUID());
            level.addFreshEntity(v);
        }

        bar = new ServerBossEvent(UUID.randomUUID(),
                Component.translatable("bossbar.israel_simulator.public_speech").withStyle(ChatFormatting.GOLD),
                BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        WorldEventManager.startEvent(WorldEventType.PUBLIC_SPEECH, center, now);

        Component msg = Component.translatable("message.israel_simulator.public_speech_start").withStyle(ChatFormatting.GOLD);
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(center.getX(), center.getY(), center.getZ()) <= ANNOUNCE_RADIUS * ANNOUNCE_RADIUS) {
                p.sendSystemMessage(msg);
            }
        }
        level.playSound(null, center, SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 2.0F, 1.0F);
        IsraelSimulator.LOGGER.info("Public Speech started at {} in {}", center, dimension.identifier());
        return true;
    }

    /** Ends the event, removes Orator and crowd and restores the plaza. */
    public static void end(ServerLevel level, boolean announce) {
        if (!isActive()) return;
        UUID id = oratorId;
        oratorId = null;
        ServerLevel eventLevel = level.getServer().getLevel(dimension);
        if (eventLevel == null) eventLevel = level;
        if (bar != null) bar.removeAllPlayers();
        bar = null;
        for (UUID c : crowd) {
            Entity e = eventLevel.getEntity(c);
            if (e != null && !e.isRemoved()) e.discard();
        }
        crowd.clear();
        Entity orator = eventLevel.getEntity(id);
        if (orator != null && !orator.isRemoved()) orator.discard();
        EpsteinArenaSnapshots.restore(eventLevel, id);
        WorldEventManager.endEvent(WorldEventType.PUBLIC_SPEECH);
        lastEndTick = eventLevel.getGameTime();
        if (announce && center != null) {
            Component msg = Component.translatable("message.israel_simulator.public_speech_end").withStyle(ChatFormatting.GOLD);
            for (ServerPlayer p : eventLevel.players()) {
                if (p.distanceToSqr(center.getX(), center.getY(), center.getZ()) <= ANNOUNCE_RADIUS * ANNOUNCE_RADIUS) {
                    p.sendSystemMessage(msg);
                }
            }
        }
        IsraelSimulator.LOGGER.info("Public Speech ended");
    }

    /** Server stop: forget runtime state (Charlie Kirk restores the plaza on the next load). */
    public static void reset() {
        oratorId = null;
        crowd.clear();
        bar = null;
        center = null;
        lastEndTick = -1L;
        WorldEventManager.endEvent(WorldEventType.PUBLIC_SPEECH);
    }

    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        if (!isActive()) {
            if (level.dimension() == Level.OVERWORLD && now % SCHEDULE_INTERVAL == 0) trySchedule(level, now);
            return;
        }
        if (level.dimension() != dimension) return;
        if (now >= endTick) {
            finale(level);
            end(level, true);
            return;
        }
        if (now >= nextLineTick) {
            speak(level);
            nextLineTick = now + LINE_INTERVAL;
        }
        if (now % 20 == 0) {
            tickParticipation(level, now);
            tickCrowd(level);
        }
    }

    private static void speak(ServerLevel level) {
        Entity orator = level.getEntity(oratorId);
        Component line = Component.translatable("speech.israel_simulator.orator.line_" + (lineIndex % SPEECH_LINES))
                .withStyle(ChatFormatting.ITALIC);
        Component full = Component.translatable("entity.israel_simulator.orator").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.GOLD)).append(line);
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(center.getX(), center.getY(), center.getZ()) <= BAR_RADIUS * BAR_RADIUS) {
                p.sendSystemMessage(full);
            }
        }
        if (orator != null) {
            level.playSound(null, orator.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.NEUTRAL, 1.0F, 1.2F);
        }
        // Crowd reacts: mostly applause, the occasional boo.
        boolean applause = level.getRandom().nextFloat() < 0.75F;
        for (UUID c : crowd) {
            if (!(level.getEntity(c) instanceof Villager v)) continue;
            if (applause) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, v.getX(), v.getY() + 2.1, v.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
                level.playSound(null, v.blockPosition(), SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 0.8F, 1.0F);
                if (v.onGround()) v.getJumpControl().jump();
            } else {
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, v.getX(), v.getY() + 2.1, v.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
                level.playSound(null, v.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 0.8F, 1.0F);
                v.setUnhappyCounter(40);
            }
        }
        lineIndex++;
    }

    private static void tickParticipation(ServerLevel level, long now) {
        float remaining = (endTick - now) / (float) WorldEventType.PUBLIC_SPEECH.getDurationTicks();
        if (bar != null) bar.setProgress(Math.max(0.0F, Math.min(1.0F, remaining)));
        for (ServerPlayer p : level.players()) {
            double d = p.distanceToSqr(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            if (bar != null) {
                if (d <= BAR_RADIUS * BAR_RADIUS) bar.addPlayer(p); else bar.removePlayer(p);
            }
            if (!p.isSpectator() && SpeechParticipation.inRange(d)) {
                p.sendOverlayMessage(Component.translatable("message.israel_simulator.public_speech_progress",
                        Math.max(0, (endTick - now) / 20)).withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    /**
     * Natural end of the speech: Charlie Kirk goes out with a purely visual explosion (particles and
     * the generic explode sound, no block or entity damage) and drops exactly one First Amendment
     * on the stage. Guarded by {@link #rewardDropped} so an event can never drop twice.
     */
    private static void finale(ServerLevel level) {
        if (!SpeechParticipation.shouldDropReward(rewardDropped, true)) return;
        rewardDropped = true;
        Entity orator = level.getEntity(oratorId);
        double x = orator != null ? orator.getX() : center.getX() + SpeechStageLayout.ORATOR_X + 0.5;
        double y = orator != null ? orator.getY() : center.getY() + SpeechStageLayout.ORATOR_Y;
        double z = orator != null ? orator.getZ() : center.getZ() + SpeechStageLayout.ORATOR_Z + 0.5;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.0, z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 1.0, z, 12, 1.0, 0.8, 1.0, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.0, z, 20, 0.6, 0.6, 0.6, 0.02);
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 4.0F, 1.0F);
        ItemEntity drop = new ItemEntity(level, x, y + 0.5, z, new ItemStack(ModItems.FIRST_AMENDMENT.get(), 1));
        drop.setDeltaMovement(0.0, 0.2, 0.0);
        drop.setDefaultPickUpDelay();
        drop.setUnlimitedLifetime();
        level.addFreshEntity(drop);
        Component msg = Component.translatable("message.israel_simulator.public_speech_reward").withStyle(ChatFormatting.GREEN);
        for (ServerPlayer p : level.players()) {
            if (SpeechParticipation.inRange(p.distanceToSqr(x, y, z)) && !p.isSpectator()) {
                p.sendSystemMessage(msg);
                QuestManager.completeQuest(p, DiscoveryQuest.CIVIC_VOICE);
            }
        }
    }

    /** Keeps the crowd near the stage and facing Charlie Kirk; small idle shuffles come from vanilla AI. */
    private static void tickCrowd(ServerLevel level) {
        Entity orator = level.getEntity(oratorId);
        int i = 0;
        for (UUID c : crowd) {
            if (!(level.getEntity(c) instanceof Villager v)) { i++; continue; }
            int[] spot = SpeechStageLayout.crowdSpots().get(i % SpeechStageLayout.crowdSpots().size());
            double sx = center.getX() + spot[0] + 0.5, sz = center.getZ() + spot[2] + 0.5;
            double dist = v.distanceToSqr(sx, center.getY(), sz);
            if (dist > 100) {
                v.teleportTo(sx, center.getY(), sz);
            } else if (dist > 9) {
                v.getNavigation().moveTo(sx, center.getY(), sz, 0.5);
            }
            if (orator != null) v.getLookControl().setLookAt(orator, 30.0F, 30.0F);
            i++;
        }
    }

    public static int requiredTicks() {
        try {
            return IsraelSimulatorConfig.speechMinParticipationTicks();
        } catch (RuntimeException e) {
            return (int) WorldEventType.PUBLIC_SPEECH.getMinParticipationTicks();
        }
    }

    private static long cooldownTicks() {
        try {
            return IsraelSimulatorConfig.eventCooldownTicks();
        } catch (RuntimeException e) {
            return 12000L;
        }
    }

    /** Scheduled start: a player standing in Tel Aviv or Jerusalem may get a speech in the city plaza. */
    private static void trySchedule(ServerLevel level, long now) {
        if (!SpeechParticipation.canSchedule(now, lastEndTick, cooldownTicks(), isActive())) return;
        if (level.players().isEmpty() || level.getRandom().nextFloat() >= SCHEDULE_CHANCE) return;
        ServerPlayer p = level.players().get(level.getRandom().nextInt(level.players().size()));
        StructureStart start = level.structureManager().getStructureWithPieceAt(p.blockPosition(),
                h -> h.is(ModStructures.TEL_AVIV_CITY) || h.is(ModStructures.JERUSALEM_CITY));
        if (start == null || !start.isValid() || start.getPieces().isEmpty()) return;
        BlockPos plaza = findPlaza(level, start.getPieces().get(0).getBoundingBox().getCenter());
        start(level, plaza);
    }

    /** Flattest spot near the city's central piece, sampled on the heightmap. */
    public static BlockPos findPlaza(ServerLevel level, BlockPos near) {
        BlockPos best = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near);
        int bestScore = Integer.MAX_VALUE;
        for (int r = 0; r <= 16; r += 4) {
            for (int dx = -r; dx <= r; dx += 4) {
                for (int dz = -r; dz <= r; dz += 4) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    BlockPos c = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, near.offset(dx, 0, dz));
                    if (!level.getFluidState(c.below()).isEmpty()) continue;
                    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                    for (int sx = SpeechStageLayout.MIN_X; sx <= SpeechStageLayout.MAX_X; sx += 4) {
                        for (int sz = SpeechStageLayout.MIN_Z; sz <= SpeechStageLayout.MAX_Z; sz += 5) {
                            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, c.getX() + sx, c.getZ() + sz);
                            min = Math.min(min, y);
                            max = Math.max(max, y);
                        }
                    }
                    int score = (max - min) * 4 + r;
                    if (score < bestScore) {
                        bestScore = score;
                        best = new BlockPos(c.getX(), min, c.getZ());
                    }
                }
            }
        }
        return best;
    }
}
