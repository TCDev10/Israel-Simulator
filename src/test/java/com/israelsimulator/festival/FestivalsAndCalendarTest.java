package com.israelsimulator.festival;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FestivalsAndCalendarTest {

    @AfterEach
    void tearDown() {
        FestivalManager.clearForcedFestival();
    }

    @Test
    @DisplayName("Verify all seven Jewish festivals exist with complete attributes")
    void testAllFestivalsPresent() {
        FestivalType[] festivals = FestivalType.values();
        assertEquals(7, festivals.length);

        for (FestivalType f : festivals) {
            assertNotNull(f.getId());
            assertNotNull(f.getDisplayName());
            assertNotNull(f.getDescription());
            assertNotNull(f.getPrimaryFood());
            assertNotNull(f.getGreeting());
            assertTrue(f.getDurationTicks() > 0L);
        }
    }

    @Test
    @DisplayName("Verify calendar progression accurately triggers annual festivals")
    void testCalendarProgression() {
        // Weekday outside of Shabbat (tick 10000 = Day 0 morning)
        assertNull(FestivalManager.getCurrentFestival(10000L));

        // Rosh Hashanah (Day 1: tick 24000)
        assertEquals(FestivalType.ROSH_HASHANAH, FestivalManager.getCurrentFestival(24000L));

        // Yom Kippur (Day 10: tick 240000)
        assertEquals(FestivalType.YOM_KIPPUR, FestivalManager.getCurrentFestival(240000L));

        // Sukkot (Day 15: tick 360000)
        assertEquals(FestivalType.SUKKOT, FestivalManager.getCurrentFestival(360000L));

        // Hanukkah (Day 35: tick 840000)
        assertEquals(FestivalType.HANUKKAH, FestivalManager.getCurrentFestival(840000L));

        // Purim (Day 74: tick 1776000)
        assertEquals(FestivalType.PURIM, FestivalManager.getCurrentFestival(1776000L));

        // Pesach (Day 90: tick 2160000)
        assertEquals(FestivalType.PESACH, FestivalManager.getCurrentFestival(2160000L));
    }

    @Test
    @DisplayName("Verify Hanukkah eight-night candle progression (nights 1 to 8)")
    void testHanukkahCandleProgression() {
        // Before Hanukkah -> 0
        assertEquals(0, FestivalManager.getHanukkahNight(10000L));

        // Night 1 (Day 35)
        assertEquals(1, FestivalManager.getHanukkahNight(35L * 24000L));
        // Night 2 (Day 36)
        assertEquals(2, FestivalManager.getHanukkahNight(36L * 24000L));
        // Night 4 (Day 38)
        assertEquals(4, FestivalManager.getHanukkahNight(38L * 24000L));
        // Night 8 (Day 42)
        assertEquals(8, FestivalManager.getHanukkahNight(42L * 24000L));

        // After Hanukkah -> 0
        assertEquals(0, FestivalManager.getHanukkahNight(43L * 24000L));
    }

    @Test
    @DisplayName("Verify Dreidel mechanics and payouts for all four letters")
    void testDreidelSpin() {
        // Nun (Nes - Miracle): Payout 0
        DreidelManager.SpinResult nunResult = DreidelManager.spinDeterministic(0);
        assertEquals(DreidelManager.DreidelLetter.NUN, nunResult.letter());
        assertEquals(0, nunResult.payout());

        // Gimel (Gadol - Great): Payout 5 Shekels
        DreidelManager.SpinResult gimelResult = DreidelManager.spinDeterministic(1);
        assertEquals(DreidelManager.DreidelLetter.GIMEL, gimelResult.letter());
        assertEquals(5, gimelResult.payout());

        // Hei (Haya - Happened): Payout 2 Shekels
        DreidelManager.SpinResult heiResult = DreidelManager.spinDeterministic(2);
        assertEquals(DreidelManager.DreidelLetter.HEI, heiResult.letter());
        assertEquals(2, heiResult.payout());

        // Shin (Sham - There): Payout -1 Shekel
        DreidelManager.SpinResult shinResult = DreidelManager.spinDeterministic(3);
        assertEquals(DreidelManager.DreidelLetter.SHIN, shinResult.letter());
        assertEquals(-1, shinResult.payout());
    }

    @Test
    @DisplayName("Verify Shofar cooldown tracking per player UUID")
    void testShofarCooldown() {
        UUID playerId = UUID.randomUUID();
        long now = 5000L;

        ShofarCooldowns cooldowns = new ShofarCooldowns();
        assertFalse(ShofarManager.isOnCooldown(cooldowns, playerId, now));

        ShofarManager.recordBlow(cooldowns, playerId, now);
        assertTrue(ShofarManager.isOnCooldown(cooldowns, playerId, now + 10L));
        assertTrue(ShofarManager.isOnCooldown(cooldowns, playerId, now + ShofarManager.COOLDOWN_TICKS - 1L));

        // After cooldown expires
        assertFalse(ShofarManager.isOnCooldown(cooldowns, playerId, now + ShofarManager.COOLDOWN_TICKS + 1L));

        ShofarManager.clearCooldown(cooldowns, playerId);
        assertFalse(ShofarManager.isOnCooldown(cooldowns, playerId, now));
    }

    @Test
    @DisplayName("Shofar cooldown is saved data, not a static map, and survives a codec reload")
    void testShofarCooldownPersists() throws Exception {
        for (Class<?> type : java.util.List.of(
                ShofarManager.class,
                ShofarCooldowns.class,
                com.israelsimulator.item.festival.ShofarItem.class)) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())) {
                    fail(type.getSimpleName() + " must not keep cooldown in a static map: " + field.getName());
                }
            }
        }

        UUID playerId = UUID.randomUUID();
        long blownAt = 5000L;
        ShofarCooldowns live = new ShofarCooldowns();
        ShofarCooldowns other = new ShofarCooldowns();
        ShofarManager.recordBlow(live, playerId, blownAt);
        assertTrue(ShofarManager.isOnCooldown(live, playerId, blownAt));
        assertFalse(ShofarManager.isOnCooldown(other, playerId, blownAt));

        net.minecraft.nbt.Tag encoded = ShofarCooldowns.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, live)
                .getOrThrow();
        ShofarCooldowns restored = ShofarCooldowns.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();

        assertNotSame(live, restored);
        assertTrue(ShofarManager.isOnCooldown(restored, playerId, blownAt + 10L));
        assertTrue(ShofarManager.isOnCooldown(restored, playerId, blownAt + ShofarManager.COOLDOWN_TICKS - 1L));
        assertFalse(ShofarManager.isOnCooldown(restored, playerId, blownAt + ShofarManager.COOLDOWN_TICKS));
        assertEquals("israel_simulator:shofar_blasts", ShofarCooldowns.TYPE.id().toString());
    }

    @Test
    @DisplayName("Verify festival and holiday translation keys exist in en_us.json")
    void testFestivalLocalizationKeys() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "lang/en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"festival.israel_simulator.shabbat\""));
            assertTrue(json.contains("\"festival.israel_simulator.rosh_hashanah\""));
            assertTrue(json.contains("\"festival.israel_simulator.yom_kippur\""));
            assertTrue(json.contains("\"festival.israel_simulator.sukkot\""));
            assertTrue(json.contains("\"festival.israel_simulator.hanukkah\""));
            assertTrue(json.contains("\"festival.israel_simulator.purim\""));
            assertTrue(json.contains("\"festival.israel_simulator.pesach\""));
            assertTrue(json.contains("\"message.israel_simulator.dreidel_spin\""));
            assertTrue(json.contains("\"message.israel_simulator.dreidel_win\""));
            assertTrue(json.contains("\"message.israel_simulator.shofar_blast\""));
            assertTrue(json.contains("\"message.israel_simulator.blessed_trader_greeting\""));
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }
}
