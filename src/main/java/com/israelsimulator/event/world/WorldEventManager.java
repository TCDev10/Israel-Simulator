package com.israelsimulator.event.world;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

/**
 * Server-authoritative lifecycle controller for dynamic world events (GAME_DESIGN.md §37, §38, TODO §37, §38).
 * Enforces start/end conditions, participation duration, anti-AFK safeguards, and rewards.
 */
public final class WorldEventManager {

    public static final int EVENT_RADIUS = 24;

    private static final Map<WorldEventType, ActiveEventData> ACTIVE_EVENTS = new ConcurrentHashMap<>();

    public static class ActiveEventData {
        private final WorldEventType type;
        private final BlockPos center;
        private final long startTick;
        private final long endTick;
        private final Map<UUID, Integer> participationTicks = new ConcurrentHashMap<>();
        private final Set<UUID> rewardedPlayers = Collections.synchronizedSet(new HashSet<>());
        private WorldEventStatus status = WorldEventStatus.ACTIVE;

        public ActiveEventData(WorldEventType type, BlockPos center, long startTick) {
            this.type = type;
            this.center = center != null ? center : BlockPos.ZERO;
            this.startTick = startTick;
            this.endTick = startTick + type.getDurationTicks();
        }

        public WorldEventType getType() {
            return type;
        }

        public BlockPos getCenter() {
            return center;
        }

        public long getStartTick() {
            return startTick;
        }

        public long getEndTick() {
            return endTick;
        }

        public WorldEventStatus getStatus() {
            return status;
        }

        public void setStatus(WorldEventStatus status) {
            this.status = status;
        }

        public void recordTicks(UUID playerId, int deltaTicks) {
            participationTicks.merge(playerId, deltaTicks, Integer::sum);
        }

        public void setTicks(UUID playerId, int ticks) {
            participationTicks.put(playerId, ticks);
        }

        public int getTicks(UUID playerId) {
            return participationTicks.getOrDefault(playerId, 0);
        }

        public boolean hasBeenRewarded(UUID playerId) {
            return rewardedPlayers.contains(playerId);
        }

        public void markRewarded(UUID playerId) {
            rewardedPlayers.add(playerId);
        }
    }

    private WorldEventManager() {}

    public static void startEvent(WorldEventType type, BlockPos center, long gameTime) {
        if (type == null) {
            return;
        }
        ACTIVE_EVENTS.put(type, new ActiveEventData(type, center, gameTime));
    }

    public static void endEvent(WorldEventType type) {
        if (type != null) {
            ActiveEventData data = ACTIVE_EVENTS.get(type);
            if (data != null) {
                data.setStatus(WorldEventStatus.COMPLETED);
            }
            ACTIVE_EVENTS.remove(type);
        }
    }

    public static boolean isEventActive(WorldEventType type) {
        ActiveEventData data = ACTIVE_EVENTS.get(type);
        return data != null && data.getStatus() == WorldEventStatus.ACTIVE;
    }

    public static ActiveEventData getEventData(WorldEventType type) {
        return ACTIVE_EVENTS.get(type);
    }

    public static void recordParticipation(WorldEventType type, UUID playerId, int ticks) {
        ActiveEventData data = ACTIVE_EVENTS.get(type);
        if (data != null && playerId != null) {
            data.recordTicks(playerId, ticks);
        }
    }

    public static int getParticipationTicks(WorldEventType type, UUID playerId) {
        ActiveEventData data = ACTIVE_EVENTS.get(type);
        return data != null && playerId != null ? data.getTicks(playerId) : 0;
    }

    /**
     * Determines whether a player has satisfied the minimum participation requirement
     * and has not yet claimed the reward (anti-AFK and anti-duplication).
     */
    public static boolean canClaimReward(WorldEventType type, UUID playerId) {
        ActiveEventData data = ACTIVE_EVENTS.get(type);
        if (data == null || playerId == null) {
            return false;
        }
        if (data.hasBeenRewarded(playerId)) {
            return false; // Prevent repeated farming
        }
        return data.getTicks(playerId) >= type.getMinParticipationTicks();
    }

    public static boolean claimReward(WorldEventType type, UUID playerId) {
        if (canClaimReward(type, playerId)) {
            ActiveEventData data = ACTIVE_EVENTS.get(type);
            if (data != null) {
                data.markRewarded(playerId);
                return true;
            }
        }
        return false;
    }

    public static void clearAll() {
        ACTIVE_EVENTS.clear();
    }
}
