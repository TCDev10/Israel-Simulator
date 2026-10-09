package com.israelsimulator.mossad;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.mossad.MossadAgentEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Rare night-time spawns of hostile agent pairs near players in Israeli biomes. Each player's Mossad
 * reputation lowers the chance ({@link MossadRules#spawnMultiplier}). There is no wanted/crime
 * system in the mod, so agents do not react to crimes.
 */
public final class MossadAgentSpawner {
    public static final TagKey<Biome> ISRAEL_REGION = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "is_israel_region"));

    private MossadAgentSpawner() {}

    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD || level.getGameTime() % MossadRules.SPAWN_CHECK_INTERVAL != 0
                || !level.isDarkOutside() || level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL
                || !level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MONSTERS)) {
            return;
        }
        MossadData data = MossadData.get(level.getServer());
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) {
                continue;
            }
            if (!level.getBiome(player.blockPosition()).is(ISRAEL_REGION)) {
                continue;
            }
            if (level.getRandom().nextDouble() >= MossadRules.spawnChance(data.reputation(player.getUUID()))) {
                continue;
            }
            int nearby = level.getEntitiesOfClass(MossadAgentEntity.class, player.getBoundingBox().inflate(64)).size();
            if (nearby >= MossadRules.MAX_AGENTS_NEARBY) {
                continue;
            }
            BlockPos at = MossadMissions.surfaceAround(level, player.blockPosition(), 24, 36, level.getRandom());
            int n = MossadMissions.spawnAgents(level, at, 1 + level.getRandom().nextInt(2), level.getRandom());
            if (n > 0) {
                IsraelSimulator.LOGGER.debug("Mossad agents ({}) spawned near {} at {}", n, player.getName().getString(), at);
            }
        }
    }
}
