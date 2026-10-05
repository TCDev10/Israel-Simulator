package com.israelsimulator.city.telaviv;

import com.israelsimulator.world.biome.ModBiomes;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-authoritative manager tracking player exploration, district notifications,
 * and district activity in Tel Aviv (GAME_DESIGN.md §7, §28).
 */
public final class TelAvivDistrictManager {

    private static final Map<UUID, Set<String>> VISITED_DISTRICTS = new ConcurrentHashMap<>();
    private static final Map<UUID, String> CURRENT_DISTRICT = new ConcurrentHashMap<>();

    private TelAvivDistrictManager() {}

    /**
     * Checks a player's position, updates visited district state, and delivers actionbar feedback on transition.
     */
    public static void checkPlayerDistrict(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        BlockPos pos = player.blockPosition();
        var biomeHolder = player.level().getBiome(pos);
        if (!biomeHolder.is(ModBiomes.URBAN_AREA)) {
            CURRENT_DISTRICT.remove(player.getUUID());
            return;
        }

        TelAvivDistricts district = TelAvivDistricts.getDistrictAt(pos);
        if (district == null) {
            return;
        }

        UUID playerId = player.getUUID();
        String prevDistrict = CURRENT_DISTRICT.get(playerId);

        if (!district.id().equals(prevDistrict)) {
            CURRENT_DISTRICT.put(playerId, district.id());
            Set<String> visited = VISITED_DISTRICTS.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());
            boolean isFirstVisit = visited.add(district.id());

            // Actionbar feedback: "Entering [District] - [Style]"
            String feedback = isFirstVisit
                    ? "§6[Tel Aviv] §eDiscovered §l" + district.displayName() + "§r §7(" + district.architecturalStyle() + ")"
                    : "§6[Tel Aviv] §fEntering §b" + district.displayName() + "§r";
            player.sendSystemMessage(Component.literal(feedback), true);
        }
    }

    public static Set<String> getVisitedDistricts(UUID playerId) {
        Set<String> set = VISITED_DISTRICTS.get(playerId);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    public static int getVisitedCount(UUID playerId) {
        Set<String> set = VISITED_DISTRICTS.get(playerId);
        return set != null ? set.size() : 0;
    }

    public static boolean hasVisitedAll(UUID playerId) {
        return getVisitedCount(playerId) >= TelAvivDistricts.ALL.size();
    }

    public static void clearPlayer(UUID playerId) {
        VISITED_DISTRICTS.remove(playerId);
        CURRENT_DISTRICT.remove(playerId);
    }

    public static void resetAll() {
        VISITED_DISTRICTS.clear();
        CURRENT_DISTRICT.clear();
    }
}
