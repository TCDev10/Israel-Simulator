package com.israelsimulator.transport;

import com.israelsimulator.IsraelSimulator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Per-dimension list of the transit stop blocks that exist in the world (placed by players or
 * by commands). The Tel Aviv–Jerusalem train runs between these, so a line is made of real
 * stations instead of fixed coordinates. Stops are added in {@code onPlace} and removed when the
 * block goes away; stale entries are also pruned when a ride finds no stop block there.
 */
public final class TransitStations extends SavedData {
    public static final Codec<TransitStations> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("stations").forGetter(TransitStations::list)
    ).apply(instance, TransitStations::new));

    public static final SavedDataType<TransitStations> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "transit_stations"),
            TransitStations::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Set<BlockPos> stations = new LinkedHashSet<>();

    public TransitStations() {}

    public TransitStations(List<BlockPos> positions) {
        if (positions != null) {
            for (BlockPos p : positions) {
                if (p != null) stations.add(p.immutable());
            }
        }
    }

    public static TransitStations get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void add(BlockPos pos) {
        if (stations.add(pos.immutable())) setDirty();
    }

    public void remove(BlockPos pos) {
        if (stations.remove(pos)) setDirty();
    }

    public List<BlockPos> list() {
        return new ArrayList<>(stations);
    }
}
