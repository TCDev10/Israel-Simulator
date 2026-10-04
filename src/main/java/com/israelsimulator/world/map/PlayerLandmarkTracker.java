package com.israelsimulator.world.map;

import com.israelsimulator.audio.ModAudioManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative landmark discovery tracking system (GAME_DESIGN.md §8–10, TODO §48).
 */
public final class PlayerLandmarkTracker {

    private static final Map<UUID, Set<String>> DISCOVERED_LANDMARKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<IsraelRegion>> VISITED_REGIONS = new ConcurrentHashMap<>();
    public static final double DISCOVERY_RADIUS_SQUARED = 48.0 * 48.0; // 48 blocks detection radius

    private PlayerLandmarkTracker() {}

    /**
     * Checks if player is within range of any undiscovered landmark.
     */
    public static void checkProximityAndDiscover(ServerPlayer player) {
        if (player == null || !player.isAlive()) return;

        BlockPos playerPos = player.blockPosition();
        UUID uuid = player.getUUID();
        Set<String> discovered = DISCOVERED_LANDMARKS.computeIfAbsent(uuid, k -> Collections.synchronizedSet(new HashSet<>()));
        Set<IsraelRegion> visited = VISITED_REGIONS.computeIfAbsent(uuid, k -> Collections.synchronizedSet(new HashSet<>()));

        for (Landmark landmark : Landmark.values()) {
            if (discovered.contains(landmark.getId())) {
                continue;
            }

            BlockPos target = landmark.getDefaultPos();
            double distSq = playerPos.distSqr(target);
            if (distSq <= DISCOVERY_RADIUS_SQUARED) {
                // Discovered landmark!
                discovered.add(landmark.getId());
                visited.add(landmark.getRegion());

                // Audio feedback
                ModAudioManager.playDiscoveryFanfare(player);

                // Chat announcement
                player.sendSystemMessage(
                        Component.literal("★ ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.translatable("message.israel_simulator.landmark_discovered", landmark.getDisplayName())
                                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                                .append(Component.literal(" (" + landmark.getRegion().getDisplayName() + ")")
                                        .withStyle(ChatFormatting.AQUA))
                );
                player.sendSystemMessage(
                        Component.literal("  " + landmark.getDescription())
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                );

                // Exploration XP reward
                player.giveExperiencePoints(100);

                int total = Landmark.values().length;
                int current = discovered.size();
                player.sendSystemMessage(
                        Component.translatable("message.israel_simulator.discovery_progress", current, total)
                                .withStyle(ChatFormatting.GREEN)
                );
            }
        }
    }

    public static boolean isDiscovered(UUID playerUuid, Landmark landmark) {
        Set<String> set = DISCOVERED_LANDMARKS.get(playerUuid);
        return set != null && set.contains(landmark.getId());
    }

    public static Set<String> getDiscoveredLandmarks(UUID playerUuid) {
        Set<String> set = DISCOVERED_LANDMARKS.get(playerUuid);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(set));
    }

    public static Set<IsraelRegion> getVisitedRegions(UUID playerUuid) {
        Set<IsraelRegion> set = VISITED_REGIONS.get(playerUuid);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(set));
    }

    public static int getDiscoveredCount(UUID playerUuid) {
        Set<String> set = DISCOVERED_LANDMARKS.get(playerUuid);
        return set == null ? 0 : set.size();
    }

    public static void clearForPlayer(UUID playerUuid) {
        DISCOVERED_LANDMARKS.remove(playerUuid);
        VISITED_REGIONS.remove(playerUuid);
    }
}
