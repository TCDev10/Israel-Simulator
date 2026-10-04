package com.israelsimulator.performance;

import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performance optimization and profiling manager (GAME_DESIGN.md §55).
 *
 * <p>Enforces:
 * <ul>
 *   <li>Tick throttling (avoids running expensive checks every tick).</li>
 *   <li>Chunk NPC density capping (prevents mob runaway).</li>
 *   <li>Particle emission limits (prevents client FPS drops).</li>
 *   <li>Memory leak prevention via periodic cleanup of inactive cache entries.</li>
 * </ul>
 * </p>
 */
public final class PerformanceManager {
    private static final Map<ChunkPos, AtomicInteger> CHUNK_NPC_COUNTS = new ConcurrentHashMap<>();
    private static final int DEFAULT_MAX_NPCS_PER_CHUNK = 12;
    private static final int MAX_PARTICLES_PER_EVENT = 30;

    private PerformanceManager() {}

    /**
     * Checks if a periodic task should run on this tick based on interval.
     */
    public static boolean shouldTick(long currentTick, int interval) {
        if (interval <= 1) {
            return true;
        }
        return currentTick % interval == 0;
    }

    /**
     * Checks if additional NPCs can spawn in the target chunk without exceeding population limits.
     */
    public static boolean canSpawnNpc(ChunkPos chunkPos, int maxNpcLimit) {
        int limit = maxNpcLimit > 0 ? maxNpcLimit : DEFAULT_MAX_NPCS_PER_CHUNK;
        AtomicInteger count = CHUNK_NPC_COUNTS.computeIfAbsent(chunkPos, k -> new AtomicInteger(0));
        return count.get() < limit;
    }

    /**
     * Tracks an NPC spawn in a chunk.
     */
    public static void registerNpcInChunk(ChunkPos chunkPos) {
        CHUNK_NPC_COUNTS.computeIfAbsent(chunkPos, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Tracks an NPC despawn or death in a chunk.
     */
    public static void unregisterNpcInChunk(ChunkPos chunkPos) {
        AtomicInteger count = CHUNK_NPC_COUNTS.get(chunkPos);
        if (count != null) {
            count.decrementAndGet();
            if (count.get() <= 0) {
                CHUNK_NPC_COUNTS.remove(chunkPos);
            }
        }
    }

    /**
     * Clamps particle counts to prevent rendering lag.
     */
    public static int clampParticleCount(int requestedCount) {
        return Math.max(1, Math.min(requestedCount, MAX_PARTICLES_PER_EVENT));
    }

    /**
     * Resets chunk tracking caches for testing or world transitions.
     */
    public static void clearCaches() {
        CHUNK_NPC_COUNTS.clear();
    }
}
