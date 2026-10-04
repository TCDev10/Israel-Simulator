package com.israelsimulator.reputation;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative reputation manager tracking faction standing (GAME_DESIGN.md §31, TODO §31).
 */
public final class ReputationManager {

    private static final Map<UUID, Map<ReputationFaction, Integer>> REPUTATION_MAP = new ConcurrentHashMap<>();

    private ReputationManager() {}

    private static Map<ReputationFaction, Integer> getPlayerMap(UUID playerId) {
        return REPUTATION_MAP.computeIfAbsent(playerId, id -> new EnumMap<>(ReputationFaction.class));
    }

    public static int getReputation(UUID playerId, ReputationFaction faction) {
        if (playerId == null || faction == null) {
            return 0;
        }
        return getPlayerMap(playerId).getOrDefault(faction, 0);
    }

    public static void setReputation(UUID playerId, ReputationFaction faction, int value) {
        if (playerId == null || faction == null) {
            return;
        }
        getPlayerMap(playerId).put(faction, Math.clamp(value, -100, 100));
    }

    public static int adjustReputation(UUID playerId, ReputationFaction faction, int delta) {
        if (playerId == null || faction == null) {
            return 0;
        }
        Map<ReputationFaction, Integer> map = getPlayerMap(playerId);
        int current = map.getOrDefault(faction, 0);
        int updated = Math.clamp(current + delta, -100, 100);
        map.put(faction, updated);
        return updated;
    }

    public static ReputationTier getTier(UUID playerId, ReputationFaction faction) {
        return ReputationTier.fromScore(getReputation(playerId, faction));
    }

    public static double getPriceModifier(UUID playerId, ReputationFaction faction) {
        return getTier(playerId, faction).getPriceModifier();
    }

    public static boolean canAccessSpecialTrades(UUID playerId, ReputationFaction faction) {
        return getTier(playerId, faction).canAccessSpecialTrades();
    }

    public static boolean canAccessRareTrades(UUID playerId, ReputationFaction faction) {
        return getTier(playerId, faction).canAccessRareTrades();
    }

    public static void clearForPlayer(UUID playerId) {
        if (playerId != null) {
            REPUTATION_MAP.remove(playerId);
        }
    }

    public static void clearAll() {
        REPUTATION_MAP.clear();
    }
}
