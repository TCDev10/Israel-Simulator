package com.israelsimulator.event.world.plane;

import com.israelsimulator.entity.event.PlaneCrashEntity;
import com.israelsimulator.entity.event.PlaneModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
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

    @Test
    @DisplayName("Plane max flight duration before despawn is 30 seconds (600 ticks)")
    void testMaxFlightDuration() {
        assertEquals(30 * 20, PlaneCrashEntity.MAX_FLIGHT_TICKS);
        assertEquals(600, PlaneCrashEntity.MAX_FLIGHT_TICKS);
    }
    @Test
    @DisplayName("Tower boxes follow the template rotation and stay inside the structure")
    void testTowerBoxesRotate() {
        BlockPos origin = new BlockPos(1000, 70, -500);
        for (Rotation rotation : Rotation.values()) {
            TwinTowersPlaneEvent.Towers t = TwinTowersPlaneEvent.fromLayout(1L, origin, rotation);
            assertEquals(2, t.towers().length);
            for (BoundingBox tower : t.towers()) {
                assertEquals(25, tower.getXSpan(), rotation + " tower is 25 blocks wide");
                assertEquals(25, tower.getZSpan(), rotation + " tower is 25 blocks deep");
                assertTrue(t.structureBox().isInside(tower.minX(), tower.minY(), tower.minZ()), rotation + " inside structure");
                assertTrue(t.structureBox().isInside(tower.maxX(), tower.maxY(), tower.maxZ()), rotation + " inside structure");
            }
            assertFalse(t.towers()[0].intersects(t.towers()[1]), rotation + " towers are separate");
        }
        TwinTowersPlaneEvent.Towers none = TwinTowersPlaneEvent.fromLayout(1L, origin, Rotation.NONE);
        assertEquals(new BoundingBox(1005, 70, -495, 1029, 247, -471), none.towers()[0]);
    }

    @Test
    @DisplayName("Target is the tower's horizontal center between 40% and 85% of its height")
    void testTowerTarget() {
        BoundingBox tower = new BoundingBox(0, 70, 0, 24, 247, 24);
        Vec3 low = TwinTowersPlaneEvent.towerTarget(tower, TwinTowersPlaneEvent.HEIGHT_FRACTION_MIN);
        Vec3 high = TwinTowersPlaneEvent.towerTarget(tower, TwinTowersPlaneEvent.HEIGHT_FRACTION_MAX);
        assertEquals(12.5, low.x, 1e-9);
        assertEquals(12.5, low.z, 1e-9);
        assertEquals(70 + 178 * 0.40, low.y, 1e-9);
        assertEquals(70 + 178 * 0.85, high.y, 1e-9);
    }

    @Test
    @DisplayName("Plane spawns outside the whole structure, 48-96 blocks beyond its half-diagonal")
    void testSpawnRingOutsideStructure() {
        BoundingBox box = new BoundingBox(0, 70, 0, 73, 265, 70);
        double halfDiagonal = Math.sqrt(37.0 * 37.0 + 35.5 * 35.5);
        assertEquals(halfDiagonal + 48.0, TwinTowersPlaneEvent.ringDistance(box, 0.0), 1e-9);
        assertEquals(halfDiagonal + 96.0, TwinTowersPlaneEvent.ringDistance(box, 1.0), 1e-9);
        assertTrue(TwinTowersPlaneEvent.horizontalDistanceToBox(box, 37.0 + TwinTowersPlaneEvent.ringDistance(box, 0.0), 35.5) >= 48.0);
    }

    @Test
    @DisplayName("Approach path check detects a crossing of the other tower")
    void testSegmentCrossesBox() {
        BoundingBox other = new BoundingBox(40, 0, 40, 64, 200, 64);
        Vec3 target = new Vec3(12.5, 100, 12.5);
        assertTrue(TwinTowersPlaneEvent.segmentCrossesBox2D(new Vec3(150, 110, 150), target, other, 18.0));
        assertFalse(TwinTowersPlaneEvent.segmentCrossesBox2D(new Vec3(-120, 110, 12.5), target, other, 18.0));
    }
}
