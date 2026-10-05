package com.israelsimulator.quest;

import com.israelsimulator.IsraelSimulator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Per-dimension saved data for completed discovery quests (GAME_DESIGN.md §52).
 *
 * <p>Stored with the world, not in a static map, so a server restart keeps finished
 * quests and prevents re-claiming rewards. These are completion flags, not cooldowns.</p>
 */
public final class CompletedQuests extends SavedData {
    public static final Codec<CompletedQuests> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            QuestRecord.CODEC.listOf().fieldOf("completed").forGetter(CompletedQuests::questRecords)
    ).apply(instance, CompletedQuests::new));

    public static final SavedDataType<CompletedQuests> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "completed_quests"),
            CompletedQuests::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, EnumSet<DiscoveryQuest>> completed = new LinkedHashMap<>();

    public CompletedQuests() {}

    public CompletedQuests(List<QuestRecord> records) {
        if (records == null) {
            return;
        }
        for (QuestRecord record : records) {
            if (record == null || record.player() == null || record.quest() == null) {
                continue;
            }
            DiscoveryQuest quest = parseQuest(record.quest());
            if (quest == null) {
                continue;
            }
            completed.computeIfAbsent(record.player(), id -> EnumSet.noneOf(DiscoveryQuest.class)).add(quest);
        }
    }

    public static CompletedQuests get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static DiscoveryQuest parseQuest(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return DiscoveryQuest.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public boolean isCompleted(UUID playerId, DiscoveryQuest quest) {
        if (playerId == null || quest == null) {
            return false;
        }
        EnumSet<DiscoveryQuest> set = completed.get(playerId);
        return set != null && set.contains(quest);
    }

    /**
     * Marks the quest completed. Returns false if it was already completed.
     */
    public boolean markCompleted(UUID playerId, DiscoveryQuest quest) {
        if (playerId == null || quest == null) {
            return false;
        }
        EnumSet<DiscoveryQuest> set = completed.computeIfAbsent(playerId, id -> EnumSet.noneOf(DiscoveryQuest.class));
        if (!set.add(quest)) {
            return false;
        }
        setDirty();
        return true;
    }

    public Set<DiscoveryQuest> getCompleted(UUID playerId) {
        if (playerId == null) {
            return Collections.emptySet();
        }
        EnumSet<DiscoveryQuest> set = completed.get(playerId);
        if (set == null || set.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(set));
    }

    public void clearPlayer(UUID playerId) {
        if (playerId != null && completed.remove(playerId) != null) {
            setDirty();
        }
    }

    public void clearAll() {
        if (!completed.isEmpty()) {
            completed.clear();
            setDirty();
        }
    }

    private List<QuestRecord> questRecords() {
        List<QuestRecord> records = new ArrayList<>();
        for (Map.Entry<UUID, EnumSet<DiscoveryQuest>> entry : completed.entrySet()) {
            for (DiscoveryQuest quest : entry.getValue()) {
                records.add(new QuestRecord(entry.getKey(), quest.name()));
            }
        }
        return records;
    }

    public record QuestRecord(UUID player, String quest) {
        public static final Codec<QuestRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("player").forGetter(QuestRecord::player),
                Codec.STRING.fieldOf("quest").forGetter(QuestRecord::quest)
        ).apply(instance, QuestRecord::new));
    }
}
