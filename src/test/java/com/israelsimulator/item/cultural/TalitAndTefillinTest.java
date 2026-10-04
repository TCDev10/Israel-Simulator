package com.israelsimulator.item.cultural;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TalitAndTefillinTest {

    @Test
    @DisplayName("Verify TalitItem class exists and extends Item")
    void testTalitItemClassStructure() throws Exception {
        ClassLoader cl = TalitAndTefillinTest.class.getClassLoader();
        Class<?> talitClass = Class.forName("com.israelsimulator.item.cultural.TalitItem", false, cl);
        assertNotNull(talitClass);
        assertEquals("net.minecraft.world.item.Item", talitClass.getSuperclass().getName());
    }

    @Test
    @DisplayName("Verify TefillinItem class exists and extends Item")
    void testTefillinItemClassStructure() throws Exception {
        ClassLoader cl = TalitAndTefillinTest.class.getClassLoader();
        Class<?> tefillinClass = Class.forName("com.israelsimulator.item.cultural.TefillinItem", false, cl);
        assertNotNull(tefillinClass);
        assertEquals("net.minecraft.world.item.Item", tefillinClass.getSuperclass().getName());
    }

    @Test
    @DisplayName("Verify Talit equipment asset key and JSON asset definition")
    void testTalitEquipmentAsset() {
        assertNotNull(CulturalItems.TALIT_ASSET);
        assertEquals("israel_simulator:talit", CulturalItems.TALIT_ASSET.identifier().toString());

        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/equipment/talit.json");
        assertNotNull(stream, "equipment/talit.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:talit"));
            assertTrue(json.contains("humanoid"));
        } catch (Exception e) {
            fail("Failed reading equipment asset JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify humanoid equipment textures exist for Talit")
    void testTalitTextures() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/equipment/humanoid/talit.png");
        assertNotNull(stream, "textures/entity/equipment/humanoid/talit.png must exist");

        InputStream babyStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/equipment/humanoid_baby/talit.png");
        assertNotNull(babyStream, "textures/entity/equipment/humanoid_baby/talit.png must exist");
    }

    @Test
    @DisplayName("Verify 3D models exist for Talit and Tefillin")
    void test3DModels() {
        InputStream talitModel = getClass().getResourceAsStream("/assets/israel_simulator/models/item/talit.json");
        assertNotNull(talitModel, "talit.json model must exist");

        InputStream tefillinModel = getClass().getResourceAsStream("/assets/israel_simulator/models/item/tefillin.json");
        assertNotNull(tefillinModel, "tefillin.json model must exist");
    }

    @Test
    @DisplayName("Verify Tefillin prayer validation logic under all conditions")
    void testTefillinPrayerValidation() {
        // Missing Kippah
        assertEquals(TefillinManager.TefillinStatus.MISSING_KIPPAH,
                TefillinManager.validatePrayer(false, true, false));

        // Not daytime
        assertEquals(TefillinManager.TefillinStatus.NOT_DAYTIME,
                TefillinManager.validatePrayer(true, false, false));

        // On cooldown
        assertEquals(TefillinManager.TefillinStatus.COOLDOWN_ACTIVE,
                TefillinManager.validatePrayer(true, true, true));

        // All conditions met
        assertEquals(TefillinManager.TefillinStatus.SUCCESS,
                TefillinManager.validatePrayer(true, true, false));
    }

    @Test
    @DisplayName("Verify Tefillin cooldown tracking per player UUID")
    void testTefillinCooldown() {
        UUID playerId = UUID.randomUUID();
        long now = 10000L;

        TefillinCooldowns cooldowns = new TefillinCooldowns();
        assertFalse(TefillinManager.isOnCooldown(cooldowns, playerId, now));

        TefillinManager.setLastUseTime(cooldowns, playerId, now);
        assertTrue(TefillinManager.isOnCooldown(cooldowns, playerId, now + 100L));
        assertTrue(TefillinManager.isOnCooldown(cooldowns, playerId, now + TefillinManager.COOLDOWN_TICKS - 1L));

        // Cooldown expires after COOLDOWN_TICKS
        assertFalse(TefillinManager.isOnCooldown(cooldowns, playerId, now + TefillinManager.COOLDOWN_TICKS + 1L));

        TefillinManager.clearCooldown(cooldowns, playerId);
        assertFalse(TefillinManager.isOnCooldown(cooldowns, playerId, now));
    }

    @Test
    @DisplayName("Tefillin cooldown is saved data, not a static map, and survives a codec reload")
    void testTefillinCooldownPersists() throws Exception {
        for (Class<?> type : java.util.List.of(TefillinManager.class, TefillinCooldowns.class, TefillinItem.class)) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())) {
                    fail(type.getSimpleName() + " must not keep cooldown in a static map: " + field.getName());
                }
            }
        }

        UUID playerId = UUID.randomUUID();
        long usedAt = 10000L;
        TefillinCooldowns live = new TefillinCooldowns();
        TefillinCooldowns other = new TefillinCooldowns();
        TefillinManager.setLastUseTime(live, playerId, usedAt);
        assertTrue(TefillinManager.isOnCooldown(live, playerId, usedAt));
        assertFalse(TefillinManager.isOnCooldown(other, playerId, usedAt));

        net.minecraft.nbt.Tag encoded = TefillinCooldowns.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, live)
                .getOrThrow();
        TefillinCooldowns restored = TefillinCooldowns.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();

        assertNotSame(live, restored);
        assertTrue(TefillinManager.isOnCooldown(restored, playerId, usedAt + 100L));
        assertTrue(TefillinManager.isOnCooldown(restored, playerId, usedAt + TefillinManager.COOLDOWN_TICKS - 1L));
        assertFalse(TefillinManager.isOnCooldown(restored, playerId, usedAt + TefillinManager.COOLDOWN_TICKS));
        assertEquals("israel_simulator:tefillin_prayers", TefillinCooldowns.TYPE.id().toString());
    }

    @Test
    @DisplayName("Verify localization keys for Talit and Tefillin")
    void testLocalizationKeys() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "lang/en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"item.israel_simulator.talit\""));
            assertTrue(json.contains("\"item.israel_simulator.talit.desc\""));
            assertTrue(json.contains("\"item.israel_simulator.talit.bonus\""));
            assertTrue(json.contains("\"item.israel_simulator.tefillin\""));
            assertTrue(json.contains("\"item.israel_simulator.tefillin.desc\""));
            assertTrue(json.contains("\"message.israel_simulator.tefillin_need_kippah\""));
            assertTrue(json.contains("\"message.israel_simulator.tefillin_not_daytime\""));
            assertTrue(json.contains("\"message.israel_simulator.tefillin_cooldown\""));
            assertTrue(json.contains("\"message.israel_simulator.tefillin_blessed\""));
            assertTrue(json.contains("\"message.israel_simulator.tefillin_talit_complete\""));
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }
}
