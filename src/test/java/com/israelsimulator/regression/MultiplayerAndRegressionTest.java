package com.israelsimulator.regression;

import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.core.authority.ServerAuthorityValidator;
import com.israelsimulator.core.data.RarityLevel;
import com.israelsimulator.easteregg.EasterEggManager;
import com.israelsimulator.economy.CityRegion;
import com.israelsimulator.economy.IsraelEconomy;
import com.israelsimulator.economy.ProductCategory;
import com.israelsimulator.entity.boss.BibiBossState;
import com.israelsimulator.performance.PerformanceManager;
import com.israelsimulator.quest.CompletedQuests;
import com.israelsimulator.quest.DiscoveryQuest;
import com.israelsimulator.quest.QuestManager;
import com.israelsimulator.reputation.ReputationFaction;
import com.israelsimulator.reputation.ReputationScores;
import com.israelsimulator.reputation.ReputationManager;
import com.israelsimulator.transport.TransportNetwork;
import com.israelsimulator.transport.TransportType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Master regression test suite per GAME_DESIGN.md §61 and §62.
 * Verifies that all implemented systems remain coherent, non-regressed,
 * and multiplayer-safe.
 */
public class MultiplayerAndRegressionTest {

    @Test
    public void testRegistryAndDomainRarityRegression() {
        assertEquals(6, RarityLevel.values().length, "RarityLevel must maintain all 6 tiers");
        assertTrue(RarityLevel.MYTHIC.isAtLeast(RarityLevel.COMMON));
        assertTrue(RarityLevel.LEGENDARY.isAtLeast(RarityLevel.EPIC));
    }

    @Test
    public void testEconomyAndArbitrageRegression() {
        for (CityRegion region : CityRegion.values()) {
            long buy = IsraelEconomy.calculateBuyPrice("falafel", region, 0);
            long sell = IsraelEconomy.calculateSellPrice("falafel", region, 0);
            assertTrue(buy > sell, "Bid-ask spread must be maintained in region: " + region);
            assertTrue(buy >= 1, "Price floor must be at least 1 in region: " + region);
        }
    }

    @Test
    public void testBossCombatStateMachineRegression() {
        assertEquals(6, BibiBossState.values().length, "BibiBossState must contain IDLE, ALERT, COMBAT, ENRAGED, DESPERATE, DEFEATED");
        assertNotNull(BibiBossState.IDLE);
        assertNotNull(BibiBossState.ALERT);
        assertNotNull(BibiBossState.COMBAT);
        assertNotNull(BibiBossState.ENRAGED);
        assertNotNull(BibiBossState.DESPERATE);
        assertNotNull(BibiBossState.DEFEATED);
    }

    @Test
    public void testQuestAndReputationMultiplayerRegression() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        // Independent reputation tracking on saved data (not a shared static map)
        ReputationScores scores = new ReputationScores();
        ReputationManager.setReputation(scores, player1, ReputationFaction.CITY, 50);
        ReputationManager.setReputation(scores, player2, ReputationFaction.CITY, -20);

        assertEquals(50, ReputationManager.getReputation(scores, player1, ReputationFaction.CITY));
        assertEquals(-20, ReputationManager.getReputation(scores, player2, ReputationFaction.CITY));

        // Independent quest state on saved data
        CompletedQuests quests = new CompletedQuests();
        assertFalse(QuestManager.isCompleted(quests, player1, DiscoveryQuest.HOLY_CITY_PILGRIM));
        assertFalse(QuestManager.isCompleted(quests, player2, DiscoveryQuest.HOLY_CITY_PILGRIM));
    }

    @Test
    public void testTransportNetworkConnectivityRegression() {
        var stops = TransportNetwork.getAllStops();
        assertNotNull(stops, "Transport network stops must be defined");
        assertFalse(stops.isEmpty(), "Transport stops must be registered");

        // Verify transport types
        for (TransportType type : TransportType.values()) {
            assertNotNull(type.getDisplayName(), "TransportType must have display name: " + type);
            assertTrue(type.getFareInShekels() >= 0, "Base fare must be non-negative: " + type);
        }
    }

    @Test
    public void testEasterEggSafeProgressionRegression() {
        var quotes = EasterEggManager.getAbsurdDialogues();
        assertNotNull(quotes);
        assertFalse(quotes.isEmpty());
        for (String q : quotes) {
            assertFalse(q.isBlank(), "Easter egg dialogue cannot be blank");
        }
    }

    @Test
    public void testPerformanceAndConfigRegression() {
        assertTrue(IsraelSimulatorConfig.maxNpcPerChunk() > 0);
        assertTrue(IsraelSimulatorConfig.eventDurationTicks() > 0);
        assertEquals(30, PerformanceManager.clampParticleCount(100), "Particle count must be clamped to safe limit");
    }

    @Test
    public void testServerAuthorityValidationRegression() {
        assertTrue(ServerAuthorityValidator.isValidCurrencyAmount(50));
        assertFalse(ServerAuthorityValidator.isValidCurrencyAmount(-50));
        assertFalse(ServerAuthorityValidator.isValidCurrencyAmount(0));
        assertTrue(ServerAuthorityValidator.isValidTradeOffer(10, 1));
        assertFalse(ServerAuthorityValidator.isValidTradeOffer(0, 1));
    }
}
