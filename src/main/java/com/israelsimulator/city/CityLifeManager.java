package com.israelsimulator.city;

import com.israelsimulator.npc.IsraelNpcData;
import com.israelsimulator.npc.schedule.NpcSchedule;
import com.israelsimulator.npc.schedule.ScheduleState;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Urban life coordinator managing city movement, population density limits,
 * and pathfinding throttling to maintain 20 TPS performance (GAME_DESIGN.md §28, TODO §28).
 */
public final class CityLifeManager {

    public static final int MAX_NPCS_PER_CHUNK = 8;
    public static final int MAX_NPCS_PER_DISTRICT = 32;
    public static final int DISTRICT_RADIUS = 48;
    public static final long PATHFINDING_COOLDOWN_TICKS = 40L; // 2 seconds between major path recalculations

    private static final Map<UUID, Long> LAST_PATHFINDING_TICKS = new ConcurrentHashMap<>();

    public enum UrbanActivity {
        WORKING_AT_STALL,
        DINING_AT_CAFE,
        STROLLING_STREET,
        VISITING_SYNAGOGUE,
        RESTING_AT_HOME
    }

    private CityLifeManager() {}

    /**
     * Verifies that adding an NPC to a chunk will not exceed the population cap.
     */
    public static boolean canSpawnNpcInChunk(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int maxX = chunkPos.getMaxBlockX();
        int maxZ = chunkPos.getMaxBlockZ();

        AABB box = new AABB(minX, level.getMinY(), minZ, maxX + 1, level.getMaxY(), maxZ + 1);
        int count = level.getEntitiesOfClass(AbstractVillager.class, box).size();

        return count < MAX_NPCS_PER_CHUNK;
    }

    /**
     * Checks district population to prevent large city entity explosions.
     */
    public static boolean canSpawnNpcInDistrict(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }

        AABB districtBox = new AABB(pos).inflate(DISTRICT_RADIUS);
        int count = level.getEntitiesOfClass(AbstractVillager.class, districtBox).size();

        return count < MAX_NPCS_PER_DISTRICT;
    }

    /**
     * Throttles pathfinding to prevent server lag spikes and preserve 20 TPS.
     */
    public static boolean shouldThrottlePathfinding(UUID npcId, long currentTick) {
        Long last = LAST_PATHFINDING_TICKS.get(npcId);
        if (last == null || (currentTick - last) >= PATHFINDING_COOLDOWN_TICKS) {
            LAST_PATHFINDING_TICKS.put(npcId, currentTick);
            return false;
        }
        return true;
    }

    public static void clearPathfindingThrottle(UUID npcId) {
        LAST_PATHFINDING_TICKS.remove(npcId);
    }

    /**
     * Determines current urban activity routine for an NPC based on daytime and schedule.
     */
    public static UrbanActivity getUrbanActivity(IsraelNpcData npcData, long dayTime, boolean isShabbat) {
        ScheduleState state = NpcSchedule.getScheduleState(dayTime, isShabbat, npcData.getProfession());
        return switch (state) {
            case WORK_MORNING, WORK_AFTERNOON -> UrbanActivity.WORKING_AT_STALL;
            case LUNCH -> UrbanActivity.DINING_AT_CAFE;
            case SOCIAL -> UrbanActivity.STROLLING_STREET;
            case SHABBAT_PRAYER -> UrbanActivity.VISITING_SYNAGOGUE;
            case WAKE, HOME, SLEEP, SHABBAT_REST -> UrbanActivity.RESTING_AT_HOME;
        };
    }
}
