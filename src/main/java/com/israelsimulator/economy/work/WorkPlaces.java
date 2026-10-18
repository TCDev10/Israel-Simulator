package com.israelsimulator.economy.work;

import com.israelsimulator.festival.FestivalManager;
import com.israelsimulator.festival.FestivalType;
import com.israelsimulator.world.biome.ModBiomes;
import com.israelsimulator.world.structure.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Where and when work happens. */
public final class WorkPlaces {
    private WorkPlaces() {}

    public static boolean isShabbat(Level level) {
        return FestivalManager.getCurrentFestival(level.getGameTime()) == FestivalType.SHABBAT;
    }

    /** Tel Aviv's Urban Area, or inside a Startup Office. */
    public static boolean isTechHub(ServerLevel level, BlockPos pos) {
        if (level.getBiome(pos).is(ModBiomes.URBAN_AREA)) return true;
        return level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(ModStructures.STARTUP_OFFICE)
                .map(s -> level.structureManager().getStructureWithPieceAt(pos, s.value()).isValid())
                .orElse(false);
    }

    /** Cities where delivery gigs exist. */
    public static boolean isCity(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).is(ModBiomes.URBAN_AREA) || level.getBiome(pos).is(ModBiomes.JERUSALEM);
    }
}
