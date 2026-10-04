package com.israelsimulator.westernwall;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WesternWallInteractionTest {

    @Test
    @DisplayName("Validation succeeds when player wears Kippah, holds Prayer Note, and is off cooldown")
    void testValidatePrayerSuccess() {
        WesternWallManager.PrayerStatus status = WesternWallManager.validatePrayer(true, true, false);
        assertEquals(WesternWallManager.PrayerStatus.SUCCESS, status);
    }

    @Test
    @DisplayName("Validation fails when player is not wearing a Kippah")
    void testValidatePrayerMissingKippah() {
        WesternWallManager.PrayerStatus status = WesternWallManager.validatePrayer(false, true, false);
        assertEquals(WesternWallManager.PrayerStatus.MISSING_KIPPAH, status);
    }

    @Test
    @DisplayName("Validation fails when player is not holding a Prayer Note")
    void testValidatePrayerMissingNote() {
        WesternWallManager.PrayerStatus status = WesternWallManager.validatePrayer(true, false, false);
        assertEquals(WesternWallManager.PrayerStatus.MISSING_PRAYER_NOTE, status);
    }

    @Test
    @DisplayName("Validation fails when prayer cooldown is still active")
    void testValidatePrayerOnCooldown() {
        WesternWallManager.PrayerStatus status = WesternWallManager.validatePrayer(true, true, true);
        assertEquals(WesternWallManager.PrayerStatus.COOLDOWN_ACTIVE, status);
    }

    @Test
    @DisplayName("Server cooldown accurately tracks player UUID and game time")
    void testCooldownTracking() {
        UUID playerId = UUID.randomUUID();
        long initialTime = 1000L;

        assertFalse(WesternWallManager.isOnCooldown(playerId, initialTime));

        WesternWallManager.setLastPrayerTime(playerId, initialTime);
        assertTrue(WesternWallManager.isOnCooldown(playerId, initialTime + 100L));
        assertTrue(WesternWallManager.isOnCooldown(playerId, initialTime + WesternWallManager.COOLDOWN_TICKS - 1L));
        assertFalse(WesternWallManager.isOnCooldown(playerId, initialTime + WesternWallManager.COOLDOWN_TICKS + 1L));

        WesternWallManager.clearCooldown(playerId);
        assertFalse(WesternWallManager.isOnCooldown(playerId, initialTime));
    }

    @Test
    @DisplayName("Award reward specifies exactly 5 Diamonds according to GAME_DESIGN.md §11")
    void testRewardConstants() {
        assertEquals(5, WesternWallManager.REWARD_DIAMONDS);
        assertEquals(24000L, WesternWallManager.COOLDOWN_TICKS);
    }
}
