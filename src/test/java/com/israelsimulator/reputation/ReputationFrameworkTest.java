package com.israelsimulator.reputation;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReputationFrameworkTest {

    @AfterEach
    void tearDown() {
        ReputationManager.clearAll();
    }

    @Test
    @DisplayName("Verify all five reputation factions exist")
    void testFactions() {
        ReputationFaction[] factions = ReputationFaction.values();
        assertEquals(5, factions.length);
        assertNotNull(ReputationFaction.MERCHANT);
        assertNotNull(ReputationFaction.CITY);
        assertNotNull(ReputationFaction.VILLAGE);
        assertNotNull(ReputationFaction.RELIGIOUS);
        assertNotNull(ReputationFaction.TECH_DISTRICT);
    }

    @Test
    @DisplayName("Verify reputation tier transitions and boundaries")
    void testReputationTiers() {
        assertEquals(ReputationTier.EXILED, ReputationTier.fromScore(-100));
        assertEquals(ReputationTier.EXILED, ReputationTier.fromScore(-65));

        assertEquals(ReputationTier.SUSPICIOUS, ReputationTier.fromScore(-60));
        assertEquals(ReputationTier.SUSPICIOUS, ReputationTier.fromScore(-1));

        assertEquals(ReputationTier.NEUTRAL, ReputationTier.fromScore(0));
        assertEquals(ReputationTier.NEUTRAL, ReputationTier.fromScore(19));

        assertEquals(ReputationTier.RESPECTED, ReputationTier.fromScore(20));
        assertEquals(ReputationTier.RESPECTED, ReputationTier.fromScore(59));

        assertEquals(ReputationTier.HONORED, ReputationTier.fromScore(60));
        assertEquals(ReputationTier.HONORED, ReputationTier.fromScore(89));

        assertEquals(ReputationTier.CHAMPION, ReputationTier.fromScore(90));
        assertEquals(ReputationTier.CHAMPION, ReputationTier.fromScore(100));
    }

    @Test
    @DisplayName("Verify reputation perks and access restrictions per tier")
    void testTierPerksAndAccess() {
        // Exiled: surcharge, no special or rare access
        ReputationTier exiled = ReputationTier.EXILED;
        assertTrue(exiled.getPriceModifier() > 0);
        assertFalse(exiled.canAccessSpecialTrades());
        assertFalse(exiled.canAccessRareTrades());

        // Respected: discount, special trade access, no rare access yet
        ReputationTier respected = ReputationTier.RESPECTED;
        assertTrue(respected.getPriceModifier() < 0);
        assertTrue(respected.canAccessSpecialTrades());
        assertFalse(respected.canAccessRareTrades());

        // Honored: larger discount, both special and rare access
        ReputationTier honored = ReputationTier.HONORED;
        assertTrue(honored.getPriceModifier() < respected.getPriceModifier());
        assertTrue(honored.canAccessSpecialTrades());
        assertTrue(honored.canAccessRareTrades());
    }

    @Test
    @DisplayName("Verify ReputationManager score adjustments and clamping")
    void testReputationManagerClamping() {
        UUID player = UUID.randomUUID();
        ReputationFaction faction = ReputationFaction.TECH_DISTRICT;

        assertEquals(0, ReputationManager.getReputation(player, faction));

        ReputationManager.adjustReputation(player, faction, 45);
        assertEquals(45, ReputationManager.getReputation(player, faction));
        assertEquals(ReputationTier.RESPECTED, ReputationManager.getTier(player, faction));

        ReputationManager.adjustReputation(player, faction, 200);
        assertEquals(100, ReputationManager.getReputation(player, faction), "Must clamp to 100 max");
        assertEquals(ReputationTier.CHAMPION, ReputationManager.getTier(player, faction));

        ReputationManager.adjustReputation(player, faction, -300);
        assertEquals(-100, ReputationManager.getReputation(player, faction), "Must clamp to -100 min");
        assertEquals(ReputationTier.EXILED, ReputationManager.getTier(player, faction));
    }
}
