package com.israelsimulator.entity.boss;

/**
 * State machine states for the Bibi Boss encounter (GAME_DESIGN.md §41.2, TODO §43).
 * Phase 1: COMBAT (100% - 66% HP)
 * Phase 2: ENRAGED (66% - 33% HP, summons Donald Trump Miniboss)
 * Phase 3: DESPERATE (33% - 0% HP, summons Jeffrey Epstein Miniboss)
 */
public enum BibiBossState {
    IDLE,
    ALERT,
    COMBAT,
    ENRAGED,
    DESPERATE,
    DEFEATED;

    public boolean isAggressive() {
        return this == COMBAT || this == ENRAGED || this == DESPERATE;
    }

    public boolean isEnraged() {
        return this == ENRAGED || this == DESPERATE;
    }

    public boolean isDesperate() {
        return this == DESPERATE;
    }
}
