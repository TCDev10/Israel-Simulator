package com.israelsimulator.mossad;

import com.israelsimulator.IsraelSimulator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * World-saved Mossad state per player: reputation, active mission and progress, completed missions.
 * Stored in the overworld's data storage so it is shared across dimensions.
 */
public final class MossadData extends SavedData {
    public static final class Entry {
        public int reputation;
        public String mission = "";
        public int progress;
        public long target = BlockPos.ZERO.asLong();
        public String informant = "";
        public int completed;
        /** Bit per {@link MossadMission#ordinal()} completed at least once (first-time weapon rewards). */
        public int doneMask;

        public MossadMission activeMission() {
            return mission.isEmpty() ? null : MossadMission.byId(mission);
        }

        public void clearMission() {
            mission = "";
            progress = 0;
            target = BlockPos.ZERO.asLong();
            informant = "";
        }
    }

    record Rec(UUID player, int reputation, String mission, int progress, long target, String informant, int completed, int doneMask) {
        static final Codec<Rec> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("player").forGetter(Rec::player),
                Codec.INT.optionalFieldOf("reputation", 0).forGetter(Rec::reputation),
                Codec.STRING.optionalFieldOf("mission", "").forGetter(Rec::mission),
                Codec.INT.optionalFieldOf("progress", 0).forGetter(Rec::progress),
                Codec.LONG.optionalFieldOf("target", 0L).forGetter(Rec::target),
                Codec.STRING.optionalFieldOf("informant", "").forGetter(Rec::informant),
                Codec.INT.optionalFieldOf("completed", 0).forGetter(Rec::completed),
                Codec.INT.optionalFieldOf("done_mask", 0).forGetter(Rec::doneMask)
        ).apply(i, Rec::new));
    }

    public static final Codec<MossadData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Rec.CODEC.listOf().fieldOf("players").forGetter(MossadData::records)
    ).apply(i, MossadData::new));

    public static final SavedDataType<MossadData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "mossad"), MossadData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<UUID, Entry> players = new LinkedHashMap<>();

    public MossadData() {}

    MossadData(List<Rec> recs) {
        for (Rec r : recs) {
            Entry e = new Entry();
            e.reputation = MossadRules.clampReputation(r.reputation());
            e.mission = r.mission();
            e.progress = r.progress();
            e.target = r.target();
            e.informant = r.informant();
            e.completed = r.completed();
            e.doneMask = r.doneMask();
            players.put(r.player(), e);
        }
    }

    private List<Rec> records() {
        List<Rec> out = new ArrayList<>();
        players.forEach((id, e) -> out.add(new Rec(id, e.reputation, e.mission, e.progress, e.target, e.informant, e.completed, e.doneMask)));
        return out;
    }

    public static MossadData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public Entry entry(UUID player) {
        return players.computeIfAbsent(player, id -> new Entry());
    }

    public int reputation(UUID player) {
        Entry e = players.get(player);
        return e == null ? 0 : e.reputation;
    }

    public int adjustReputation(UUID player, int delta) {
        Entry e = entry(player);
        e.reputation = MossadRules.clampReputation(e.reputation + delta);
        setDirty();
        return e.reputation;
    }
}
