package com.israelsimulator.reputation;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReputationFrameworkTest {

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
        ReputationTier exiled = ReputationTier.EXILED;
        assertTrue(exiled.getPriceModifier() > 0);
        assertFalse(exiled.canAccessSpecialTrades());
        assertFalse(exiled.canAccessRareTrades());

        ReputationTier respected = ReputationTier.RESPECTED;
        assertTrue(respected.getPriceModifier() < 0);
        assertTrue(respected.canAccessSpecialTrades());
        assertFalse(respected.canAccessRareTrades());

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
        ReputationScores scores = new ReputationScores();

        assertEquals(0, ReputationManager.getReputation(scores, player, faction));

        ReputationManager.adjustReputation(scores, player, faction, 45);
        assertEquals(45, ReputationManager.getReputation(scores, player, faction));
        assertEquals(ReputationTier.RESPECTED, ReputationManager.getTier(scores, player, faction));

        ReputationManager.adjustReputation(scores, player, faction, 200);
        assertEquals(100, ReputationManager.getReputation(scores, player, faction), "Must clamp to 100 max");
        assertEquals(ReputationTier.CHAMPION, ReputationManager.getTier(scores, player, faction));

        ReputationManager.adjustReputation(scores, player, faction, -300);
        assertEquals(-100, ReputationManager.getReputation(scores, player, faction), "Must clamp to -100 min");
        assertEquals(ReputationTier.EXILED, ReputationManager.getTier(scores, player, faction));
    }

    @Test
    @DisplayName("Reputation is saved data, not a static map, and survives a codec reload")
    void testReputationPersistsThroughCodec() throws Exception {
        for (Class<?> type : java.util.List.of(ReputationManager.class, ReputationScores.class)) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())) {
                    fail(type.getSimpleName() + " must not keep reputation in a static map: " + field.getName());
                }
            }
        }

        UUID playerId = UUID.randomUUID();
        ReputationFaction faction = ReputationFaction.CITY;
        ReputationScores live = new ReputationScores();
        ReputationScores other = new ReputationScores();
        ReputationManager.setReputation(live, playerId, faction, 42);
        assertEquals(42, ReputationManager.getReputation(live, playerId, faction));
        assertEquals(0, ReputationManager.getReputation(other, playerId, faction));

        net.minecraft.nbt.Tag encoded = ReputationScores.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, live)
                .getOrThrow();
        ReputationScores restored = ReputationScores.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();

        assertNotSame(live, restored);
        assertEquals(42, ReputationManager.getReputation(restored, playerId, faction));
        assertEquals(ReputationTier.RESPECTED, ReputationManager.getTier(restored, playerId, faction));
        assertEquals("israel_simulator:reputation_scores", ReputationScores.TYPE.id().toString());
    }
}
