package com.israelsimulator.npc;

import com.israelsimulator.npc.schedule.NpcSchedule;
import com.israelsimulator.npc.schedule.ScheduleState;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NpcFrameworkAndScheduleTest {

    @Test
    @DisplayName("Verify all 15 functional NPC professions are present and well-formed")
    void testFifteenProfessions() {
        NpcProfession[] professions = NpcProfession.values();
        assertEquals(15, professions.length, "Must define exactly 15 functional NPC professions");

        for (NpcProfession p : professions) {
            assertNotNull(p.getId());
            assertNotNull(p.getDisplayName());
            assertNotNull(p.getPrimaryCategory());
            assertNotNull(p.getPreferredRegion());
            assertFalse(p.getGreetings().isEmpty(), "Every profession must have dialogue greetings");
            assertNotNull(p.getShabbatGreeting(), "Every profession must have a Shabbat greeting");
        }
    }

    @Test
    @DisplayName("Verify daily schedule timeline transitions")
    void testDailyScheduleTimeline() {
        NpcProfession dev = NpcProfession.DEVELOPER;

        assertEquals(ScheduleState.WAKE, NpcSchedule.getScheduleState(0L, false, dev));
        assertEquals(ScheduleState.WORK_MORNING, NpcSchedule.getScheduleState(3000L, false, dev));
        assertEquals(ScheduleState.LUNCH, NpcSchedule.getScheduleState(6500L, false, dev));
        assertEquals(ScheduleState.WORK_AFTERNOON, NpcSchedule.getScheduleState(9000L, false, dev));
        assertEquals(ScheduleState.SOCIAL, NpcSchedule.getScheduleState(13000L, false, dev));
        assertEquals(ScheduleState.HOME, NpcSchedule.getScheduleState(17000L, false, dev));
        assertEquals(ScheduleState.SLEEP, NpcSchedule.getScheduleState(20000L, false, dev));
    }

    @Test
    @DisplayName("Verify Shabbat behavior for observant and non-observant professions")
    void testShabbatScheduleOverrides() {
        NpcProfession rabbi = NpcProfession.RABBI;
        assertTrue(rabbi.isObservantShabbat());

        // During Shabbat morning service (tick 3000)
        assertEquals(ScheduleState.SHABBAT_PRAYER, NpcSchedule.getScheduleState(3000L, true, rabbi));
        // During Shabbat afternoon rest
        assertEquals(ScheduleState.SHABBAT_REST, NpcSchedule.getScheduleState(7000L, true, rabbi));

        // Commercial trading is strictly prohibited for observant NPCs during Shabbat
        assertFalse(NpcSchedule.canNpcTrade(3000L, true, rabbi));
        assertFalse(NpcSchedule.canNpcTrade(7000L, true, rabbi));
    }

    @Test
    @DisplayName("Verify Shabbat weekly cycle calculation (Friday sunset to Saturday night)")
    void testShabbatWeeklyCycle() {
        // Weekday (Day 1 at noon = 1 * 24000 + 6000 = 30000)
        assertFalse(NpcSchedule.isShabbat(30000L));

        // Friday sunset (Day 5 at tick 12000 = 5 * 24000 + 12000 = 132000)
        assertTrue(NpcSchedule.isShabbat(132000L));
        // Saturday morning (Day 6 at tick 4000 = 6 * 24000 + 4000 = 148000)
        assertTrue(NpcSchedule.isShabbat(148000L));
        // Sunday morning (Day 0 / week rollover = 169000 % 168000 = 1000)
        assertFalse(NpcSchedule.isShabbat(169000L));
    }

    @Test
    @DisplayName("Verify IsraelNpcData reputation and daily trade limits")
    void testNpcDataReputationAndTradingLimits() {
        UUID id = UUID.randomUUID();
        IsraelNpcData data = new IsraelNpcData(id, NpcProfession.MERCHANT,
                new BlockPos(10, 64, 10), new BlockPos(20, 64, 20), new BlockPos(30, 64, 30));

        assertEquals(0, data.getReputation());
        data.adjustReputation(15);
        assertEquals(15, data.getReputation());
        data.adjustReputation(200);
        assertEquals(100, data.getReputation(), "Reputation must clamp to 100 max");
        data.adjustReputation(-250);
        assertEquals(-100, data.getReputation(), "Reputation must clamp to -100 min");

        // Daily trade limits
        assertTrue(data.canTradeToday());
        for (int i = 0; i < 16; i++) {
            data.recordTrade(1000L);
        }
        assertFalse(data.canTradeToday(), "NPC must refuse trades after reaching daily 16 trade cap");

        // Next day resets trade cap
        data.recordTrade(1000L + 25000L);
        assertTrue(data.canTradeToday());
    }

    @Test
    @DisplayName("Verify NPC target location resolves according to schedule state")
    void testNpcTargetLocation() {
        BlockPos home = new BlockPos(10, 64, 10);
        BlockPos work = new BlockPos(20, 64, 20);
        BlockPos social = new BlockPos(30, 64, 30);

        IsraelNpcData data = new IsraelNpcData(UUID.randomUUID(), NpcProfession.FARMER, home, work, social);

        // At 09:00 (work) -> should target workPos
        assertEquals(work, data.getTargetLocation(3000L, false));
        // At 19:00 (social) -> should target socialPos
        assertEquals(social, data.getTargetLocation(13000L, false));
        // At 23:00 (home) -> should target homePos
        assertEquals(home, data.getTargetLocation(17000L, false));
    }
}
