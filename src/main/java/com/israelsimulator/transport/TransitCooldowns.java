package com.israelsimulator.transport;

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
 * Per-dimension saved data for transport stop use times (GAME_DESIGN.md §32).
 *
 * <p>Stored with the world, not in a static map, so a server restart keeps the
 * spam cooldown for players and the villager sound interval. Only the server
 * level that handled the transit reads and writes it.</p>
 */
public final class TransitCooldowns extends SavedData {
    public static final Codec<TransitCooldowns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UseRecord.CODEC.listOf().fieldOf("transits").forGetter(TransitCooldowns::useRecords)
    ).apply(instance, TransitCooldowns::new));

    public static final SavedDataType<TransitCooldowns> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "transit_times"),
            TransitCooldowns::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Long> lastUseTimes = new LinkedHashMap<>();

    public TransitCooldowns() {}

    public TransitCooldowns(List<UseRecord> records) {
        if (records == null) {
            return;
        }
        for (UseRecord record : records) {
            if (record != null && record.entity() != null) {
                lastUseTimes.put(record.entity(), record.time());
            }
        }
    }

    public static TransitCooldowns get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Long getLastUseTime(UUID entityId) {
        if (entityId == null) {
            return null;
        }
        return lastUseTimes.get(entityId);
    }

    public void setLastUseTime(UUID entityId, long time) {
        if (entityId == null) {
            return;
        }
        lastUseTimes.put(entityId, time);
        setDirty();
    }

    public void clearCooldown(UUID entityId) {
        if (entityId != null && lastUseTimes.remove(entityId) != null) {
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

    public record UseRecord(UUID entity, long time) {
        public static final Codec<UseRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("entity").forGetter(UseRecord::entity),
                Codec.LONG.fieldOf("time").forGetter(UseRecord::time)
        ).apply(instance, UseRecord::new));
    }
}
