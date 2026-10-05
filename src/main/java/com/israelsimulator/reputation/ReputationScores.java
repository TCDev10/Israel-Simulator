package com.israelsimulator.reputation;

import com.israelsimulator.IsraelSimulator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Per-dimension saved data for faction reputation scores (GAME_DESIGN.md §31).
 *
 * <p>Stored with the world, not in a static map, so a server restart keeps standing.
 * Only the server level that handled the change reads and writes it. These are scores,
 * not cooldowns.</p>
 */
public final class ReputationScores extends SavedData {
    public static final Codec<ReputationScores> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScoreRecord.CODEC.listOf().fieldOf("scores").forGetter(ReputationScores::scoreRecords)
    ).apply(instance, ReputationScores::new));

    public static final SavedDataType<ReputationScores> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "reputation_scores"),
            ReputationScores::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Map<ReputationFaction, Integer>> scores = new LinkedHashMap<>();

    public ReputationScores() {}

    public ReputationScores(List<ScoreRecord> records) {
        if (records == null) {
            return;
        }
        for (ScoreRecord record : records) {
            if (record == null || record.player() == null || record.faction() == null) {
                continue;
            }
            ReputationFaction faction = parseFaction(record.faction());
            if (faction == null) {
                continue;
            }
            scores.computeIfAbsent(record.player(), id -> new EnumMap<>(ReputationFaction.class))
                    .put(faction, Math.clamp(record.score(), -100, 100));
        }
    }

    public static ReputationScores get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static ReputationFaction parseFaction(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) {
            return null;
        }
        for (ReputationFaction faction : ReputationFaction.values()) {
            if (faction.getId().equals(idOrName) || faction.name().equals(idOrName)) {
                return faction;
            }
        }
        return null;
    }

    private Map<ReputationFaction, Integer> playerMap(UUID playerId) {
        return scores.computeIfAbsent(playerId, id -> new EnumMap<>(ReputationFaction.class));
    }

    public int getScore(UUID playerId, ReputationFaction faction) {
        if (playerId == null || faction == null) {
            return 0;
        }
        Map<ReputationFaction, Integer> map = scores.get(playerId);
        if (map == null) {
            return 0;
        }
        return map.getOrDefault(faction, 0);
    }

    public void setScore(UUID playerId, ReputationFaction faction, int value) {
        if (playerId == null || faction == null) {
            return;
        }
        playerMap(playerId).put(faction, Math.clamp(value, -100, 100));
        setDirty();
    }

    public int adjustScore(UUID playerId, ReputationFaction faction, int delta) {
        if (playerId == null || faction == null) {
            return 0;
        }
        int updated = Math.clamp(getScore(playerId, faction) + delta, -100, 100);
        playerMap(playerId).put(faction, updated);
        setDirty();
        return updated;
    }

    public void clearPlayer(UUID playerId) {
        if (playerId != null && scores.remove(playerId) != null) {
            setDirty();
        }
    }

    public void clearAll() {
        if (!scores.isEmpty()) {
            scores.clear();
            setDirty();
        }
    }

    private List<ScoreRecord> scoreRecords() {
        List<ScoreRecord> records = new ArrayList<>();
        for (Map.Entry<UUID, Map<ReputationFaction, Integer>> playerEntry : scores.entrySet()) {
            for (Map.Entry<ReputationFaction, Integer> factionEntry : playerEntry.getValue().entrySet()) {
                records.add(new ScoreRecord(
                        playerEntry.getKey(),
                        factionEntry.getKey().getId(),
                        factionEntry.getValue()
                ));
            }
        }
        return records;
    }

    public record ScoreRecord(UUID player, String faction, int score) {
        public static final Codec<ScoreRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("player").forGetter(ScoreRecord::player),
                Codec.STRING.fieldOf("faction").forGetter(ScoreRecord::faction),
                Codec.INT.fieldOf("score").forGetter(ScoreRecord::score)
        ).apply(instance, ScoreRecord::new));
    }
}
