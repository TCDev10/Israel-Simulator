package com.israelsimulator.entity.boss;

import com.israelsimulator.registry.ModEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;

/**
 * Spawner and encounter manager for the Bibi Boss (GAME_DESIGN.md §42, TODO §43, §44).
 * Handles structured encounters in government buildings and rare structures while
 * strictly preventing duplicate boss instances.
 */
public final class BibiBossSpawner {

    public static final double BOSS_DUPLICATION_CHECK_RADIUS = 96.0;

    private BibiBossSpawner() {}

    /**
     * Checks if a Bibi Boss is already active within the designated radius to prevent duplicate boss fights.
     */
    public static boolean isBossAlreadyActive(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        AABB aabb = new AABB(pos).inflate(BOSS_DUPLICATION_CHECK_RADIUS);
        List<BibiBossEntity> bosses = level.getEntitiesOfClass(BibiBossEntity.class, aabb, BibiBossEntity::isAlive);
        return !bosses.isEmpty();
    }

    /**
     * Spawns a Bibi Boss with server-authoritative duplication protection.
     */
    public static BibiBossEntity spawnBoss(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null || ModEntities.BIBI_BOSS == null) {
            return null;
        }

        if (isBossAlreadyActive(level, pos)) {
            return null; // Prevent duplicate boss in the same arena
        }

        BibiBossEntity boss = ModEntities.BIBI_BOSS.get().create(level, EntitySpawnReason.TRIGGERED);
        if (boss != null) {
            boss.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            level.addFreshEntity(boss);

            // Grand dramatic spawn FX
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    boss.getX(), boss.getY() + 1.0, boss.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    boss.getX(), boss.getY() + 1.0, boss.getZ(),
                    5, 0.5, 0.5, 0.5, 0.1);
            level.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 1.0F);
        }

        return boss;
    }
}
