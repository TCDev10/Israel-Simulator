package com.israelsimulator.item.cultural;

import com.israelsimulator.IsraelSimulator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
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
 * Per-dimension saved data for Tefillin prayer times (GAME_DESIGN.md).
 *
 * <p>Stored with the world, not in a static map, so a server restart keeps the cooldown.
 * Only the server level that handled the action reads and writes it.</p>
 */
public final class TefillinCooldowns extends SavedData {
    public static final Codec<TefillinCooldowns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UseRecord.CODEC.listOf().fieldOf("prayers").forGetter(TefillinCooldowns::useRecords)
    ).apply(instance, TefillinCooldowns::new));

    public static final SavedDataType<TefillinCooldowns> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "tefillin_prayers"),
            TefillinCooldowns::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Long> lastUseTimes = new LinkedHashMap<>();

    public TefillinCooldowns() {}

    public TefillinCooldowns(List<UseRecord> records) {
        if (records == null) {
            return;
        }
        for (UseRecord record : records) {
            if (record != null && record.player() != null) {
                lastUseTimes.put(record.player(), record.time());
            }
        }
    }

    public static TefillinCooldowns get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Long getLastUseTime(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return lastUseTimes.get(playerId);
    }

    public void setLastUseTime(UUID playerId, long time) {
        if (playerId == null) {
            return;
        }
        lastUseTimes.put(playerId, time);
        setDirty();
    }

    public void clearCooldown(UUID playerId) {
        if (playerId != null && lastUseTimes.remove(playerId) != null) {
            setDirty();
        }
    }

    private List<UseRecord> useRecords() {
        List<UseRecord> records = new ArrayList<>(lastUseTimes.size());
        for (Map.Entry<UUID, Long> entry : lastUseTimes.entrySet()) {
            records.add(new UseRecord(entry.getKey(), entry.getValue()));
        }
        return records;
    }

    public record UseRecord(UUID player, long time) {
        public static final Codec<UseRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("player").forGetter(UseRecord::player),
                Codec.LONG.fieldOf("time").forGetter(UseRecord::time)
        ).apply(instance, UseRecord::new));
    }
}
