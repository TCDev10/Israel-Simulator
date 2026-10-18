package com.israelsimulator.event.world.plane;

import com.israelsimulator.entity.event.PlaneCrashEntity;
import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModSoundEvents;
import com.israelsimulator.world.structure.ModStructures;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;

/**
 * Event controller for the Twin Towers Plane Crash in the Plains (GAME_DESIGN.md §41–44).
 * Periodically triggers with a chance when a player is inside the Twin Towers in the plains.
 * Sounds air raid siren, counts down, launches the plane, and detonates TNT inside the elevator shaft.
 */
public final class TwinTowersPlaneEvent {

    public static final int SCHEDULE_INTERVAL = 1200; // 60 seconds
    public static final float SCHEDULE_CHANCE = 0.35F; // 35% chance per interval
    public static final int COUNTDOWN_SECONDS = 10;

    // Twin towers dimensions from twin_towers.nbt / towers.schem (74 x 200 x 71)
    public static final int TOWER_WIDTH_X = 74;
    public static final int TOWER_HEIGHT = 195;
    public static final int TOWER_LENGTH_Z = 71;

    // Manually set or tracked towers origin position in the world
    private static BlockPos towersOrigin = null;

    // Runtime state
    private static boolean active = false;
    private static UUID activePlaneId = null;
    private static int countdownTicks = -1;
    private static int lastCountdownSecond = -1;
    private static BlockPos targetElevatorShaft = null;
    private static BlockPos targetTowerMin = null;
    private static BlockPos targetTowerMax = null;
    private static Vec3 targetImpactPos = Vec3.ZERO;
    private static long lastTriggerTick = -1L;

    private TwinTowersPlaneEvent() {}

    public static boolean isActive() {
        return active;
    }

    public static void setTowersOrigin(BlockPos origin) {
        towersOrigin = origin != null ? origin.immutable() : null;
    }

    public static BlockPos getTowersOrigin() {
        return towersOrigin;
    }

    /**
     * Periodic tick called from ModGameEvents onLevelTick.
     */
    public static void tick(ServerLevel level) {
        // Twin Towers are in the Overworld; prevent ticking multiple times per game tick in Nether/End
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }

        long now = level.getGameTime();

        if (active) {
            tickActiveEvent(level);
            return;
        }

        // Try schedule every 1200 ticks
        if (now % SCHEDULE_INTERVAL == 0) {
            trySchedule(level, now);
        }
    }

    private static void trySchedule(ServerLevel level, long now) {
        if (active) {
            return;
        }
        if (lastTriggerTick > 0 && now - lastTriggerTick < 6000L) { // 5 minutes cooldown
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (isPlayerInsideTowers(level, player)) {
                if (level.getRandom().nextFloat() < SCHEDULE_CHANCE) {
                    startEvent(level, player.blockPosition());
                    lastTriggerTick = now;
                    return;
                }
            }
        }
    }

    public static boolean isPlayerInsideTowers(ServerLevel level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();

        // 1. Check generated world structure if player is inside it
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos,
                h -> h.is(ModStructures.TWIN_TOWERS));
        if (start != null && start.isValid() && !start.getPieces().isEmpty()) {
            var box = start.getBoundingBox();
            towersOrigin = new BlockPos(box.minX(), box.minY(), box.minZ());
            return true;
        }

        // 2. Check manually configured or cached origin bounding box
        if (towersOrigin != null) {
            return pos.getX() >= towersOrigin.getX() && pos.getX() <= towersOrigin.getX() + TOWER_WIDTH_X
                    && pos.getY() >= towersOrigin.getY() && pos.getY() <= towersOrigin.getY() + TOWER_HEIGHT
                    && pos.getZ() >= towersOrigin.getZ() && pos.getZ() <= towersOrigin.getZ() + TOWER_LENGTH_Z;
        }

        return false;
    }

    public static boolean startEvent(ServerLevel level, BlockPos nearPos) {
        if (active) return false;

        // Auto-locate structure or use base pos
        StructureStart start = level.structureManager().getStructureWithPieceAt(nearPos,
                h -> h.is(ModStructures.TWIN_TOWERS));
        BlockPos base;
        if (start != null && start.isValid() && !start.getPieces().isEmpty()) {
            var box = start.getBoundingBox();
            base = new BlockPos(box.minX(), box.minY(), box.minZ());
            towersOrigin = base;
        } else if (towersOrigin != null) {
            base = towersOrigin;
        } else {
            base = new BlockPos(nearPos.getX() - 35, nearPos.getY(), nearPos.getZ() - 35);
            towersOrigin = base;
        }

        // Randomly select North Tower or South Tower
        boolean targetNorthTower = level.getRandom().nextBoolean();
        // Random impact height between floor 25 and floor 80 of the tower
        double impactY = base.getY() + 55.0 + level.getRandom().nextDouble() * 85.0;

        if (targetNorthTower) {
            targetTowerMin = base;
            targetTowerMax = base.offset(35, TOWER_HEIGHT, 35);
            targetElevatorShaft = base.offset(17, 0, 17);
            targetImpactPos = new Vec3(base.getX() + 17.5, impactY, base.getZ() + 17.5);
        } else {
            targetTowerMin = base.offset(30, 0, 35);
            targetTowerMax = base.offset(74, TOWER_HEIGHT, 71);
            targetElevatorShaft = base.offset(51, 0, 53);
            targetImpactPos = new Vec3(base.getX() + 52.0, impactY, base.getZ() + 53.0);
        }

        active = true;
        countdownTicks = COUNTDOWN_SECONDS * 20;
        lastCountdownSecond = -1;

        // Broadcast Air Raid Warning & sound siren
        broadcastAirRaidAlert(level, base);

        // Spawn plane immediately so it flies towards the towers during the 10-second countdown!
        spawnIncomingPlane(level);

        return true;
    }

    private static void tickActiveEvent(ServerLevel level) {
        if (countdownTicks > 0) {
            int secs = (countdownTicks + 19) / 20;
            if (secs != lastCountdownSecond) {
                lastCountdownSecond = secs;
                sendCountdownTitle(level, secs);

                // Periodic Air Raid Siren sound every 2 seconds
                if (secs % 2 == 0) {
                    BlockPos soundPos = targetTowerMin != null ? targetTowerMin.offset(17, 100, 17) : BlockPos.ZERO;
                    level.playSound(null, soundPos.getX(), soundPos.getY(), soundPos.getZ(),
                            ModSoundEvents.AIR_RAID_SIREN.get(), SoundSource.NEUTRAL, 8.0F, 1.0F);
                }
            }

            countdownTicks--;
            if (countdownTicks == 0) {
                sendCountdownTitle(level, 0);
            }
        }
    }

    private static void broadcastAirRaidAlert(ServerLevel level, BlockPos base) {
        Component title = Component.literal("§c§l⚠ ALLARME ANTIAEREO ⚠");
        Component subtitle = Component.literal("§eAereo in avvicinamento alle torri! Evacuare!");

        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 10));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.sendSystemMessage(Component.literal("§4[EMERGENZA] §cSirena di allarme antiaereo attivata! Rilevato velivolo in rotta di collisione!"));
        }

        level.playSound(null, base.getX() + 35, base.getY() + 100, base.getZ() + 35,
                ModSoundEvents.AIR_RAID_SIREN.get(), SoundSource.NEUTRAL, 10.0F, 1.0F);
    }

    private static void sendCountdownTitle(ServerLevel level, int seconds) {
        Component title = Component.literal("§c§l⚠ ALLARME ANTIAEREO ⚠");
        Component subtitle = Component.literal("§6Impatto stimato in: §e§l" + seconds + "s");
        Component overlay = Component.literal("§c§lALLARME ANTIAEREO §7- §6Impatto in: §e§l" + seconds + "s");
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 20, 0));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.sendOverlayMessage(overlay);
        }
        BlockPos soundCenter = targetTowerMin != null ? targetTowerMin.offset(17, 100, 17) : (towersOrigin != null ? towersOrigin : BlockPos.ZERO);
        level.playSound(null, soundCenter.getX(), soundCenter.getY(), soundCenter.getZ(),
                SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 2.0F, 1.4F);
    }

    private static void spawnIncomingPlane(ServerLevel level) {
        // Random spawn distance between 100 and 140 blocks away
        double distance = 100.0 + level.getRandom().nextDouble() * 40.0;
        double speed = distance / (COUNTDOWN_SECONDS * 20.0); // Exactly reaches tower area at countdown ~0s

        // Completely random 360-degree approach angle around the target tower
        double angle = level.getRandom().nextDouble() * 2.0 * Math.PI;
        double spawnX = targetImpactPos.x + Math.cos(angle) * distance;
        double spawnZ = targetImpactPos.z + Math.sin(angle) * distance;
        // Slight random altitude variation (diving or climbing approach)
        double spawnY = targetImpactPos.y + (level.getRandom().nextDouble() * 20.0 - 5.0);

        Vec3 spawnPos = new Vec3(spawnX, spawnY, spawnZ);

        PlaneCrashEntity plane = new PlaneCrashEntity(ModEntities.PLANE_CRASH.get(), level);
        plane.initFlight(spawnPos, targetImpactPos, targetTowerMin, targetTowerMax, targetElevatorShaft, speed);

        level.addFreshEntity(plane);
        activePlaneId = plane.getUUID();

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                ModSoundEvents.PLANE_JET.get(), SoundSource.NEUTRAL, 6.0F, 0.8F);
    }

    /**
     * Called when the plane crashes directly into the tower facade and detonates.
     */
    public static void onPlaneCrashImpact(ServerLevel level, Vec3 impactPos) {
        Component title = Component.literal("§4§l⚠ IMPATTO AVVENUTO ⚠");
        Component subtitle = Component.literal("§cL'aereo si è schiantato contro la torre!");
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 40, 15));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.sendOverlayMessage(Component.literal("§4§lIMPATTO! §cL'aereo si è schiantato contro la torre!"));
            player.sendSystemMessage(Component.literal("§4§l[DISASTRO] §cL'aereo si è schiantato contro la torre distruggendo la facciata e la struttura!"));
        }

        // Event finished
        active = false;
        countdownTicks = -1;
        lastCountdownSecond = -1;
        activePlaneId = null;
    }

    public static void stopEvent() {
        active = false;
        countdownTicks = -1;
        lastCountdownSecond = -1;
        activePlaneId = null;
    }
}
