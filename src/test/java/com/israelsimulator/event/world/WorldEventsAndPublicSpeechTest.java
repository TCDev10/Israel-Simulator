package com.israelsimulator.event.world;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldEventsAndPublicSpeechTest {

    @BeforeEach
    void setup() {
        WorldEventManager.clearAll();
    }

    @Test
    @DisplayName("Verify all 10 WorldEventType enum constants exist and have valid configuration")
    void testAllWorldEventTypes() {
        assertEquals(10, WorldEventType.values().length, "There must be exactly 10 world event types");

        for (WorldEventType type : WorldEventType.values()) {
            assertNotNull(type.getId(), "Event type id cannot be null");
            assertFalse(type.getId().isBlank(), "Event type id cannot be blank");
            assertNotNull(type.getDisplayName(), "Event display name cannot be null");
            assertNotNull(type.getDescription(), "Event description cannot be null");
            assertTrue(type.getDurationTicks() > 0, "Event duration must be > 0");
            assertTrue(type.getMinParticipationTicks() > 0, "Min participation ticks must be > 0");
            assertTrue(type.getMinParticipationTicks() <= type.getDurationTicks(),
                    "Min participation cannot exceed total event duration");
        }

        assertNotNull(WorldEventType.valueOf("PUBLIC_SPEECH"));
        assertNotNull(WorldEventType.valueOf("MARKET_DAY"));
        assertNotNull(WorldEventType.valueOf("FESTIVAL"));
        assertNotNull(WorldEventType.valueOf("CONCERT"));
        assertNotNull(WorldEventType.valueOf("BEACH_EVENT"));
        assertNotNull(WorldEventType.valueOf("RELIGIOUS_EVENT"));
        assertNotNull(WorldEventType.valueOf("FOOD_FESTIVAL"));
        assertNotNull(WorldEventType.valueOf("TECHNOLOGY_CONFERENCE"));
        assertNotNull(WorldEventType.valueOf("RARE_NPC_SPAWN"));
        assertNotNull(WorldEventType.valueOf("BOSS_EVENT"));
    }

    @Test
    @DisplayName("Verify Public Speech configuration matches GAME_DESIGN.md and TODO §38 requirements")
    void testPublicSpeechEventRequirements() {
        WorldEventType speech = WorldEventType.PUBLIC_SPEECH;
        assertEquals("public_speech", speech.getId());
        assertEquals(2400L, speech.getDurationTicks(), "Public speech duration should be 2400 ticks (2 min)");
        assertEquals(1200L, speech.getMinParticipationTicks(), "Minimum participation must be 1200 ticks (60s)");
    }

    @Test
    @DisplayName("Verify WorldEventStatus enum values")
    void testWorldEventStatus() {
        assertEquals(4, WorldEventStatus.values().length);
        assertNotNull(WorldEventStatus.valueOf("SCHEDULED"));
        assertNotNull(WorldEventStatus.valueOf("ACTIVE"));
        assertNotNull(WorldEventStatus.valueOf("COMPLETED"));
        assertNotNull(WorldEventStatus.valueOf("COOLDOWN"));
    }

    @Test
    @DisplayName("Verify WorldEventManager lifecycle: start, track participation, claim reward, and conclude")
    void testWorldEventManagerLifecycle() {
        BlockPos speechPos = new BlockPos(100, 64, 200);
        long startTime = 5000L;
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        assertFalse(WorldEventManager.isEventActive(WorldEventType.PUBLIC_SPEECH));

        WorldEventManager.startEvent(WorldEventType.PUBLIC_SPEECH, speechPos, startTime);
        assertTrue(WorldEventManager.isEventActive(WorldEventType.PUBLIC_SPEECH));

        WorldEventManager.ActiveEventData data = WorldEventManager.getEventData(WorldEventType.PUBLIC_SPEECH);
        assertNotNull(data);
        assertEquals(speechPos, data.getCenter());
        assertEquals(startTime, data.getStartTick());
        assertEquals(startTime + 2400L, data.getEndTick());
        assertEquals(WorldEventStatus.ACTIVE, data.getStatus());

        // Player 1 participates for 600 ticks (30s) -> insufficient for 60s reward
        WorldEventManager.recordParticipation(WorldEventType.PUBLIC_SPEECH, player1, 600);
        assertEquals(600, WorldEventManager.getParticipationTicks(WorldEventType.PUBLIC_SPEECH, player1));
        assertFalse(WorldEventManager.canClaimReward(WorldEventType.PUBLIC_SPEECH, player1));
        assertFalse(WorldEventManager.claimReward(WorldEventType.PUBLIC_SPEECH, player1));

        // Player 1 stays for another 600 ticks (total 1200 ticks / 60s)
        WorldEventManager.recordParticipation(WorldEventType.PUBLIC_SPEECH, player1, 600);
        assertEquals(1200, WorldEventManager.getParticipationTicks(WorldEventType.PUBLIC_SPEECH, player1));
        assertTrue(WorldEventManager.canClaimReward(WorldEventType.PUBLIC_SPEECH, player1));

        // Claim reward
        assertTrue(WorldEventManager.claimReward(WorldEventType.PUBLIC_SPEECH, player1));

        // Anti-farming & anti-exploit check: Player 1 cannot claim again
        assertFalse(WorldEventManager.canClaimReward(WorldEventType.PUBLIC_SPEECH, player1));
        assertFalse(WorldEventManager.claimReward(WorldEventType.PUBLIC_SPEECH, player1));

        // Multiplayer isolation: Player 2 joins and participates for 1500 ticks
        WorldEventManager.recordParticipation(WorldEventType.PUBLIC_SPEECH, player2, 1500);
        assertTrue(WorldEventManager.canClaimReward(WorldEventType.PUBLIC_SPEECH, player2));
        assertTrue(WorldEventManager.claimReward(WorldEventType.PUBLIC_SPEECH, player2));
        assertFalse(WorldEventManager.canClaimReward(WorldEventType.PUBLIC_SPEECH, player2));

        // End event
        WorldEventManager.endEvent(WorldEventType.PUBLIC_SPEECH);
        assertFalse(WorldEventManager.isEventActive(WorldEventType.PUBLIC_SPEECH));
        assertNull(WorldEventManager.getEventData(WorldEventType.PUBLIC_SPEECH));
    }

    @Test
    @DisplayName("Verify FirstAmendmentItem class exists and extends Item")
    void testFirstAmendmentItemClassStructure() throws Exception {
        ClassLoader cl = WorldEventsAndPublicSpeechTest.class.getClassLoader();
        Class<?> firstAmendmentClass = Class.forName("com.israelsimulator.item.cultural.FirstAmendmentItem", false, cl);
        assertNotNull(firstAmendmentClass);
        assertEquals("net.minecraft.world.item.Item", firstAmendmentClass.getSuperclass().getName());
    }

    @Test
    @DisplayName("Verify First Amendment item model JSON exists and references item texture")
    void testFirstAmendmentModelJson() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/models/item/first_amendment.json");
        assertNotNull(stream, "models/item/first_amendment.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:item/first_amendment"), "Model must reference first_amendment texture");
        } catch (Exception e) {
            fail("Failed reading first_amendment model JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify First Amendment texture file exists and is non-empty")
    void testFirstAmendmentTextureFile() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/textures/item/first_amendment.png");
        assertNotNull(stream, "textures/item/first_amendment.png must exist");
        try {
            byte[] bytes = stream.readAllBytes();
            assertTrue(bytes.length > 0, "Texture file must not be empty");
        } catch (Exception e) {
            fail("Failed reading first amendment texture: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Public Speech and First Amendment translations exist in en_us.json")
    void testEventAndFirstAmendmentTranslations() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"item.israel_simulator.first_amendment\""), "Missing first_amendment item key");
            assertTrue(json.contains("\"item.israel_simulator.first_amendment.desc\""), "Missing first_amendment desc key");
            assertTrue(json.contains("\"item.israel_simulator.first_amendment.effect\""), "Missing first_amendment effect key");
            assertTrue(json.contains("\"message.israel_simulator.first_amendment_used\""), "Missing first_amendment used key");
            assertTrue(json.contains("\"message.israel_simulator.public_speech_start\""), "Missing public speech start key");
            assertTrue(json.contains("\"message.israel_simulator.public_speech_reward\""), "Missing public speech reward key");
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }
}

