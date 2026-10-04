package com.israelsimulator.quest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class QuestAndDiscoveryTest {

    @BeforeEach
    public void setup() {
        QuestManager.resetForTesting();
    }

    @Test
    public void testDiscoveryQuestsIntegrity() {
        for (DiscoveryQuest quest : DiscoveryQuest.values()) {
            assertNotNull(quest.getTitle(), "Quest title must not be null");
            assertFalse(quest.getTitle().isBlank(), "Quest title must not be blank");
            assertNotNull(quest.getDescription(), "Quest description must not be null");
            assertFalse(quest.getDescription().isBlank(), "Quest description must not be blank");
            assertNotNull(quest.getCategory(), "Quest category must not be null");
            assertNotNull(quest.getRewardFaction(), "Quest reward faction must not be null");
            assertTrue(quest.getReputationReward() > 0, "Reputation reward must be positive");
            assertTrue(quest.getRewardAmount() > 0, "Currency reward must be positive");
        }
    }

    @Test
    public void testQuestManagerCompletionState() {
        UUID testPlayer = UUID.randomUUID();
        assertFalse(QuestManager.isCompleted(testPlayer, DiscoveryQuest.HOLY_CITY_PILGRIM));

        // Initial completion count is 0
        assertTrue(QuestManager.getCompletedQuests(testPlayer).isEmpty());
    }
}
