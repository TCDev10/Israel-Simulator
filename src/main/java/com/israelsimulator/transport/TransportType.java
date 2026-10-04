package com.israelsimulator.transport;

/**
 * Types of transportation available in Israel-Simulator (GAME_DESIGN.md §32, TODO §47).
 */
public enum TransportType {
    WALKING("Walking", 0, 1.2F),
    BICYCLE("Bicycle", 0, 1.8F),
    BUS("Egged Bus", 5, 2.5F),
    TRAIN("Israel Railways Fast Train", 10, 4.0F),
    TAXI("Sherut Taxi", 8, 2.2F),
    BOAT("Mediterranean Ferry", 6, 2.0F);

    private final String displayName;
    private final int fareInShekels;
    private final float speedFactor;

    TransportType(String displayName, int fareInShekels, float speedFactor) {
        this.displayName = displayName;
        this.fareInShekels = fareInShekels;
        this.speedFactor = speedFactor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getFareInShekels() {
        return fareInShekels;
    }

    public float getSpeedFactor() {
        return speedFactor;
    }
}
