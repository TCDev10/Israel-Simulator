package com.israelsimulator.entity.boss;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Epstein fight: glass dome + pavilion arena at 50% HP, rename to "Palm Beach Pete" at 33% HP,
 * both one-shot and persisted.
 */
class EpsteinPhaseTriggersTest {
    private static final Path SRC = Path.of("src/main/java/com/israelsimulator/entity/boss/JeffreyEpsteinEntity.java");

    @Test
    @DisplayName("Thresholds: dome at 50%, rename at 33%")
    void thresholds() {
        assertEquals(0.5F, EpsteinPhaseRules.DOME_HEALTH_FRACTION, 1e-6);
        assertEquals(0.33F, EpsteinPhaseRules.RENAME_HEALTH_FRACTION, 1e-6);
    }

    @Test
    @DisplayName("Dome triggers at or below 50% only, and only once")
    void domeOneShot() {
        assertFalse(EpsteinPhaseRules.shouldSpawnDome(false, 0.51F));
        assertTrue(EpsteinPhaseRules.shouldSpawnDome(false, 0.5F));
        assertTrue(EpsteinPhaseRules.shouldSpawnDome(false, 0.4F));
        assertFalse(EpsteinPhaseRules.shouldSpawnDome(true, 0.4F), "never twice");
        assertFalse(EpsteinPhaseRules.shouldSpawnDome(true, 0.1F), "never twice");
    }

    @Test
    @DisplayName("Rename triggers at or below 33% only, and only once")
    void renameOneShot() {
        assertFalse(EpsteinPhaseRules.shouldRename(false, 0.5F));
        assertFalse(EpsteinPhaseRules.shouldRename(false, 0.34F));
        assertTrue(EpsteinPhaseRules.shouldRename(false, 0.33F));
        assertTrue(EpsteinPhaseRules.shouldRename(false, 0.05F));
        assertFalse(EpsteinPhaseRules.shouldRename(true, 0.05F));
    }

    @Test
    @DisplayName("Simulated fight: each trigger fires exactly once as health drops")
    void simulatedFight() {
        boolean dome = false, renamed = false;
        int domeFires = 0, renameFires = 0;
        for (int hp = 100; hp >= 0; hp--) {
            float f = hp / 100.0F;
            if (EpsteinPhaseRules.shouldSpawnDome(dome, f)) { dome = true; domeFires++; }
            if (EpsteinPhaseRules.shouldRename(renamed, f)) { renamed = true; renameFires++; }
            if (hp == 40) {
                assertTrue(dome);
                assertFalse(renamed, "rename must not happen before 33%");
            }
        }
        assertEquals(1, domeFires);
        assertEquals(1, renameFires);
    }

    @Test
    @DisplayName("Rename sets custom name + boss bar and both flags are saved and loaded")
    void renameAndPersistence() throws Exception {
        String src = Files.readString(SRC);
        assertTrue(src.contains("EpsteinPhaseRules.shouldSpawnDome(") && src.contains("EpsteinPhaseRules.shouldRename("));
        assertTrue(src.contains("this.setCustomName(renamedName())"));
        assertTrue(src.contains("this.bossEvent.setName("));
        assertTrue(src.contains("output.putBoolean(\"DomeSpawned\", this.domeSpawned)"));
        assertTrue(src.contains("output.putBoolean(\"Renamed\", this.renamed)"));
        assertTrue(src.contains("this.domeSpawned = input.getBooleanOr(\"DomeSpawned\", false)"));
        int load = src.indexOf("input.getBooleanOr(\"Renamed\", false)");
        assertTrue(load >= 0 && src.indexOf("applyRename()", load) > load, "boss bar name restored on load");
        assertFalse(src.contains("progress <= 0.3F"), "old 30% trigger removed");
    }

    @Test
    @DisplayName("Palm Beach Pete is translated in en_us and it_it")
    void lang() throws Exception {
        for (String f : new String[] {"en_us", "it_it"}) {
            String json = Files.readString(Path.of("src/main/resources/assets/israel_simulator/lang/" + f + ".json"));
            assertTrue(json.contains("\"" + EpsteinPhaseRules.RENAMED_NAME_KEY + "\": \"Palm Beach Pete\""), f);
        }
    }
}
