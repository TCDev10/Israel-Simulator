package com.israelsimulator.easteregg;

import com.israelsimulator.core.data.RarityLevel;
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
}
