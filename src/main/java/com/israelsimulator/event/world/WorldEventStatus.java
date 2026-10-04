package com.israelsimulator.event.world;

/**
 * Lifecycle states for world events (GAME_DESIGN.md §37, TODO §37).
 */
public enum WorldEventStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    COOLDOWN
}
