package com.israelsimulator.transport;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global transit network interconnecting cities, rural hubs, and coastal ports (GAME_DESIGN.md §32, TODO §47).
 */
public final class TransportNetwork {

    public record TransportStop(String id, String displayName, String cityRegion, TransportType primaryType, BlockPos defaultPos) {}

    private static final Map<String, TransportStop> STOPS = new ConcurrentHashMap<>();
    private static final List<String> STOP_ORDER = new ArrayList<>();

    static {
        registerStop(new TransportStop("tel_aviv_central", "Tel Aviv Savidor Central", "Tel Aviv", TransportType.TRAIN, new BlockPos(100, 70, 100)));
        registerStop(new TransportStop("jaffa_clock_tower", "Jaffa Clock Tower Stop", "Jaffa", TransportType.BUS, new BlockPos(150, 68, 500)));
        registerStop(new TransportStop("jaffa_port", "Old Jaffa Port Pier", "Jaffa", TransportType.BOAT, new BlockPos(120, 64, 600)));
        registerStop(new TransportStop("jerusalem_navon", "Jerusalem Yitzhak Navon Station", "Jerusalem", TransportType.TRAIN, new BlockPos(800, 110, 800)));
        registerStop(new TransportStop("dead_sea_resort", "Dead Sea Ein Gedi Stop", "Dead Sea", TransportType.BUS, new BlockPos(1500, 45, 1200)));
        registerStop(new TransportStop("galilee_hub", "Galilee Agricultural Moshav Hub", "Galilee", TransportType.BUS, new BlockPos(-600, 75, -800)));
    }

    private TransportNetwork() {}

    public static void registerStop(TransportStop stop) {
        STOPS.put(stop.id(), stop);
        if (!STOP_ORDER.contains(stop.id())) {
            STOP_ORDER.add(stop.id());
        }
    }

    public static TransportStop getStop(String id) {
        return STOPS.get(id);
    }

    public static List<TransportStop> getAllStops() {
        return Collections.unmodifiableList(new ArrayList<>(STOPS.values()));
    }

    /**
     * Gets the nearest stop to a given block position.
     */
    public static TransportStop getNearestStop(BlockPos pos) {
        if (STOPS.isEmpty() || pos == null) return null;
        TransportStop nearest = null;
        double minDistanceSq = Double.MAX_VALUE;
        for (TransportStop stop : STOPS.values()) {
            double distSq = stop.defaultPos().distSqr(pos);
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                nearest = stop;
            }
        }
        return nearest;
    }

    /**
     * Gets the next destination stop along the transit line.
     */
    public static TransportStop getNextStop(String currentStopId) {
        if (STOP_ORDER.isEmpty()) return null;
        int idx = STOP_ORDER.indexOf(currentStopId);
        if (idx < 0 || idx >= STOP_ORDER.size() - 1) {
            return STOPS.get(STOP_ORDER.get(0));
        }
        return STOPS.get(STOP_ORDER.get(idx + 1));
    }
}
