package com.israelsimulator.westernwall;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WesternWallInteractionTest {

    @BeforeEach
    void clearSessions() {
        WesternWallManager.clearActiveForTests();
    }

    @Test
    @DisplayName("Valid prayer inputs validate as SUCCESS")
    void testValidPrayer() {
        assertEquals(WesternWallManager.PrayerStatus.SUCCESS,
                WesternWallManager.validatePrayer(true, true, false));
    }

    @Test
    @DisplayName("Missing Kippah fails validation")
    void testMissingKippah() {
        assertEquals(WesternWallManager.PrayerStatus.MISSING_KIPPAH,
                WesternWallManager.validatePrayer(false, true, false));
    }

    @Test
    @DisplayName("Missing Prayer Note fails validation")
    void testMissingPrayerNote() {
        assertEquals(WesternWallManager.PrayerStatus.MISSING_PRAYER_NOTE,
                WesternWallManager.validatePrayer(true, false, false));
    }

    @Test
    @DisplayName("Cooldown fails validation")
    void testCooldown() {
        assertEquals(WesternWallManager.PrayerStatus.COOLDOWN_ACTIVE,
                WesternWallManager.validatePrayer(true, true, true));
    }

    @Test
    @DisplayName("Session completes after DURATION_TICKS and not before")
    void sessionCompletesAfterDuration() {
        WesternWallPrayerSession session = new WesternWallPrayerSession(
                net.minecraft.core.BlockPos.ZERO, 100L, 0, 0, 0);
        assertFalse(WesternWallManager.isComplete(session, 100L));
        assertFalse(WesternWallManager.isComplete(session, 100L + WesternWallPrayerSession.DURATION_TICKS - 1));
        assertTrue(WesternWallManager.isComplete(session, 100L + WesternWallPrayerSession.DURATION_TICKS));
    }

    @Test
    @DisplayName("Session cancels when player moves beyond threshold")
    void sessionCancelsOnMove() {
        WesternWallPrayerSession session = new WesternWallPrayerSession(
                net.minecraft.core.BlockPos.ZERO, 0L, 10.0, 64.0, 20.0);
        assertFalse(WesternWallManager.shouldCancelForMovement(session, 10.0, 64.0, 20.0));
        assertFalse(WesternWallManager.shouldCancelForMovement(session, 10.3, 64.0, 20.0));
        assertTrue(WesternWallManager.shouldCancelForMovement(session, 10.6, 64.0, 20.0));
    }

    @Test
    @DisplayName("Active session map tracks praying players without double-entry")
    void activeSessionTracking() {
        UUID id = UUID.randomUUID();
        assertFalse(WesternWallManager.isPraying(id));
        WesternWallPrayerSession session = new WesternWallPrayerSession(
                net.minecraft.core.BlockPos.ZERO, 50L, 1, 2, 3);
        WesternWallManager.putSessionForTests(id, session);
        assertTrue(WesternWallManager.isPraying(id));
        assertEquals(session, WesternWallManager.getSession(id));
        // second put replaces — still one session (no double reward path)
        WesternWallManager.putSessionForTests(id, session);
        assertTrue(WesternWallManager.isPraying(id));
        WesternWallManager.clearActiveForTests();
        assertFalse(WesternWallManager.isPraying(id));
    }

    @Test
    @DisplayName("Cooldown enforcement is always on, including creative")
    void cooldownAlwaysEnforced() {
        assertTrue(WesternWallManager.enforcesCooldown(true));
        assertTrue(WesternWallManager.enforcesCooldown(false));
    }

    @Test
    @DisplayName("Reward and timing constants")
    void constants() {
        assertEquals(5, WesternWallManager.REWARD_DIAMONDS);
        assertEquals(24000L, WesternWallManager.COOLDOWN_TICKS);
        assertEquals(60, WesternWallPrayerSession.DURATION_TICKS);
    }

    @Test
    @DisplayName("No static cooldown maps remain on manager/cooldowns classes")
    void noStaticCooldownMaps() {
        for (Class<?> type : List.of(WesternWallManager.class, WesternWallCooldowns.class)) {
            for (var field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())
                        && field.getName().toLowerCase().contains("cooldown")) {
                    fail("Unexpected static cooldown map: " + field);
                }
            }
        }
    }

    @Test
    @DisplayName("SavedData codec round-trip still works")
    void cooldownSavedDataRoundTrip() {
        WesternWallCooldowns live = new WesternWallCooldowns();
        UUID playerId = UUID.randomUUID();
        long prayedAt = 5000L;
        live.setLastPrayerTime(playerId, prayedAt);
        net.minecraft.nbt.Tag encoded = WesternWallCooldowns.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, live)
                .getOrThrow();
        WesternWallCooldowns restored = WesternWallCooldowns.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();
        assertTrue(WesternWallManager.isOnCooldown(restored, playerId, prayedAt + 100L));
        assertFalse(WesternWallManager.isOnCooldown(restored, playerId, prayedAt + WesternWallManager.COOLDOWN_TICKS));
        assertEquals("israel_simulator:western_wall_prayers", WesternWallCooldowns.TYPE.id().toString());
    }
}
