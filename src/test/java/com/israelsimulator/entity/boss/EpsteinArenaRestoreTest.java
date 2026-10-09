package com.israelsimulator.entity.boss;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Snapshot/restore of the Epstein dome + pavilion arena (pure rules with String states,
 * plus wiring checks on the Minecraft-side classes).
 */
class EpsteinArenaRestoreTest {
    private static final Path BOSS = Path.of("src/main/java/com/israelsimulator/entity/boss/");

    @Test
    @DisplayName("Only changed positions (or ones with a block entity) are recorded")
    void shouldRecord() {
        assertTrue(ArenaRestoreRules.shouldRecord("grass", "quartz", false));
        assertFalse(ArenaRestoreRules.shouldRecord("air", "air", false), "untouched air is not stored");
        assertTrue(ArenaRestoreRules.shouldRecord("chest", "chest", true), "block-entity data may have changed");
    }

    @Test
    @DisplayName("Restore puts back placed blocks and skips positions a player changed afterwards")
    void planSkipsPlayerChanges() {
        List<ArenaRestoreRules.Entry<String>> entries = List.of(
                new ArenaRestoreRules.Entry<>(1L, "grass", "quartz"),
                new ArenaRestoreRules.Entry<>(2L, "air", "glass"),
                new ArenaRestoreRules.Entry<>(3L, "dirt", "gold_block"),
                new ArenaRestoreRules.Entry<>(4L, "stone", "chest"));
        Map<Long, String> world = new HashMap<>(Map.of(1L, "quartz", 2L, "glass", 3L, "air", 4L, "chest"));
        world.put(3L, "cobblestone"); // player replaced the gold block

        ArenaRestoreRules.Plan<String> plan = ArenaRestoreRules.plan(entries, world::get);
        assertEquals(List.of(1L, 2L, 4L), plan.restore().stream().map(ArenaRestoreRules.Entry::pos).toList());
        assertEquals(List.of(3L), plan.skipped().stream().map(ArenaRestoreRules.Entry::pos).toList());

        for (ArenaRestoreRules.Entry<String> e : plan.restore()) world.put(e.pos(), e.original());
        assertEquals("grass", world.get(1L));
        assertEquals("air", world.get(2L));
        assertEquals("cobblestone", world.get(3L), "player change kept");
        assertEquals("stone", world.get(4L));
    }

    @Test
    @DisplayName("Snapshot then restore gives back the exact original world")
    void snapshotRoundTrip() {
        Map<Long, String> world = new HashMap<>();
        for (long p = 0; p < 50; p++) world.put(p, p % 3 == 0 ? "air" : "sand");
        Map<Long, String> original = new HashMap<>(world);
        Map<Long, String> before = new HashMap<>(world);
        for (long p = 0; p < 50; p++) world.put(p, p % 2 == 0 ? "glass" : world.get(p)); // arena placed

        List<ArenaRestoreRules.Entry<String>> entries = before.entrySet().stream()
                .filter(e -> ArenaRestoreRules.shouldRecord(e.getValue(), world.get(e.getKey()), false))
                .map(e -> new ArenaRestoreRules.Entry<>(e.getKey(), e.getValue(), world.get(e.getKey())))
                .toList();
        ArenaRestoreRules.Plan<String> plan = ArenaRestoreRules.plan(entries, world::get);
        assertTrue(plan.skipped().isEmpty());
        for (ArenaRestoreRules.Entry<String> e : plan.restore()) world.put(e.pos(), e.original());
        assertEquals(original, world);
    }

    @Test
    @DisplayName("Palette encoding round-trips and deduplicates states")
    void paletteRoundTrip() {
        List<String> states = List.of("air", "glass", "air", "quartz", "glass", "air");
        ArenaRestoreRules.Palette<String> pal = ArenaRestoreRules.encode(states);
        assertEquals(3, pal.states().size());
        assertEquals(states, ArenaRestoreRules.decode(pal));
    }

    @Test
    @DisplayName("Fight ends on kill/discard, not on chunk unload")
    void removalReasons() {
        assertTrue(ArenaRestoreRules.restoresOnRemoval(true));
        assertFalse(ArenaRestoreRules.restoresOnRemoval(false));
    }

    @Test
    @DisplayName("Orphan arenas restore only after the boss is missing twice with its area loaded")
    void orphanChecks() {
        int m = 0;
        m = ArenaRestoreRules.nextMissingCount(m, false, false);
        assertEquals(0, m, "unloaded area never counts");
        m = ArenaRestoreRules.nextMissingCount(m, false, true);
        assertFalse(ArenaRestoreRules.shouldRestoreOrphan(m));
        m = ArenaRestoreRules.nextMissingCount(m, true, true);
        assertEquals(0, m, "boss back: counter resets");
        m = ArenaRestoreRules.nextMissingCount(m, false, true);
        m = ArenaRestoreRules.nextMissingCount(m, false, true);
        assertTrue(ArenaRestoreRules.shouldRestoreOrphan(m));
    }

    @Test
    @DisplayName("Wiring: snapshot before placement, restore on death/removal/orphan, no item spill")
    void wiring() throws Exception {
        String boss = Files.readString(BOSS.resolve("JeffreyEpsteinEntity.java"));
        int capture = boss.indexOf("EpsteinArenaSnapshots.capture(");
        int place = boss.indexOf("t.placeInWorld(");
        int glass = boss.indexOf("serverLevel.setBlock(cursor, glass");
        int record = boss.indexOf("EpsteinArenaSnapshots.record(");
        assertTrue(capture >= 0 && capture < place && place < glass && glass < record,
                "capture before the pavilion and dome are placed, record after");
        assertTrue(boss.contains("public void onRemoval(Entity.RemovalReason reason)"));
        int die = boss.indexOf("public void die(");
        assertTrue(boss.indexOf("EpsteinArenaSnapshots.restore(", die) > die, "restore on death");

        String snap = Files.readString(BOSS.resolve("EpsteinArenaSnapshots.java"));
        assertTrue(snap.contains("extends SavedData"), "snapshot persists with the world");
        assertTrue(snap.contains("saveWithFullMetadata"), "block-entity NBT captured");
        assertTrue(snap.contains("loadWithComponents"), "block-entity NBT restored");
        int clear = snap.indexOf("clearable.clearContent()");
        int set = snap.indexOf("level.setBlock(BlockPos.of(e.pos()), e.original(), RESTORE_FLAGS)");
        assertTrue(clear >= 0 && clear < set, "containers emptied before blocks are replaced");
        assertTrue(snap.contains("Block.UPDATE_SUPPRESS_DROPS") && snap.contains("Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS"));
        assertTrue(snap.contains("ArenaRestoreRules.plan("), "player-changed positions are skipped");

        String events = Files.readString(Path.of("src/main/java/com/israelsimulator/event/ModGameEvents.java"));
        assertTrue(events.contains("EpsteinArenaSnapshots.tickOrphans(level)"));
    }
}
