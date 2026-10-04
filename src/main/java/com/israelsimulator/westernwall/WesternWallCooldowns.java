package com.israelsimulator.westernwall;

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
 * Per-dimension saved data for Western Wall prayer times (GAME_DESIGN.md §11, §53).
 *
 * <p>Stored with the world, not in a static map, so a server restart keeps the cooldown.
 * Only the server level that handled the prayer reads and writes it.</p>
 */
public final class WesternWallCooldowns extends SavedData {
    public static final Codec<WesternWallCooldowns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PrayerRecord.CODEC.listOf().fieldOf("prayers").forGetter(WesternWallCooldowns::prayerRecords)
    ).apply(instance, WesternWallCooldowns::new));

    public static final SavedDataType<WesternWallCooldowns> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "western_wall_prayers"),
            WesternWallCooldowns::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Long> lastPrayerTimes = new LinkedHashMap<>();

    public WesternWallCooldowns() {}

    public WesternWallCooldowns(List<PrayerRecord> records) {
        if (records == null) {
            return;
        }
        for (PrayerRecord record : records) {
            if (record != null && record.player() != null) {
                lastPrayerTimes.put(record.player(), record.time());
            }
        }
    }

    public static WesternWallCooldowns get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Long getLastPrayerTime(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return lastPrayerTimes.get(playerId);
    }

    public void setLastPrayerTime(UUID playerId, long time) {
        if (playerId == null) {
            return;
        }
        lastPrayerTimes.put(playerId, time);
        setDirty();
    }

    public void clearCooldown(UUID playerId) {
        if (playerId != null && lastPrayerTimes.remove(playerId) != null) {
            setDirty();
        }
    }

    private List<PrayerRecord> prayerRecords() {
        List<PrayerRecord> records = new ArrayList<>(lastPrayerTimes.size());
        for (Map.Entry<UUID, Long> entry : lastPrayerTimes.entrySet()) {
            records.add(new PrayerRecord(entry.getKey(), entry.getValue()));
        }
        return records;
    }

    public record PrayerRecord(UUID player, long time) {
        public static final Codec<PrayerRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("player").forGetter(PrayerRecord::player),
                Codec.LONG.fieldOf("time").forGetter(PrayerRecord::time)
        ).apply(instance, PrayerRecord::new));
    }
}
