package com.israelsimulator.westernwall;

import java.util.List;
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
        WesternWallCooldowns cooldowns = new WesternWallCooldowns();

        assertFalse(WesternWallManager.isOnCooldown(cooldowns, playerId, initialTime));

        cooldowns.setLastPrayerTime(playerId, initialTime);
        assertTrue(WesternWallManager.isOnCooldown(cooldowns, playerId, initialTime + 100L));
        assertTrue(WesternWallManager.isOnCooldown(cooldowns, playerId, initialTime + WesternWallManager.COOLDOWN_TICKS - 1L));
        assertFalse(WesternWallManager.isOnCooldown(cooldowns, playerId, initialTime + WesternWallManager.COOLDOWN_TICKS + 1L));

        cooldowns.clearCooldown(playerId);
        assertFalse(WesternWallManager.isOnCooldown(cooldowns, playerId, initialTime));
    }

    @Test
    @DisplayName("Cooldown is not a static map: two stores do not share prayer times")
    void testCooldownIsNotStatic() throws Exception {
        for (Class<?> type : List.of(WesternWallManager.class, WesternWallCooldowns.class)) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())) {
                    fail(type.getSimpleName() + " must not keep cooldown in a static map: " + field.getName());
                }
            }
        }

        UUID playerId = UUID.randomUUID();
        WesternWallCooldowns first = new WesternWallCooldowns();
        WesternWallCooldowns second = new WesternWallCooldowns();
        first.setLastPrayerTime(playerId, 1000L);
        assertTrue(WesternWallManager.isOnCooldown(first, playerId, 1000L));
        assertFalse(WesternWallManager.isOnCooldown(second, playerId, 1000L));
    }

    @Test
    @DisplayName("Cooldown round-trips through the saved-data codec, as after a server restart")
    void testCooldownSurvivesSaveLoad() {
        UUID playerId = UUID.randomUUID();
        long prayedAt = 5000L;
        WesternWallCooldowns live = new WesternWallCooldowns();
        live.setLastPrayerTime(playerId, prayedAt);

        net.minecraft.nbt.Tag encoded = WesternWallCooldowns.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, live)
                .getOrThrow();
        WesternWallCooldowns restored = WesternWallCooldowns.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();

        assertNotSame(live, restored);
        assertTrue(WesternWallManager.isOnCooldown(restored, playerId, prayedAt + 100L));
        assertTrue(WesternWallManager.isOnCooldown(restored, playerId, prayedAt + WesternWallManager.COOLDOWN_TICKS - 1L));
        assertFalse(WesternWallManager.isOnCooldown(restored, playerId, prayedAt + WesternWallManager.COOLDOWN_TICKS));
        assertEquals(WesternWallCooldowns.TYPE.id().toString(), "israel_simulator:western_wall_prayers");
    }

    @Test
    @DisplayName("Instabuild does not bypass the prayer cooldown")
    void testInstabuildDoesNotBypassCooldown() {
        assertTrue(WesternWallManager.enforcesCooldown(true));
        assertTrue(WesternWallManager.enforcesCooldown(false));
    }

    @Test
    @DisplayName("Award reward specifies exactly 5 Diamonds according to GAME_DESIGN.md §11")
    void testRewardConstants() {
        assertEquals(5, WesternWallManager.REWARD_DIAMONDS);
        assertEquals(24000L, WesternWallManager.COOLDOWN_TICKS);
    }

    @Test
    @DisplayName("Prayer attempts with a note should animate the arm swing")
    void prayerAttemptsAnimateSwing() {
        assertTrue(WesternWallManager.shouldAnimateSwing(WesternWallManager.PrayerStatus.SUCCESS));
        assertTrue(WesternWallManager.shouldAnimateSwing(WesternWallManager.PrayerStatus.MISSING_KIPPAH));
        assertTrue(WesternWallManager.shouldAnimateSwing(WesternWallManager.PrayerStatus.COOLDOWN_ACTIVE));
        assertFalse(WesternWallManager.shouldAnimateSwing(WesternWallManager.PrayerStatus.MISSING_PRAYER_NOTE));
    }

    @Test
    @DisplayName("tryPray source swings the main hand on handled prayer paths")
    void tryPraySourceCallsSwing() throws Exception {
        String src = java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/java/com/israelsimulator/westernwall/WesternWallManager.java"));
        assertTrue(src.contains("player.swing(InteractionHand.MAIN_HAND)"),
                "client path should swing locally");
        assertTrue(src.contains("player.swing(InteractionHand.MAIN_HAND, true)"),
                "server path should swing and notify other clients");
    }
}
