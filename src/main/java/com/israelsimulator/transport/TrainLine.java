package com.israelsimulator.transport;

import com.israelsimulator.IsraelSimulator;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * The Tel Aviv – Jerusalem train (Israel Railways), run from the transit stop blocks.
 *
 * <p>A stop standing in the Urban Area biome is a Tel Aviv station, one in the Jerusalem biome is
 * a Jerusalem station. Riding from one end takes you to the nearest station at the other end; if
 * nobody has built one there yet, the train stops at the nearest generated city of that end.</p>
 */
public final class TrainLine {

    public enum Terminal {
        TEL_AVIV("tel_aviv", "urban_area", "message.israel_simulator.train_terminal.tel_aviv"),
        JERUSALEM("jerusalem", "jerusalem", "message.israel_simulator.train_terminal.jerusalem");

        private final String id;
        private final ResourceKey<Biome> biome;
        private final TagKey<Structure> cities;
        private final String nameKey;

        Terminal(String id, String biome, String nameKey) {
            this.id = id;
            this.nameKey = nameKey;
            this.biome = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, biome));
            this.cities = TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "train/" + id));
        }

        public String id() {
            return id;
        }

        /** Translation key of the city name shown in train messages. */
        public String nameKey() {
            return nameKey;
        }

        public ResourceKey<Biome> biome() {
            return biome;
        }

        public TagKey<Structure> cities() {
            return cities;
        }

        public Terminal other() {
            return this == TEL_AVIV ? JERUSALEM : TEL_AVIV;
        }
    }

    /** Fare without a Rav-Kav, in Shekels. */
    public static final int FARE = TransportType.TRAIN.getFareInShekels();
    /** Search radius (chunks) for the destination city when no station exists there. */
    static final int CITY_SEARCH_RADIUS_CHUNKS = 100;

    /** Where the train goes from a station, or null if none. */
    public record Trip(Terminal from, Terminal to, BlockPos arrival, boolean toStation) {}

    private TrainLine() {}

    public static Optional<Terminal> terminalAt(ServerLevel level, BlockPos pos) {
        for (Terminal t : Terminal.values()) {
            if (level.getBiome(pos).is(t.biome())) return Optional.of(t);
        }
        return Optional.empty();
    }

    public static Trip plan(ServerLevel level, BlockPos stop) {
        Terminal from = terminalAt(level, stop).orElse(null);
        if (from == null) return null;
        Terminal to = from.other();
        TransitStations stations = TransitStations.get(level);
        BlockPos best = null;
        for (BlockPos p : stations.list().stream().sorted(Comparator.comparingDouble(p -> p.distSqr(stop))).toList()) {
            if (p.equals(stop)) continue;
            if (!(level.getBlockState(p).getBlock() instanceof TransportStopBlock)) {
                stations.remove(p);
                continue;
            }
            if (terminalAt(level, p).orElse(null) == to) {
                best = p;
                break;
            }
        }
        if (best != null) {
            return new Trip(from, to, standingSpot(level, best), true);
        }
        BlockPos city = level.findNearestMapStructure(to.cities(), stop, CITY_SEARCH_RADIUS_CHUNKS, false);
        if (city == null) return null;
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, city);
        return new Trip(from, to, surface, false);
    }

    /** A free two-block spot next to the station (platform side), or on top of it. */
    static BlockPos standingSpot(ServerLevel level, BlockPos stop) {
        for (BlockPos p : new BlockPos[]{stop.north(), stop.south(), stop.east(), stop.west()}) {
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                    && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)) {
                return p;
            }
        }
        return stop.above();
    }
}
