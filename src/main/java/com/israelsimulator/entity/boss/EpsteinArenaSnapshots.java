package com.israelsimulator.entity.boss;

import com.israelsimulator.IsraelSimulator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;

/**
 * Per-dimension snapshot of the blocks the Epstein arena (glass dome + pavilion) replaced,
 * keyed by the boss UUID, so the world can be put back exactly when the fight ends, even
 * after a save/reload.
 */
public final class EpsteinArenaSnapshots extends SavedData {
    /** Restore flags: send to clients, no neighbour shape updates, no drops, no container spill. */
    static final int RESTORE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE
            | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS;

    public static final Codec<EpsteinArenaSnapshots> CODEC = RecordCodecBuilder.create(i -> i.group(
            Arena.CODEC.listOf().fieldOf("arenas").forGetter(s -> new ArrayList<>(s.arenas.values()))
    ).apply(i, EpsteinArenaSnapshots::new));

    public static final SavedDataType<EpsteinArenaSnapshots> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "epstein_arena_snapshots"),
            EpsteinArenaSnapshots::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Arena> arenas = new LinkedHashMap<>();
    /** Transient orphan-check counters (reset on reload, which only delays a restore). */
    private final Map<UUID, Integer> missingChecks = new HashMap<>();

    public EpsteinArenaSnapshots() {}

    private EpsteinArenaSnapshots(List<Arena> list) {
        for (Arena a : list) {
            arenas.put(a.boss(), a);
        }
    }

    public static EpsteinArenaSnapshots get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean has(UUID boss) {
        return arenas.containsKey(boss);
    }

    public void put(Arena arena) {
        arenas.put(arena.boss(), arena);
        setDirty();
    }

    /** Snapshot of one position before the arena is placed. */
    public record Before(BlockPos pos, BlockState state, CompoundTag blockEntity) {}

    /** Captures the state (and block-entity NBT) of every position before placement. */
    public static List<Before> capture(ServerLevel level, Iterable<BlockPos> positions) {
        List<Before> out = new ArrayList<>();
        for (BlockPos p : positions) {
            BlockPos pos = p.immutable();
            BlockEntity be = level.getBlockEntity(pos);
            CompoundTag tag = be != null ? be.saveWithFullMetadata(level.registryAccess()) : null;
            out.add(new Before(pos, level.getBlockState(pos), tag));
        }
        return out;
    }

    /** After placement: keeps only the positions the arena changed and stores them for this boss. */
    public static void record(ServerLevel level, UUID boss, List<Before> before, BlockPos min, BlockPos max) {
        List<Long> positions = new ArrayList<>();
        List<BlockState> originals = new ArrayList<>();
        List<BlockState> placed = new ArrayList<>();
        List<BlockEntityTag> tags = new ArrayList<>();
        for (Before b : before) {
            BlockState after = level.getBlockState(b.pos());
            if (!ArenaRestoreRules.shouldRecord(b.state(), after, b.blockEntity() != null)) continue;
            positions.add(b.pos().asLong());
            originals.add(b.state());
            placed.add(after);
            if (b.blockEntity() != null) tags.add(new BlockEntityTag(b.pos().asLong(), b.blockEntity()));
        }
        List<BlockState> all = new ArrayList<>(originals);
        all.addAll(placed);
        ArenaRestoreRules.Palette<BlockState> pal = ArenaRestoreRules.encode(all);
        List<Integer> idx = new ArrayList<>(pal.indices().length);
        for (int v : pal.indices()) idx.add(v);
        get(level).put(new Arena(boss, min.asLong(), max.asLong(), pal.states(), positions,
                idx.subList(0, originals.size()), idx.subList(originals.size(), idx.size()), tags));
    }

    /**
     * Puts the world back for this boss and forgets the snapshot. Positions a player changed after
     * placement are kept. Does nothing (keeps the snapshot) if the area isn't loaded.
     *
     * @return true when the arena was restored
     */
    public static boolean restore(ServerLevel level, UUID boss) {
        EpsteinArenaSnapshots data = get(level);
        Arena arena = data.arenas.get(boss);
        if (arena == null) return false;
        BlockPos min = BlockPos.of(arena.min());
        BlockPos max = BlockPos.of(arena.max());
        if (!level.hasChunksAt(min, max)) return false;

        List<ArenaRestoreRules.Entry<BlockState>> entries = arena.entries();
        ArenaRestoreRules.Plan<BlockState> plan = ArenaRestoreRules.plan(entries, p -> level.getBlockState(BlockPos.of(p)));

        // 1) empty pavilion containers so nothing spills into the world
        for (ArenaRestoreRules.Entry<BlockState> e : plan.restore()) {
            BlockEntity be = level.getBlockEntity(BlockPos.of(e.pos()));
            if (be instanceof Clearable clearable) clearable.clearContent();
        }
        // 2) original blocks, top-down so nothing above is left unsupported mid-pass
        List<ArenaRestoreRules.Entry<BlockState>> ordered = new ArrayList<>(plan.restore());
        ordered.sort((a, b) -> Integer.compare(BlockPos.getY(b.pos()), BlockPos.getY(a.pos())));
        for (ArenaRestoreRules.Entry<BlockState> e : ordered) {
            level.setBlock(BlockPos.of(e.pos()), e.original(), RESTORE_FLAGS);
        }
        // 3) original block-entity data (chests, signs, ...)
        Map<Long, CompoundTag> tags = new HashMap<>();
        for (BlockEntityTag t : arena.blockEntities()) tags.put(t.pos(), t.tag());
        for (ArenaRestoreRules.Entry<BlockState> e : plan.restore()) {
            CompoundTag tag = tags.get(e.pos());
            if (tag == null) continue;
            BlockEntity be = level.getBlockEntity(BlockPos.of(e.pos()));
            if (be != null) {
                be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
                be.setChanged();
            }
        }
        // 4) nobody left stuck inside restored terrain
        AABB box = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box)) {
            if (!level.noCollision(player)) {
                BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
                player.teleportTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5);
            }
        }

        data.arenas.remove(boss);
        data.missingChecks.remove(boss);
        data.setDirty();
        IsraelSimulator.LOGGER.info("Restored Epstein arena for {}: {} blocks restored, {} left as changed by players",
                boss, plan.restore().size(), plan.skipped().size());
        return true;
    }

    /** Periodic orphan check: restores arenas whose boss is gone while the area is loaded. */
    public static void tickOrphans(ServerLevel level) {
        EpsteinArenaSnapshots data = get(level);
        if (data.arenas.isEmpty()) return;
        for (Arena arena : new ArrayList<>(data.arenas.values())) {
            BlockPos min = BlockPos.of(arena.min());
            BlockPos max = BlockPos.of(arena.max());
            BlockPos center = new BlockPos((min.getX() + max.getX()) / 2, min.getY(), (min.getZ() + max.getZ()) / 2);
            boolean loaded = level.hasChunksAt(min, max) && level.areEntitiesLoaded(ChunkPos.pack(center));
            boolean present = level.getEntity(arena.boss()) instanceof JeffreyEpsteinEntity e && e.isAlive();
            int missing = ArenaRestoreRules.nextMissingCount(data.missingChecks.getOrDefault(arena.boss(), 0), present, loaded);
            data.missingChecks.put(arena.boss(), missing);
            if (ArenaRestoreRules.shouldRestoreOrphan(missing)) {
                restore(level, arena.boss());
            }
        }
    }

    public record BlockEntityTag(long pos, CompoundTag tag) {
        static final Codec<BlockEntityTag> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("pos").forGetter(BlockEntityTag::pos),
                CompoundTag.CODEC.fieldOf("nbt").forGetter(BlockEntityTag::tag)
        ).apply(i, BlockEntityTag::new));
    }

    /** Stored arena: palette-encoded original/placed states per changed position. */
    public record Arena(UUID boss, long min, long max, List<BlockState> palette, List<Long> positions,
                        List<Integer> original, List<Integer> placed, List<BlockEntityTag> blockEntities) {
        static final Codec<Arena> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.STRING_CODEC.fieldOf("boss").forGetter(Arena::boss),
                Codec.LONG.fieldOf("min").forGetter(Arena::min),
                Codec.LONG.fieldOf("max").forGetter(Arena::max),
                BlockState.CODEC.listOf().fieldOf("palette").forGetter(Arena::palette),
                Codec.LONG.listOf().fieldOf("positions").forGetter(Arena::positions),
                Codec.INT.listOf().fieldOf("original").forGetter(Arena::original),
                Codec.INT.listOf().fieldOf("placed").forGetter(Arena::placed),
                BlockEntityTag.CODEC.listOf().fieldOf("block_entities").forGetter(Arena::blockEntities)
        ).apply(i, Arena::new));

        public Arena {
            palette = List.copyOf(palette);
            positions = List.copyOf(positions);
            original = List.copyOf(original);
            placed = List.copyOf(placed);
            blockEntities = List.copyOf(blockEntities);
        }

        List<ArenaRestoreRules.Entry<BlockState>> entries() {
            List<ArenaRestoreRules.Entry<BlockState>> out = new ArrayList<>(positions.size());
            int n = Math.min(positions.size(), Math.min(original.size(), placed.size()));
            for (int k = 0; k < n; k++) {
                out.add(new ArenaRestoreRules.Entry<>(positions.get(k), palette.get(original.get(k)), palette.get(placed.get(k))));
            }
            return out;
        }
    }
}
