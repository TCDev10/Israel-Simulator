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
import net.minecraft.world.entity.item.PrimedTnt;
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

    // Twin towers dimensions from torri_gemelle.nbt / towers.schem (74 x 200 x 71)
    public static final int TOWER_WIDTH_X = 74;
    public static final int TOWER_HEIGHT = 195;
    public static final int TOWER_LENGTH_Z = 71;

    // Manually set or tracked towers origin position in the world
    private static BlockPos towersOrigin = null;

    // Runtime state
    private static boolean active = false;
    private static UUID activePlaneId = null;
    private static int countdownTicks = -1;
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
        if (now - lastTriggerTick < 6000L) { // 5 minutes cooldown
            return;
        }

        for (ServerPlayer player : level.players()) {
            var biomeHolder = level.getBiome(player.blockPosition());
            boolean inPlains = biomeHolder.is(Biomes.PLAINS);

            if (inPlains && isPlayerInsideTowers(level, player)) {
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

        // 1. Check generated world structure if player is in one
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos,
                h -> h.is(ModStructures.TORRI_GEMELLE));
        if (start != null && start.isValid() && !start.getPieces().isEmpty()) {
            var box = start.getBoundingBox();
            towersOrigin = new BlockPos(box.minX(), box.minY(), box.minZ());
            return true;
        }

        // 2. Check manually configured origin bounding box
        if (towersOrigin != null) {
            return pos.getX() >= towersOrigin.getX() && pos.getX() <= towersOrigin.getX() + TOWER_WIDTH_X
                    && pos.getY() >= towersOrigin.getY() && pos.getY() <= towersOrigin.getY() + TOWER_HEIGHT
                    && pos.getZ() >= towersOrigin.getZ() && pos.getZ() <= towersOrigin.getZ() + TOWER_LENGTH_Z;
        }

        // 3. Fallback: If player is high up in plains (Y >= 80)
        return pos.getY() >= 80;
    }

    public static boolean startEvent(ServerLevel level, BlockPos nearPos) {
        if (active) return false;

        // Auto-locate structure or use base pos
        StructureStart start = level.structureManager().getStructureWithPieceAt(nearPos,
                h -> h.is(ModStructures.TORRI_GEMELLE));
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

        // Select North Tower or South Tower
        boolean targetNorthTower = level.getRandom().nextBoolean();
        double impactY = Math.max(nearPos.getY(), base.getY() + 100);

        if (targetNorthTower) {
            targetTowerMin = base;
            targetTowerMax = base.offset(35, TOWER_HEIGHT, 35);
            targetElevatorShaft = base.offset(17, 0, 17);
            targetImpactPos = new Vec3(targetTowerMin.getX(), impactY, targetElevatorShaft.getZ());
        } else {
            targetTowerMin = base.offset(30, 0, 35);
            targetTowerMax = base.offset(74, TOWER_HEIGHT, 71);
            targetElevatorShaft = base.offset(51, 0, 53);
            targetImpactPos = new Vec3(targetTowerMax.getX(), impactY, targetElevatorShaft.getZ());
        }

        active = true;
        countdownTicks = COUNTDOWN_SECONDS * 20;

        // Broadcast Air Raid Warning & sound siren
        broadcastAirRaidAlert(level, base);

        // Spawn plane immediately so it flies towards the towers during the 10-second countdown!
        spawnIncomingPlane(level);

        return true;
    }

    private static void tickActiveEvent(ServerLevel level) {
        if (countdownTicks > 0) {
            countdownTicks--;

            // Periodic Air Raid Siren sound every 2 seconds
            if (countdownTicks % 40 == 0) {
                BlockPos soundPos = targetTowerMin != null ? targetTowerMin.offset(17, 100, 17) : BlockPos.ZERO;
                level.playSound(null, soundPos.getX(), soundPos.getY(), soundPos.getZ(),
                        ModSoundEvents.AIR_RAID_SIREN.get(), SoundSource.NEUTRAL, 8.0F, 1.0F);
            }

            // On-screen countdown notification every second
            if (countdownTicks % 20 == 0) {
                int secs = countdownTicks / 20;
                sendCountdownTitle(level, secs);
            }
        }
    }

    private static void broadcastAirRaidAlert(ServerLevel level, BlockPos base) {
        Component title = Component.literal("§c§l⚠ ALLARME ANTIAEREO ⚠");
        Component subtitle = Component.literal("§eAereo in avvicinamento alle torri! Evacuare!");

        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.sendSystemMessage(Component.literal("§4[EMERGENZA] §cSirena di allarme antiaereo attivata! Rilevato velivolo in rotta di collisione!"));
        }

        level.playSound(null, base.getX() + 35, base.getY() + 100, base.getZ() + 35,
                ModSoundEvents.AIR_RAID_SIREN.get(), SoundSource.NEUTRAL, 10.0F, 1.0F);
    }

    private static void sendCountdownTitle(ServerLevel level, int seconds) {
        Component subtitle = Component.literal("§6Impatto stimato in: §e§l" + seconds + "s");
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 25, 5));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    private static void spawnIncomingPlane(ServerLevel level) {
        // Plane spawns ~100 blocks away along X axis. Over 200 ticks (10s), speed is ~0.5 blocks/tick
        double distance = 100.0;
        double speed = distance / (COUNTDOWN_SECONDS * 20.0); // Exactly reaches tower at countdown 0

        Vec3 spawnPos;
        if (targetImpactPos.x <= (towersOrigin.getX() + 35)) {
            // Approaching from West (-X) towards North Tower
            spawnPos = targetImpactPos.add(-distance, 0, 0);
        } else {
            // Approaching from East (+X) towards South Tower
            spawnPos = targetImpactPos.add(distance, 0, 0);
        }

        PlaneCrashEntity plane = new PlaneCrashEntity(ModEntities.PLANE_CRASH.get(), level);
        plane.initFlight(spawnPos, targetImpactPos, targetTowerMin, targetTowerMax, targetElevatorShaft, speed);

        level.addFreshEntity(plane);
        activePlaneId = plane.getUUID();

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                ModSoundEvents.PLANE_JET.get(), SoundSource.NEUTRAL, 6.0F, 0.8F);
    }

    /**
     * Called when the plane reaches 25% penetration inside the tower facade.
     * Spawns normal PrimedTnt inside the elevator shaft at staggered heights to detonate and destroy blocks!
     */
    public static void detonateElevatorShaft(ServerLevel level, BlockPos shaftPos) {
        if (shaftPos == null) return;

        int baseX = shaftPos.getX();
        int baseZ = shaftPos.getZ();
        int originY = towersOrigin != null ? towersOrigin.getY() : 64;

        // Spawn normal TNT entities at multiple floor levels inside the elevator shaft
        int[] floorHeights = {
                originY + 15,
                originY + 40,
                originY + 70,
                originY + 95,
                originY + 115,
                originY + 135,
                originY + 160
        };

        int fuseDelay = 10;
        for (int y : floorHeights) {
            // Multiple TNTs per level inside the shaft
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    PrimedTnt tnt = new PrimedTnt(level, baseX + dx + 0.5, y, baseZ + dz + 0.5, null);
                    tnt.setFuse(fuseDelay);
                    level.addFreshEntity(tnt);
                }
            }
            fuseDelay += 12; // Cascade explosion vertically down/up the elevator shaft!
        }

        // Additional immediate massive explosion at the impact level inside the elevator
        level.explode(null, baseX + 0.5, originY + 110, baseZ + 0.5, 6.0F, Level.ExplosionInteraction.BLOCK);

        level.playSound(null, baseX, originY + 110, baseZ,
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 6.0F, 0.7F);

        // Alert players
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(Component.literal("§4§l[DISASTRO] §cEsplosioni multiple a catena nel vano ascensore!"));
        }

        // Event finished
        active = false;
        activePlaneId = null;
    }

    public static void stopEvent() {
        active = false;
        countdownTicks = -1;
        activePlaneId = null;
    }
}
