package com.israelsimulator.entity.boss;

/**
 * State machine states for the Bibi Boss encounter (GAME_DESIGN.md §41.2, TODO §43).
 */
public enum BibiBossState {
    IDLE,
    ALERT,
    COMBAT,
    ENRAGED,
    DEFEATED;

    public boolean isAggressive() {
        return this == COMBAT || this == ENRAGED;
    }

    public boolean isEnraged() {
        return this == ENRAGED;
    }
}
