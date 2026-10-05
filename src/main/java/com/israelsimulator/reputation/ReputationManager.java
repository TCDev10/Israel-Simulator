package com.israelsimulator.reputation;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/**
 * Server-authoritative reputation manager tracking faction standing (GAME_DESIGN.md §31, TODO §31).
 *
 * <p>Scores live in {@link ReputationScores} (world SavedData), not in a static map.
 * A server restart must not reset faction standing.</p>
 */
public final class ReputationManager {

    private ReputationManager() {}

    public static ReputationScores get(ServerLevel level) {
        return ReputationScores.get(level);
    }

    public static int getReputation(ReputationScores scores, UUID playerId, ReputationFaction faction) {
        if (scores == null) {
            return 0;
        }
        return scores.getScore(playerId, faction);
    }

    public static void setReputation(ReputationScores scores, UUID playerId, ReputationFaction faction, int value) {
        if (scores == null) {
            return;
        }
        scores.setScore(playerId, faction, value);
    }

    public static int adjustReputation(ReputationScores scores, UUID playerId, ReputationFaction faction, int delta) {
        if (scores == null) {
            return 0;
        }
        return scores.adjustScore(playerId, faction, delta);
    }

    public static ReputationTier getTier(ReputationScores scores, UUID playerId, ReputationFaction faction) {
        return ReputationTier.fromScore(getReputation(scores, playerId, faction));
    }

    public static double getPriceModifier(ReputationScores scores, UUID playerId, ReputationFaction faction) {
        return getTier(scores, playerId, faction).getPriceModifier();
    }

    public static boolean canAccessSpecialTrades(ReputationScores scores, UUID playerId, ReputationFaction faction) {
        return getTier(scores, playerId, faction).canAccessSpecialTrades();
    }

    public static boolean canAccessRareTrades(ReputationScores scores, UUID playerId, ReputationFaction faction) {
        return getTier(scores, playerId, faction).canAccessRareTrades();
    }

    public static void clearForPlayer(ReputationScores scores, UUID playerId) {
        if (scores != null) {
            scores.clearPlayer(playerId);
        }
    }

    public static void clearAll(ReputationScores scores) {
        if (scores != null) {
            scores.clearAll();
        }
    }
}
