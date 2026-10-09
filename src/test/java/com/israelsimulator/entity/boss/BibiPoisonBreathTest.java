package com.israelsimulator.entity.boss;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Bibi's poison breath: unlocked only after Epstein / Palm Beach Pete dies, random 15-25 s interval, Poison cloud. */
class BibiPoisonBreathTest {
    private static final Path BOSS = Path.of("src/main/java/com/israelsimulator/entity/boss/BibiBossEntity.java");
    private static final Path EPSTEIN = Path.of("src/main/java/com/israelsimulator/entity/boss/JeffreyEpsteinEntity.java");
    private static final Path ORB = Path.of("src/main/java/com/israelsimulator/entity/projectile/BibiPoisonBreathEntity.java");

    @Test
    @DisplayName("Only after Epstein has died, and never once Bibi is defeated")
    void onlyAfterEpstein() {
        assertFalse(BibiPoisonBreathRules.shouldLaunch(false, false, 0), "Epstein alive / never summoned");
        assertTrue(BibiPoisonBreathRules.shouldLaunch(true, false, 0));
        assertFalse(BibiPoisonBreathRules.shouldLaunch(true, false, 1), "still on cooldown");
        assertFalse(BibiPoisonBreathRules.shouldLaunch(true, true, 0), "Bibi defeated");
    }

    @Test
    @DisplayName("Interval is random between 15 and 25 seconds")
    void interval() {
        assertEquals(300, BibiPoisonBreathRules.MIN_INTERVAL_TICKS);
        assertEquals(500, BibiPoisonBreathRules.MAX_INTERVAL_TICKS);
        Random r = new Random(42);
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int i = 0; i < 5000; i++) {
            int v = BibiPoisonBreathRules.nextInterval(r);
            assertTrue(v >= 300 && v <= 500, "out of range: " + v);
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        assertEquals(300, min);
        assertEquals(500, max);
    }

    @Test
    @DisplayName("Cloud: radius 3, 6 s, Poison II for 5 s, dragon breath particles, no instant damage, no block breaking")
    void cloudEffect() throws Exception {
        assertEquals(3.0F, BibiPoisonBreathRules.CLOUD_RADIUS, 1e-6);
        assertEquals(120, BibiPoisonBreathRules.CLOUD_DURATION_TICKS);
        assertEquals(100, BibiPoisonBreathRules.POISON_DURATION_TICKS);
        assertEquals(1, BibiPoisonBreathRules.POISON_AMPLIFIER);
        String src = Files.readString(ORB);
        assertTrue(src.contains("MobEffects.POISON"));
        assertFalse(src.contains("INSTANT_DAMAGE"));
        assertTrue(src.contains("ParticleTypes.DRAGON_BREATH"));
        assertFalse(src.contains("explode("), "must never create a block-breaking explosion");
        assertTrue(src.contains("onHitBlock"), "block hits are overridden to do nothing");
    }

    @Test
    @DisplayName("Flag is persisted and set only by the Epstein summoned by this Bibi")
    void persistedFlag() throws Exception {
        String boss = Files.readString(BOSS);
        assertTrue(boss.contains("output.putBoolean(\"EpsteinDefeated\""));
        assertTrue(boss.contains("input.getBooleanOr(\"EpsteinDefeated\""));
        assertTrue(boss.contains("tickPoisonBreath(serverLevel)"));
        String ep = Files.readString(EPSTEIN);
        int die = ep.indexOf("public void die(");
        assertTrue(die > 0);
        assertTrue(ep.indexOf("onEpsteinDefeated()", die) > die, "Epstein.die() must notify its own Bibi");
        assertTrue(ep.contains("serverLevel.getEntity(this.bibiBossUuid) instanceof BibiBossEntity bibi"));
    }
}
