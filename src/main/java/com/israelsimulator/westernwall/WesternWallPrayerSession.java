package com.israelsimulator.westernwall;

import net.minecraft.core.BlockPos;

/** In-progress Western Wall prayer on the server (or mirrored on clients for posing). */
public record WesternWallPrayerSession(BlockPos wallPos, long startGameTime, double startX, double startY, double startZ) {
    public static final int DURATION_TICKS = 60;
}
