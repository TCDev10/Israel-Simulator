package com.israelsimulator.synagogue;

import com.israelsimulator.world.structure.ModStructures;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SynagogueFrameworkTest {

    @Test
    @DisplayName("Verify Great Synagogue structure keys in ModStructures")
    void testGreatSynagogueStructureKeys() {
        assertNotNull(ModStructures.SYNAGOGUE);
        assertNotNull(ModStructures.GREAT_SYNAGOGUE);
        assertNotNull(ModStructures.SYNAGOGUES);
        assertNotNull(ModStructures.GREAT_SYNAGOGUES);

        assertTrue(ModStructures.allStructures().contains(ModStructures.SYNAGOGUE));
        assertTrue(ModStructures.allStructures().contains(ModStructures.GREAT_SYNAGOGUE));
    }

    @Test
    @DisplayName("Verify great_synagogue.json and great_synagogues.json exist and are valid")
    void testSynagogueStructureDataFiles() {
        InputStream structStream = getClass().getResourceAsStream("/data/israel_simulator/worldgen/structure/great_synagogue.json");
        assertNotNull(structStream, "great_synagogue.json must exist in data");
        try {
            String json = new String(structStream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("minecraft:jigsaw"));
            assertTrue(json.contains("israel_simulator:jerusalem"));
            assertFalse(json.contains("is_israel_region"));
        } catch (Exception e) {
            fail("Failed reading great_synagogue.json: " + e.getMessage());
        }

        InputStream setStream = getClass().getResourceAsStream("/data/israel_simulator/worldgen/structure_set/great_synagogues.json");
        assertNotNull(setStream, "great_synagogues.json must exist in data");
        try {
            String json = new String(setStream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:great_synagogue"));
            assertTrue(json.contains("minecraft:random_spread"));
        } catch (Exception e) {
            fail("Failed reading great_synagogues.json: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify synagogue_ark.json loot table exists and contains ritual items")
    void testSynagogueArkLootTable() {
        InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/loot_table/chests/synagogue_ark.json");
        assertNotNull(stream, "synagogue_ark.json must exist in loot_table/chests");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:mezuzah"));
            assertTrue(json.contains("israel_simulator:talit"));
            assertTrue(json.contains("israel_simulator:tefillin"));
            assertTrue(json.contains("israel_simulator:kippah"));
            assertTrue(json.contains("israel_simulator:prayer_note"));
            assertTrue(json.contains("israel_simulator:shekel"));
        } catch (Exception e) {
            fail("Failed reading synagogue_ark.json: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify SynagogueManager validation and cooldown logic")
    void testSynagogueManagerValidation() {
        // Without Kippah
        assertEquals(SynagogueManager.ArkInteractionStatus.MISSING_KIPPAH,
                SynagogueManager.validateInteraction(false, false));

        // On cooldown
        assertEquals(SynagogueManager.ArkInteractionStatus.COOLDOWN_ACTIVE,
                SynagogueManager.validateInteraction(true, true));

        // Valid
        assertEquals(SynagogueManager.ArkInteractionStatus.SUCCESS,
                SynagogueManager.validateInteraction(true, false));

        UUID id = UUID.randomUUID();
        SynagogueCooldowns cooldowns = new SynagogueCooldowns();
        assertFalse(SynagogueManager.isOnCooldown(cooldowns, id, 5000L));
        cooldowns.setLastUseTime(id, 5000L);
        assertTrue(SynagogueManager.isOnCooldown(cooldowns, id, 5000L));
        SynagogueManager.clearCooldown(cooldowns, id);
        assertFalse(SynagogueManager.isOnCooldown(cooldowns, id, 5000L));
    }

    @Test
    @DisplayName("Synagogue cooldown is saved data, not a static map, and survives a codec reload")
    void testSynagogueCooldownPersists() throws Exception {
        for (Class<?> type : java.util.List.of(SynagogueManager.class, SynagogueCooldowns.class)) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())) {
                    fail(type.getSimpleName() + " must not keep cooldown in a static map: " + field.getName());
                }
            }
        }

        UUID playerId = UUID.randomUUID();
        long prayedAt = 8000L;
        SynagogueCooldowns first = new SynagogueCooldowns();
        SynagogueCooldowns second = new SynagogueCooldowns();
        first.setLastUseTime(playerId, prayedAt);
        assertTrue(SynagogueManager.isOnCooldown(first, playerId, prayedAt));
        assertFalse(SynagogueManager.isOnCooldown(second, playerId, prayedAt));

        net.minecraft.nbt.Tag encoded = SynagogueCooldowns.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, first)
                .getOrThrow();
        SynagogueCooldowns restored = SynagogueCooldowns.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();

        assertNotSame(first, restored);
        assertTrue(SynagogueManager.isOnCooldown(restored, playerId, prayedAt + 100L));
        assertTrue(SynagogueManager.isOnCooldown(restored, playerId, prayedAt + SynagogueManager.COOLDOWN_TICKS - 1L));
        assertFalse(SynagogueManager.isOnCooldown(restored, playerId, prayedAt + SynagogueManager.COOLDOWN_TICKS));
        assertEquals("israel_simulator:synagogue_prayers", SynagogueCooldowns.TYPE.id().toString());
    }
}
