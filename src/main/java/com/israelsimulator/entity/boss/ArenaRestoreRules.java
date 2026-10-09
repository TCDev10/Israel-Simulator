package com.israelsimulator.entity.boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongFunction;

/**
 * Pure snapshot/restore rules for the Epstein dome + pavilion arena (no Minecraft types, so
 * unit-testable). {@code S} is the block-state type (BlockState in game, String in tests).
 *
 * <p>Before the arena is placed every affected position is snapshotted; after placement only
 * positions whose state changed (or that held a block entity) are kept. On restore a position
 * is put back only if it still holds the state the arena placed there, so blocks a player
 * changed afterwards are left alone.</p>
 */
public final class ArenaRestoreRules {
    /** Ticks between orphan checks of saved arenas. */
    public static final int ORPHAN_CHECK_INTERVAL = 100;
    /** Consecutive checks the boss must be missing (with its area loaded) before restoring. */
    public static final int ORPHAN_CHECKS_BEFORE_RESTORE = 2;

    private ArenaRestoreRules() {}

    /** One changed position: packed pos, state before the arena, state the arena placed. */
    public record Entry<S>(long pos, S original, S placed) {}

    /** Result of a restore pass: positions to put back and positions left as the player made them. */
    public record Plan<S>(List<Entry<S>> restore, List<Entry<S>> skipped) {}

    /** Palette-encoded states (compact storage): {@code states.get(indices[i])} is entry i. */
    public record Palette<S>(List<S> states, int[] indices) {}

    /** Keep a snapshot entry if placement changed the state, or the original held a block entity. */
    public static <S> boolean shouldRecord(S before, S after, boolean hadBlockEntity) {
        return hadBlockEntity || !Objects.equals(before, after);
    }

    /** Restore a position only while it still holds the state the arena placed. */
    public static <S> boolean shouldRestore(S current, S placed) {
        return Objects.equals(current, placed);
    }

    /** Splits entries into the ones to restore and the ones a player has changed since. */
    public static <S> Plan<S> plan(List<Entry<S>> entries, LongFunction<S> currentState) {
        List<Entry<S>> restore = new ArrayList<>();
        List<Entry<S>> skipped = new ArrayList<>();
        for (Entry<S> e : entries) {
            if (shouldRestore(currentState.apply(e.pos()), e.placed())) {
                restore.add(e);
            } else {
                skipped.add(e);
            }
        }
        return new Plan<>(restore, skipped);
    }

    /** The fight is over when the boss is destroyed (killed/discarded), not when merely unloaded. */
    public static boolean restoresOnRemoval(boolean removalDestroysEntity) {
        return removalDestroysEntity;
    }

    /**
     * Orphan check: the boss is gone (dead while unloaded, other dimension, deleted) only when its
     * arena area and entities are loaded and it still isn't there.
     *
     * @return the new missing-check counter; restore when it reaches {@link #ORPHAN_CHECKS_BEFORE_RESTORE}
     */
    public static int nextMissingCount(int missing, boolean entityPresent, boolean areaAndEntitiesLoaded) {
        if (entityPresent) return 0;
        if (!areaAndEntitiesLoaded) return missing;
        return missing + 1;
    }

    public static boolean shouldRestoreOrphan(int missing) {
        return missing >= ORPHAN_CHECKS_BEFORE_RESTORE;
    }

    public static <S> Palette<S> encode(List<S> states) {
        List<S> palette = new ArrayList<>();
        Map<S, Integer> index = new HashMap<>();
        int[] indices = new int[states.size()];
        for (int i = 0; i < states.size(); i++) {
            S s = states.get(i);
            Integer idx = index.get(s);
            if (idx == null) {
                idx = palette.size();
                palette.add(s);
                index.put(s, idx);
            }
            indices[i] = idx;
        }
        return new Palette<>(palette, indices);
    }

    public static <S> List<S> decode(Palette<S> palette) {
        List<S> out = new ArrayList<>(palette.indices().length);
        for (int idx : palette.indices()) {
            out.add(palette.states().get(idx));
        }
        return out;
    }
}
