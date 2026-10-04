package com.israelsimulator.desert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DesertHazardTest {

    @Test
    @DisplayName("Player is vulnerable to heat during daytime in open sunlight without headgear")
    void testVulnerableWhenDaySkyNoHat() {
        boolean vulnerable = DesertHazards.isVulnerableToHeat(true, true, false);
        assertTrue(vulnerable, "Unsheltered player under direct daytime desert sun should be vulnerable");
    }

    @Test
    @DisplayName("Headgear (such as Kippah or helmet) protects from desert heat")
    void testProtectedByHat() {
        boolean vulnerable = DesertHazards.isVulnerableToHeat(true, true, true);
        assertFalse(vulnerable, "Wearing headgear should completely protect from direct solar heat exhaustion");
    }

    @Test
    @DisplayName("Nighttime eliminates heat exhaustion hazard")
    void testProtectedAtNight() {
        boolean vulnerable = DesertHazards.isVulnerableToHeat(false, true, false);
        assertFalse(vulnerable, "Nighttime should not trigger daytime solar heat exhaustion");
    }

    @Test
    @DisplayName("Roof or cave shelter eliminates direct solar heat exhaustion")
    void testProtectedUnderRoof() {
        boolean vulnerable = DesertHazards.isVulnerableToHeat(true, false, false);
        assertFalse(vulnerable, "Shade or cave cover should eliminate direct solar heat exhaustion");
    }
}
