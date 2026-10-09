package com.israelsimulator.event.world.speech;

import com.israelsimulator.entity.boss.ArenaRestoreRules;
import com.israelsimulator.event.world.WorldEventManager;
import com.israelsimulator.event.world.WorldEventType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongFunction;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PublicSpeechEventTest {

    @BeforeEach
    void setup() {
        WorldEventManager.clearAll();
    }

    @Test
    void participationTimerIsContinuousAndResetsWhenLeaving() {
        int t = 0;
        for (int i = 0; i < 30; i++) t = SpeechParticipation.nextTicks(t, true, 20);
        assertEquals(600, t);
        t = SpeechParticipation.nextTicks(t, false, 20);
        assertEquals(0, t, "walking away restarts the 60 s");
        for (int i = 0; i < 59; i++) t = SpeechParticipation.nextTicks(t, true, 20);
        assertFalse(SpeechParticipation.shouldReward(t, 1200, false));
        t = SpeechParticipation.nextTicks(t, true, 20);
        assertTrue(SpeechParticipation.shouldReward(t, 1200, false), "60 continuous seconds earn the reward");
        assertTrue(SpeechParticipation.inRange(16 * 16));
        assertFalse(SpeechParticipation.inRange(16.1 * 16.1));
        assertEquals(0.5F, SpeechParticipation.progress(600, 1200), 1e-6);
    }

    @Test
    void rewardIsGivenOncePerEvent() {
        UUID p = UUID.randomUUID();
        WorldEventManager.startEvent(WorldEventType.PUBLIC_SPEECH, BlockPos.ZERO, 0L);
        WorldEventManager.ActiveEventData data = WorldEventManager.getEventData(WorldEventType.PUBLIC_SPEECH);
        data.setTicks(p, 1200);
        assertTrue(WorldEventManager.claimReward(WorldEventType.PUBLIC_SPEECH, p));
        data.setTicks(p, 5000);
        assertFalse(WorldEventManager.claimReward(WorldEventType.PUBLIC_SPEECH, p), "no second First Amendment");
        assertFalse(SpeechParticipation.shouldReward(5000, 1200, data.hasBeenRewarded(p)));
        // A new event is a new chance.
        WorldEventManager.endEvent(WorldEventType.PUBLIC_SPEECH);
        WorldEventManager.startEvent(WorldEventType.PUBLIC_SPEECH, BlockPos.ZERO, 3000L);
        WorldEventManager.getEventData(WorldEventType.PUBLIC_SPEECH).setTicks(p, 1200);
        assertTrue(WorldEventManager.claimReward(WorldEventType.PUBLIC_SPEECH, p));
    }

    @Test
    void stageStaysInsideTheSnapshotFootprintAndHasAllProps() {
        Set<String> kinds = new HashSet<>();
        for (SpeechStageLayout.Placement p : SpeechStageLayout.placements()) {
            assertTrue(SpeechStageLayout.inFootprint(p.dx(), p.dy(), p.dz()), "placement outside snapshot: " + p);
            kinds.add(p.kind().name());
        }
        for (SpeechStageLayout.Kind k : SpeechStageLayout.Kind.values()) assertTrue(kinds.contains(k.name()), k + " missing");
        assertEquals(9 * 11 * 10, SpeechStageLayout.footprint().size());
        for (int[] c : SpeechStageLayout.crowdSpots())
            assertTrue(SpeechStageLayout.inFootprint(c[0], c[1], c[2]));
        assertTrue(SpeechStageLayout.inFootprint(SpeechStageLayout.ORATOR_X, SpeechStageLayout.ORATOR_Y, SpeechStageLayout.ORATOR_Z));
    }

    @Test
    void restorePutsBackOriginalsExceptPlayerEdits() {
        // Original "stone" everywhere; stage places "stage"; a player then replaces one block with "dirt".
        List<ArenaRestoreRules.Entry<String>> entries = new ArrayList<>();
        for (long pos = 0; pos < 5; pos++) {
            assertTrue(ArenaRestoreRules.shouldRecord("stone", "stage", false));
            entries.add(new ArenaRestoreRules.Entry<>(pos, "stone", "stage"));
        }
        assertFalse(ArenaRestoreRules.shouldRecord("stone", "stone", false), "untouched blocks are not stored");
        LongFunction<String> world = pos -> pos == 2 ? "dirt" : "stage";
        ArenaRestoreRules.Plan<String> plan = ArenaRestoreRules.plan(entries, world);
        assertEquals(4, plan.restore().size());
        assertEquals(1, plan.skipped().size());
        assertTrue(ArenaRestoreRules.restoresOnRemoval(true), "event end discards the Orator, which restores");
        assertFalse(ArenaRestoreRules.restoresOnRemoval(false), "chunk unload keeps the stage");
    }

    @Test
    void schedulerRespectsCooldownAndActiveEvent() {
        assertTrue(SpeechParticipation.canSchedule(100, -1, 12000, false));
        assertFalse(SpeechParticipation.canSchedule(100, -1, 12000, true));
        assertFalse(SpeechParticipation.canSchedule(5000, 1000, 12000, false));
        assertTrue(SpeechParticipation.canSchedule(13000, 1000, 12000, false));
    }
}
