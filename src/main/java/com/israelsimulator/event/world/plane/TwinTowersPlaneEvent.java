package com.israelsimulator.event.world.plane;

import com.israelsimulator.entity.event.PlaneCrashEntity;
import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModSoundEvents;
import com.israelsimulator.world.structure.ModStructures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

/**
 * Twin Towers plane event (GAME_DESIGN.md §41–44).
 *
 * <p>Every minute (1200 ticks), every generated {@code twin_towers} structure with a player within
 * {@link #PLAYER_RADIUS} blocks gets one plane. The plane spawns on a ring outside the whole structure,
 * aimed straight at the center of one of the two towers (picked at random), and explodes when it hits
 * the facade. Only one plane per structure is in the air at a time. Explosions, fire and wreckage only
 * touch blocks inside the targeted tower (see {@link PlaneCrashEntity}).
 */
public final class TwinTowersPlaneEvent {

    public static final int SCHEDULE_INTERVAL = 1200; // 60 seconds
    public static final int PLAYER_RADIUS = 160;
    public static final int ALERT_RADIUS = 256;
    /** Ticks the plane needs to reach the tower from its spawn point (~10 s). */
    public static final int APPROACH_TICKS = 200;
    public static final double RING_MARGIN_MIN = 48.0;
    public static final double RING_MARGIN_MAX = 96.0;
    public static final double HEIGHT_FRACTION_MIN = 0.40;
    public static final double HEIGHT_FRACTION_MAX = 0.85;

    // Twin towers template size from twin_towers.nbt (74 x 200 x 71)
    public static final int TOWER_WIDTH_X = 74;
    public static final int TOWER_HEIGHT = 195;
    public static final int TOWER_LENGTH_Z = 71;

    /**
     * Footprints of the two towers in template-local coordinates (unrotated), measured from twin_towers.nbt:
     * {minX, minY, minZ, maxX, maxY, maxZ}.
     */
    static final int[][] TOWER_LOCAL_BOXES = {
            {5, 0, 5, 29, 177, 29},
            {33, 0, 42, 57, 178, 66}
    };

    // Manually set origin for towers built by hand (/israelsim towers setpos); unrotated template layout.
    private static BlockPos towersOrigin = null;

    /** Active plane per structure, keyed by the structure start chunk (or the manual origin). */
    private static final Map<Long, UUID> ACTIVE_PLANES = new HashMap<>();

    private TwinTowersPlaneEvent() {}

    /** Target geometry of one structure: the whole structure box plus the two tower boxes. */
    public record Towers(long key, BoundingBox structureBox, BoundingBox[] towers) {}

    public static boolean isActive() {
        return !ACTIVE_PLANES.isEmpty();
    }

    public static void setTowersOrigin(BlockPos origin) {
        towersOrigin = origin != null ? origin.immutable() : null;
    }

    public static BlockPos getTowersOrigin() {
        return towersOrigin;
    }

    /** Called every level tick from ModGameEvents. */
    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (level.getGameTime() % SCHEDULE_INTERVAL != 0) {
            return;
        }
        cleanup(level);
        for (Towers towers : findTowersNearPlayers(level)) {
            launchPlane(level, towers);
        }
    }

    /** Manual trigger used by {@code /israelsim event plane_crash start}. */
    public static boolean startEvent(ServerLevel level, BlockPos nearPos) {
        cleanup(level);
        Towers towers = findTowersNear(level, nearPos);
        return towers != null && launchPlane(level, towers);
    }

    // ---------------------------------------------------------------- structure lookup

    private static List<Towers> findTowersNearPlayers(ServerLevel level) {
        Structure structure = twinTowersStructure(level);
        Map<Long, Towers> found = new HashMap<>();
        if (structure != null) {
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator()) continue;
                collectStarts(level, structure, player.getBlockX(), player.getBlockZ(), found);
            }
        }
        // keep only structures really within PLAYER_RADIUS of some player
        List<Towers> result = new ArrayList<>();
        for (Towers t : found.values()) {
            for (ServerPlayer player : level.players()) {
                if (!player.isSpectator() && horizontalDistanceToBox(t.structureBox(), player.getX(), player.getZ()) <= PLAYER_RADIUS) {
                    result.add(t);
                    break;
                }
            }
        }
        return result;
    }

    private static Towers findTowersNear(ServerLevel level, BlockPos pos) {
        Structure structure = twinTowersStructure(level);
        if (structure != null) {
            Map<Long, Towers> found = new HashMap<>();
            collectStarts(level, structure, pos.getX(), pos.getZ(), found);
            Towers best = null;
            double bestDist = Double.MAX_VALUE;
            for (Towers t : found.values()) {
                double d = horizontalDistanceToBox(t.structureBox(), pos.getX() + 0.5, pos.getZ() + 0.5);
                if (d < bestDist) {
                    bestDist = d;
                    best = t;
                }
            }
            if (best != null) {
                return best;
            }
        }
        if (towersOrigin != null) {
            return fromLayout(Long.MIN_VALUE, towersOrigin, Rotation.NONE);
        }
        return null;
    }

    private static Structure twinTowersStructure(ServerLevel level) {
        return level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(ModStructures.TWIN_TOWERS);
    }

    /** Collects twin_towers starts referenced by loaded chunks within PLAYER_RADIUS of (x, z). Never loads chunks. */
    private static void collectStarts(ServerLevel level, Structure structure, int x, int z, Map<Long, Towers> out) {
        int radius = SectionPos.blockToSectionCoord(PLAYER_RADIUS) + 1;
        int ccx = SectionPos.blockToSectionCoord(x);
        int ccz = SectionPos.blockToSectionCoord(z);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(ccx + dx, ccz + dz);
                if (chunk == null) continue;
                for (long ref : chunk.getReferencesForStructure(structure)) {
                    if (out.containsKey(ref)) continue;
                    ChunkPos startPos = ChunkPos.unpack(ref);
                    ChunkAccess startChunk = level.getChunk(startPos.x(), startPos.z(), ChunkStatus.STRUCTURE_STARTS, false);
                    if (startChunk == null) continue;
                    StructureStart start = startChunk.getStartForStructure(structure);
                    if (start == null || !start.isValid() || start.getPieces().isEmpty()) continue;
                    Towers towers = fromStart(ref, start);
                    if (towers != null) out.put(ref, towers);
                }
            }
        }
    }

    static Towers fromStart(long key, StructureStart start) {
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof PoolElementStructurePiece pool) {
                Towers t = fromLayout(key, pool.getPosition(), pool.getRotation());
                return new Towers(key, start.getBoundingBox(), t.towers());
            }
        }
        // Unknown piece type: split the structure box in two halves along its longer side
        BoundingBox box = start.getBoundingBox();
        BoundingBox a, b;
        if (box.getXSpan() >= box.getZSpan()) {
            int mid = (box.minX() + box.maxX()) / 2;
            a = new BoundingBox(box.minX(), box.minY(), box.minZ(), mid, box.maxY(), box.maxZ());
            b = new BoundingBox(mid + 1, box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
        } else {
            int mid = (box.minZ() + box.maxZ()) / 2;
            a = new BoundingBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), mid);
            b = new BoundingBox(box.minX(), box.minY(), mid + 1, box.maxX(), box.maxY(), box.maxZ());
        }
        return new Towers(key, box, new BoundingBox[] {a, b});
    }

    /** Tower boxes in world coordinates for a template placed at {@code position} with {@code rotation}. */
    static Towers fromLayout(long key, BlockPos position, Rotation rotation) {
        BoundingBox[] towers = new BoundingBox[TOWER_LOCAL_BOXES.length];
        for (int i = 0; i < TOWER_LOCAL_BOXES.length; i++) {
            int[] l = TOWER_LOCAL_BOXES[i];
            BlockPos c0 = StructureTemplate.transform(new BlockPos(l[0], l[1], l[2]), Mirror.NONE, rotation, BlockPos.ZERO);
            BlockPos c1 = StructureTemplate.transform(new BlockPos(l[3], l[4], l[5]), Mirror.NONE, rotation, BlockPos.ZERO);
            towers[i] = BoundingBox.fromCorners(position.offset(c0), position.offset(c1));
        }
        BlockPos s0 = position;
        BlockPos s1 = position.offset(StructureTemplate.transform(
                new BlockPos(TOWER_WIDTH_X - 1, TOWER_HEIGHT, TOWER_LENGTH_Z - 1), Mirror.NONE, rotation, BlockPos.ZERO));
        return new Towers(key, BoundingBox.fromCorners(s0, s1), towers);
    }

    static double horizontalDistanceToBox(BoundingBox box, double x, double z) {
        double dx = Math.max(0.0, Math.max(box.minX() - x, x - (box.maxX() + 1)));
        double dz = Math.max(0.0, Math.max(box.minZ() - z, z - (box.maxZ() + 1)));
        return Math.sqrt(dx * dx + dz * dz);
    }

    // ---------------------------------------------------------------- spawning

    /** Target point: horizontal center of the tower, at 40%–85% of its height. */
    static Vec3 towerTarget(BoundingBox tower, double heightFraction) {
        double cx = (tower.minX() + tower.maxX() + 1) / 2.0;
        double cz = (tower.minZ() + tower.maxZ() + 1) / 2.0;
        double y = tower.minY() + tower.getYSpan() * heightFraction;
        return new Vec3(cx, y, cz);
    }

    /** Spawn distance from the structure's horizontal center: half-diagonal of its box plus 48..96 blocks. */
    static double ringDistance(BoundingBox structureBox, double margin01) {
        double hx = structureBox.getXSpan() / 2.0;
        double hz = structureBox.getZSpan() / 2.0;
        return Math.sqrt(hx * hx + hz * hz) + Mth.lerp(margin01, RING_MARGIN_MIN, RING_MARGIN_MAX);
    }

    /** Whether the horizontal segment from {@code a} to {@code b} crosses {@code box} grown by {@code pad} blocks. */
    static boolean segmentCrossesBox2D(Vec3 a, Vec3 b, BoundingBox box, double pad) {
        double minX = box.minX() - pad, maxX = box.maxX() + 1 + pad;
        double minZ = box.minZ() - pad, maxZ = box.maxZ() + 1 + pad;
        double t0 = 0.0, t1 = 1.0;
        double[] p = {a.x, a.z};
        double[] d = {b.x - a.x, b.z - a.z};
        double[] lo = {minX, minZ};
        double[] hi = {maxX, maxZ};
        for (int i = 0; i < 2; i++) {
            if (Math.abs(d[i]) < 1.0E-9) {
                if (p[i] < lo[i] || p[i] > hi[i]) return false;
            } else {
                double ta = (lo[i] - p[i]) / d[i];
                double tb = (hi[i] - p[i]) / d[i];
                t0 = Math.max(t0, Math.min(ta, tb));
                t1 = Math.min(t1, Math.max(ta, tb));
                if (t0 > t1) return false;
            }
        }
        return true;
    }

    private static boolean launchPlane(ServerLevel level, Towers towers) {
        UUID existing = ACTIVE_PLANES.get(towers.key());
        if (existing != null) {
            Entity e = level.getEntity(existing);
            if (e != null && e.isAlive()) {
                return false; // one plane per structure at a time
            }
            ACTIVE_PLANES.remove(towers.key());
        }

        RandomSource random = level.getRandom();
        int towerIndex = random.nextInt(towers.towers().length);
        BoundingBox tower = towers.towers()[towerIndex];
        Vec3 target = towerTarget(tower, Mth.lerp(random.nextDouble(), HEIGHT_FRACTION_MIN, HEIGHT_FRACTION_MAX));

        BoundingBox sb = towers.structureBox();
        double centerX = (sb.minX() + sb.maxX() + 1) / 2.0;
        double centerZ = (sb.minZ() + sb.maxZ() + 1) / 2.0;

        // Prefer an approach that does not cross the other tower (wingspan ~36 blocks), else any loaded spot.
        Vec3 spawn = null;
        Vec3 fallback = null;
        for (int attempt = 0; attempt < 16 && spawn == null; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = ringDistance(sb, random.nextDouble());
            double y = Math.min(target.y + random.nextDouble() * 15.0, level.getMaxY() - 10.0);
            Vec3 candidate = new Vec3(centerX + Math.cos(angle) * dist, y, centerZ + Math.sin(angle) * dist);
            if (!level.isPositionEntityTicking(BlockPos.containing(candidate))) {
                continue;
            }
            boolean blocked = false;
            for (int i = 0; i < towers.towers().length; i++) {
                if (i != towerIndex && segmentCrossesBox2D(candidate, target, towers.towers()[i], 18.0)) {
                    blocked = true;
                    break;
                }
            }
            if (blocked) {
                if (fallback == null) fallback = candidate;
            } else {
                spawn = candidate;
            }
        }
        if (spawn == null) {
            spawn = fallback;
        }
        if (spawn == null) {
            return false; // no loaded spot around the structure
        }

        double speed = spawn.distanceTo(target) / APPROACH_TICKS;
        PlaneCrashEntity plane = new PlaneCrashEntity(ModEntities.PLANE_CRASH.get(), level);
        BlockPos towerMin = new BlockPos(tower.minX(), tower.minY(), tower.minZ());
        BlockPos towerMax = new BlockPos(tower.maxX(), tower.maxY(), tower.maxZ());
        BlockPos shaft = BlockPos.containing((tower.minX() + tower.maxX()) / 2.0, tower.minY(), (tower.minZ() + tower.maxZ()) / 2.0);
        plane.initFlight(spawn, target, towerMin, towerMax, shaft, speed);
        if (!level.addFreshEntity(plane)) {
            return false;
        }
        ACTIVE_PLANES.put(towers.key(), plane.getUUID());

        level.playSound(null, spawn.x, spawn.y, spawn.z, ModSoundEvents.PLANE_JET.get(), SoundSource.NEUTRAL, 6.0F, 0.8F);
        level.playSound(null, target.x, target.y, target.z, ModSoundEvents.AIR_RAID_SIREN.get(), SoundSource.NEUTRAL, 8.0F, 1.0F);
        broadcastAlert(level, target);
        return true;
    }

    private static void broadcastAlert(ServerLevel level, Vec3 target) {
        Component title = Component.literal("§c§l⚠ ALLARME ANTIAEREO ⚠");
        Component subtitle = Component.literal("§eAereo in avvicinamento alle torri! Evacuare!");
        for (ServerPlayer player : nearbyPlayers(level, target)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 10));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    private static List<ServerPlayer> nearbyPlayers(ServerLevel level, Vec3 pos) {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - pos.x;
            double dz = player.getZ() - pos.z;
            if (dx * dx + dz * dz <= (double) ALERT_RADIUS * ALERT_RADIUS) {
                out.add(player);
            }
        }
        return out;
    }

    private static void cleanup(ServerLevel level) {
        ACTIVE_PLANES.values().removeIf(id -> {
            Entity e = level.getEntity(id);
            return e == null || !e.isAlive();
        });
    }

    // ---------------------------------------------------------------- callbacks from the plane

    /** Called when the plane crashes into the tower facade and detonates. */
    public static void onPlaneCrashImpact(ServerLevel level, Vec3 impactPos, UUID planeId) {
        ACTIVE_PLANES.values().remove(planeId);
        Component title = Component.literal("§4§l⚠ IMPATTO AVVENUTO ⚠");
        Component subtitle = Component.literal("§cL'aereo si è schiantato contro la torre!");
        for (ServerPlayer player : nearbyPlayers(level, impactPos)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 40, 15));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    /** Called when the plane despawns without hitting anything. */
    public static void onPlaneGone(UUID planeId) {
        ACTIVE_PLANES.values().remove(planeId);
    }

    /** Removes every tracked plane from the world ({@code /israelsim event plane_crash stop}). */
    public static void stopEvent(ServerLevel level) {
        for (UUID id : List.copyOf(ACTIVE_PLANES.values())) {
            Entity e = level.getEntity(id);
            if (e != null) e.discard();
        }
        ACTIVE_PLANES.clear();
    }

    public static void stopEvent() {
        ACTIVE_PLANES.clear();
    }
}
