package com.israelsimulator.event.world.plane;

import com.israelsimulator.entity.event.PlaneCrashEntity;
import com.israelsimulator.entity.event.PlaneModelData;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TwinTowersPlaneEventTest {

    @Test
    @DisplayName("PlaneModelData loads schematic blocks successfully")
    void testPlaneModelData() {
        var blocks = PlaneModelData.getBlocks();
        assertNotNull(blocks);
        assertFalse(blocks.isEmpty(), "Plane model blocks must not be empty");
        assertEquals(1034, blocks.size(), "Plane should have 1034 blocks from plane1-e1160.schem");
    }

    @Test
    @DisplayName("Penetration threshold is exactly 25% of total plane length")
    void testPenetrationThreshold() {
        double expected = PlaneCrashEntity.TOTAL_PLANE_LENGTH * 0.25;
        assertEquals(expected, PlaneCrashEntity.PENETRATION_THRESHOLD, 0.001);
        assertTrue(PlaneCrashEntity.PENETRATION_THRESHOLD > 10.0 && PlaneCrashEntity.PENETRATION_THRESHOLD < 11.0);
    }

    @Test
    @DisplayName("TwinTowersPlaneEvent origin and state management")
    void testEventOriginAndState() {
        assertFalse(TwinTowersPlaneEvent.isActive());
        BlockPos testPos = new BlockPos(100, 64, 200);
        TwinTowersPlaneEvent.setTowersOrigin(testPos);
        assertEquals(testPos, TwinTowersPlaneEvent.getTowersOrigin());

        TwinTowersPlaneEvent.stopEvent();
        assertFalse(TwinTowersPlaneEvent.isActive());
    }
}

