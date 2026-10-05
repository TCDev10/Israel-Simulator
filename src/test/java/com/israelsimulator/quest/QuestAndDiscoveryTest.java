package com.israelsimulator.quest;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuestAndDiscoveryTest {

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
        CompletedQuests data = new CompletedQuests();
        assertFalse(QuestManager.isCompleted(data, testPlayer, DiscoveryQuest.HOLY_CITY_PILGRIM));
        assertTrue(QuestManager.getCompletedQuests(data, testPlayer).isEmpty());
    }

    @Test
    @DisplayName("Completed quests are saved data, not a static map, and survive a codec reload")
    void testCompletedQuestsPersistThroughCodec() throws Exception {
        for (Class<?> type : java.util.List.of(QuestManager.class, CompletedQuests.class)) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())) {
                    fail(type.getSimpleName() + " must not keep completed quests in a static map: " + field.getName());
                }
            }
        }

        UUID playerId = UUID.randomUUID();
        DiscoveryQuest quest = DiscoveryQuest.HOLY_CITY_PILGRIM;
        CompletedQuests live = new CompletedQuests();
        CompletedQuests other = new CompletedQuests();
        assertTrue(live.markCompleted(playerId, quest));
        assertFalse(live.markCompleted(playerId, quest));
        assertTrue(QuestManager.isCompleted(live, playerId, quest));
        assertFalse(QuestManager.isCompleted(other, playerId, quest));

        net.minecraft.nbt.Tag encoded = CompletedQuests.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, live)
                .getOrThrow();
        CompletedQuests restored = CompletedQuests.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();

        assertNotSame(live, restored);
        assertTrue(QuestManager.isCompleted(restored, playerId, quest));
        assertTrue(QuestManager.getCompletedQuests(restored, playerId).contains(quest));
        assertFalse(QuestManager.isCompleted(restored, playerId, DiscoveryQuest.DEAD_SEA_MINERALIST));
        assertEquals("israel_simulator:completed_quests", CompletedQuests.TYPE.id().toString());
    }
}
