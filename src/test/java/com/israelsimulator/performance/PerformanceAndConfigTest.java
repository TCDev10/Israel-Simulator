package com.israelsimulator.performance;

import com.israelsimulator.config.IsraelSimulatorConfig;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceAndConfigTest {

    @BeforeEach
    public void setup() {
        PerformanceManager.clearCaches();
    }

    @Test
    public void testTickThrottling() {
        // Ticking every 20 ticks
        assertTrue(PerformanceManager.shouldTick(0L, 20));
        assertFalse(PerformanceManager.shouldTick(1L, 20));
        assertFalse(PerformanceManager.shouldTick(19L, 20));
        assertTrue(PerformanceManager.shouldTick(20L, 20));
        assertTrue(PerformanceManager.shouldTick(100L, 20));

        // Interval 1 always ticks
        assertTrue(PerformanceManager.shouldTick(5L, 1));
    }

    @Test
    public void testChunkNpcDensityCapping() {
        ChunkPos chunk = new ChunkPos(10, 20);
        int maxLimit = 3;

        assertTrue(PerformanceManager.canSpawnNpc(chunk, maxLimit));
        PerformanceManager.registerNpcInChunk(chunk); // 1
        assertTrue(PerformanceManager.canSpawnNpc(chunk, maxLimit));
        PerformanceManager.registerNpcInChunk(chunk); // 2
        assertTrue(PerformanceManager.canSpawnNpc(chunk, maxLimit));
        PerformanceManager.registerNpcInChunk(chunk); // 3
        assertFalse(PerformanceManager.canSpawnNpc(chunk, maxLimit), "Must reject spawn when at or above limit");

        PerformanceManager.unregisterNpcInChunk(chunk); // 2
        assertTrue(PerformanceManager.canSpawnNpc(chunk, maxLimit));
    }

    @Test
    public void testParticleClamping() {
        assertEquals(1, PerformanceManager.clampParticleCount(-5));
        assertEquals(1, PerformanceManager.clampParticleCount(0));
        assertEquals(15, PerformanceManager.clampParticleCount(15));
        assertEquals(30, PerformanceManager.clampParticleCount(30));
        assertEquals(30, PerformanceManager.clampParticleCount(500), "Must clamp excessive particle counts");
    }

    @Test
    public void testConfigDefaults() {
        assertTrue(IsraelSimulatorConfig.citySpacingChunks() >= 16);
        assertTrue(IsraelSimulatorConfig.rareStructureSpacingChunks() >= 24);
        assertTrue(IsraelSimulatorConfig.maxNpcPerChunk() > 0);
        assertTrue(IsraelSimulatorConfig.eventDurationTicks() > 0);
        assertTrue(IsraelSimulatorConfig.eventCooldownTicks() > 0);
        assertTrue(IsraelSimulatorConfig.speechMinParticipationTicks() > 0);
        assertTrue(IsraelSimulatorConfig.prayerCooldownTicks() > 0);
        assertTrue(IsraelSimulatorConfig.shofarCooldownTicks() > 0);
        assertTrue(IsraelSimulatorConfig.transitCooldownTicks() > 0);
        assertTrue(IsraelSimulatorConfig.maxParticlesPerEvent() > 0);
        assertTrue(IsraelSimulatorConfig.musicVolumeMultiplier() > 0);
    }
}
