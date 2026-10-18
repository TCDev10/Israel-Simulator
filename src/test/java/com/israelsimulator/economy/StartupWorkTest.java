package com.israelsimulator.economy;

import static org.junit.jupiter.api.Assertions.*;

import com.israelsimulator.economy.work.StartupWork;
import org.junit.jupiter.api.Test;

class StartupWorkTest {
    @Test
    void techHubPaysDouble() {
        assertEquals(StartupWork.BASE_PAY, StartupWork.sessionPay(1, false));
        assertEquals(StartupWork.BASE_PAY * 2, StartupWork.sessionPay(1, true));
    }

    @Test
    void everyTenthSessionIsAnExit() {
        assertFalse(StartupWork.isExit(9));
        assertTrue(StartupWork.isExit(10));
        assertTrue(StartupWork.isExit(20));
        assertEquals(StartupWork.BASE_PAY + StartupWork.EXIT_BONUS, StartupWork.sessionPay(10, false));
        assertEquals(10, StartupWork.sessionsToExit(0));
        assertEquals(1, StartupWork.sessionsToExit(9));
        assertEquals(10, StartupWork.sessionsToExit(10));
    }

    @Test
    void cooldownsPreventSpam() {
        assertTrue(StartupWork.LAPTOP_COOLDOWN >= StartupWork.GIG_COOLDOWN);
        assertTrue(StartupWork.WORK_TICKS >= 40);
    }
}
