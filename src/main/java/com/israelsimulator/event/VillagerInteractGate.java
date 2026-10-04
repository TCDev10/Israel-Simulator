package com.israelsimulator.event;

/**
 * Decides whether a villager interaction replaces the vanilla trading GUI.
 * Neither a missed regional trade nor an ordinary villager is enough to cancel.
 */
public final class VillagerInteractGate {
    private VillagerInteractGate() {}

    public static boolean shouldCancelVanillaGui(boolean regionalTradeHandled, boolean registeredModNpcHandled) {
        return regionalTradeHandled || registeredModNpcHandled;
    }
}
