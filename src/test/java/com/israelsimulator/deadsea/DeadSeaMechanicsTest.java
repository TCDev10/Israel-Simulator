package com.israelsimulator.deadsea;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeadSeaMechanicsTest {

    @Test
    @DisplayName("Dead Sea buoyancy lifts non-crouching entity upward")
    void testBuoyancyLiftsEntity() {
        double currentVy = 0.0;
        double newVy = DeadSeaMechanics.calculateBuoyancyVelocity(currentVy, false);
        assertTrue(newVy > currentVy, "Buoyancy must provide positive upward vertical lift");
        assertEquals(DeadSeaMechanics.BUOYANCY_LIFT, newVy, 0.001);
    }

    @Test
    @DisplayName("Dead Sea buoyancy respects maximum vertical lift ceiling")
    void testBuoyancyCeiling() {
        double highVy = 0.10;
        double cappedVy = DeadSeaMechanics.calculateBuoyancyVelocity(highVy, false);
        assertEquals(highVy, cappedVy, "Velocity above max buoyancy ceiling should not be inflated further");

        double nearCeiling = 0.07;
        double clampedVy = DeadSeaMechanics.calculateBuoyancyVelocity(nearCeiling, false);
        assertEquals(DeadSeaMechanics.MAX_BUOYANT_VELOCITY, clampedVy, 0.001);
    }

    @Test
    @DisplayName("Dead Sea buoyancy permits descending when crouching")
    void testBuoyancyAllowsDivingWhenCrouching() {
        double currentVy = -0.05;
        double diveVy = DeadSeaMechanics.calculateBuoyancyVelocity(currentVy, true);
        assertTrue(diveVy <= currentVy, "Crouching should permit entity to swim or dive downwards");
    }
}
