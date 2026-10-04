package com.israelsimulator.easteregg;

import com.israelsimulator.core.data.RarityLevel;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EasterEggAndRarityTest {

    @Test
    public void testRarityLevelsAndHierarchy() {
        assertEquals(6, RarityLevel.values().length, "Must define exactly 6 rarity tiers per §51");

        assertSame(RarityLevel.COMMON, RarityLevel.valueOf("COMMON"));
        assertSame(RarityLevel.UNCOMMON, RarityLevel.valueOf("UNCOMMON"));
        assertSame(RarityLevel.RARE, RarityLevel.valueOf("RARE"));
        assertSame(RarityLevel.EPIC, RarityLevel.valueOf("EPIC"));
        assertSame(RarityLevel.LEGENDARY, RarityLevel.valueOf("LEGENDARY"));
        assertSame(RarityLevel.MYTHIC, RarityLevel.valueOf("MYTHIC"));

        // Verify tier progression
        assertTrue(RarityLevel.MYTHIC.isAtLeast(RarityLevel.LEGENDARY));
        assertTrue(RarityLevel.LEGENDARY.isAtLeast(RarityLevel.EPIC));
        assertTrue(RarityLevel.EPIC.isAtLeast(RarityLevel.RARE));
        assertTrue(RarityLevel.RARE.isAtLeast(RarityLevel.UNCOMMON));
        assertTrue(RarityLevel.UNCOMMON.isAtLeast(RarityLevel.COMMON));

        // Verify loot weights decrease with higher rarity
        assertTrue(RarityLevel.COMMON.defaultLootWeight() > RarityLevel.UNCOMMON.defaultLootWeight());
        assertTrue(RarityLevel.UNCOMMON.defaultLootWeight() > RarityLevel.RARE.defaultLootWeight());
        assertTrue(RarityLevel.RARE.defaultLootWeight() > RarityLevel.EPIC.defaultLootWeight());
        assertTrue(RarityLevel.EPIC.defaultLootWeight() > RarityLevel.LEGENDARY.defaultLootWeight());
        assertTrue(RarityLevel.LEGENDARY.defaultLootWeight() > RarityLevel.MYTHIC.defaultLootWeight());
    }

    @Test
    public void testEasterEggManagerDialogues() {
        var dialogues = EasterEggManager.getAbsurdDialogues();
        assertNotNull(dialogues);
        assertFalse(dialogues.isEmpty());
        assertTrue(dialogues.size() >= 4, "Should have multiple satirical dialogue options");

        for (String line : dialogues) {
            assertNotNull(line);
            assertFalse(line.isBlank());
        }
    }

    @Test
    @DisplayName("Easter egg cooldown is saved data, not a static map, and survives a codec reload")
    void testEasterEggCooldownPersists() throws Exception {
        for (Class<?> type : java.util.List.of(EasterEggManager.class, EasterEggCooldowns.class)) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && java.util.Map.class.isAssignableFrom(field.getType())) {
                    fail(type.getSimpleName() + " must not keep cooldown in a static map: " + field.getName());
                }
            }
        }

        UUID playerId = UUID.randomUUID();
        long usedAt = 4000L;
        EasterEggCooldowns live = new EasterEggCooldowns();
        EasterEggCooldowns other = new EasterEggCooldowns();
        EasterEggManager.recordInteraction(live, playerId, usedAt);
        assertTrue(EasterEggManager.isOnCooldown(live, playerId, usedAt));
        assertFalse(EasterEggManager.isOnCooldown(other, playerId, usedAt));

        net.minecraft.nbt.Tag encoded = EasterEggCooldowns.CODEC
                .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, live)
                .getOrThrow();
        EasterEggCooldowns restored = EasterEggCooldowns.CODEC
                .parse(net.minecraft.nbt.NbtOps.INSTANCE, encoded)
                .getOrThrow();

        assertNotSame(live, restored);
        assertTrue(EasterEggManager.isOnCooldown(restored, playerId, usedAt + 1L));
        assertTrue(EasterEggManager.isOnCooldown(restored, playerId, usedAt + EasterEggManager.COOLDOWN_TICKS - 1L));
        assertFalse(EasterEggManager.isOnCooldown(restored, playerId, usedAt + EasterEggManager.COOLDOWN_TICKS));
        assertEquals(100L, EasterEggManager.COOLDOWN_TICKS);
        assertEquals("israel_simulator:easter_egg_interactions", EasterEggCooldowns.TYPE.id().toString());

        EasterEggManager.clearCooldown(restored, playerId);
        assertFalse(EasterEggManager.isOnCooldown(restored, playerId, usedAt));
    }
}
