package com.israelsimulator.world.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/**
 * Strict no-overlap between all Israel-Simulator structures, without biome-blind exclusion zones.
 *
 * <p>Every mod structure has a fixed priority ({@link #PRIORITY}, first = highest). When a start is
 * about to be created (real generation and {@code /locate} both go through
 * {@code Structure.findValidGenerationPoint}), the guard rebuilds this start's pieces on a fresh,
 * identical generation context and looks at every candidate start of every higher-priority mod
 * structure (and of the same structure in an earlier chunk) within {@link #RANGE_CHUNKS}. A candidate
 * counts only if it would really generate: right biome, not itself cancelled by an even higher one.
 * If any of its piece boxes touches one of ours in X/Z, this start is cancelled.
 *
 * <p>So a structure only loses a spawn where it would really have overlapped a structure that
 * really spawns. Everything is a pure function of seed + chunk, so the result is the same in every
 * chunk-generation order and for {@code /locate}. Results are cached per (structure, chunk, seed).
 */
public final class StructureOverlapGuard {

    /** Highest priority first: landmarks and big cities keep their spot, small fillers yield. */
    public static final List<String> PRIORITY = List.of(
            "tropical_island", "torri_gemelle", "jerusalem_city", "tel_aviv_city", "jaffa_port", "gaza_strip",
            "western_wall", "dead_sea_resort", "agricultural_farm", "grand_market", "mediterranean_village",
            "desert_ruins", "ancient_sanctuary", "government_building", "great_synagogue", "startup_office",
            "ein_gedi_oasis", "synagogue", "historical_house");

    /** Candidate search radius; must exceed (reachA + reachB) / 16 for every pair (largest: island + Gaza). */
    public static final int RANGE_CHUNKS = 16;

    private static final ThreadLocal<Integer> BYPASS = ThreadLocal.withInitial(() -> 0);
    private static final Map<Key, List<BoundingBox>> CACHE = new ConcurrentHashMap<>();
    private static final List<BoundingBox> NONE = List.of();
    private static volatile Index index;

    private record Key(int structure, long chunk, long seed) {}

    private record Entry(Holder<Structure> structure, RandomSpreadStructurePlacement placement) {}

    private record Index(RegistryAccess access, Map<Structure, Integer> rank, List<Entry> entries) {}

    private StructureOverlapGuard() {}

    public static boolean bypassed() {
        return BYPASS.get() > 0;
    }

    /** Called from the mixin with the result of the vanilla check: false = cancel this start. */
    public static boolean allows(Structure structure, Structure.GenerationContext ctx) {
        Index idx = index(ctx.registryAccess());
        Integer rank = idx.rank.get(structure);
        if (rank == null) {
            return true;
        }
        return !compute(idx, rank, ctx.chunkPos(), ctx).isEmpty();
    }

    /** Raw check without the guard (for diagnostics/tests). */
    public static boolean rawValid(Structure structure, ChunkPos chunk, Structure.GenerationContext base) {
        BYPASS.set(BYPASS.get() + 1);
        try {
            return structure.findValidGenerationPoint(context(base, structure, chunk)).isPresent();
        } finally {
            BYPASS.set(BYPASS.get() - 1);
        }
    }

    private static Index index(RegistryAccess access) {
        Index idx = index;
        if (idx != null && idx.access == access) {
            return idx;
        }
        synchronized (StructureOverlapGuard.class) {
            if (index != null && index.access == access) {
                return index;
            }
            Registry<Structure> structures = access.lookupOrThrow(Registries.STRUCTURE);
            Registry<StructureSet> sets = access.lookupOrThrow(Registries.STRUCTURE_SET);
            Map<Structure, Integer> rank = new java.util.IdentityHashMap<>();
            List<Entry> entries = new ArrayList<>();
            for (String id : PRIORITY) {
                Entry found = null;
                for (Holder.Reference<StructureSet> set : sets.listElements().toList()) {
                    if (!(set.value().placement() instanceof RandomSpreadStructurePlacement rs)) continue;
                    for (StructureSet.StructureSelectionEntry e : set.value().structures()) {
                        Identifier key = structures.getKey(e.structure().value());
                        if (key != null && key.getNamespace().equals("israel_simulator") && key.getPath().equals(id)) {
                            found = new Entry(e.structure(), rs);
                        }
                    }
                }
                rank.put(found == null ? null : found.structure.value(), entries.size());
                entries.add(found);
            }
            rank.remove(null);
            CACHE.clear();
            index = new Index(access, rank, entries);
            return index;
        }
    }

    private static Structure.GenerationContext context(Structure.GenerationContext base, Structure s, ChunkPos chunk) {
        return new Structure.GenerationContext(base.registryAccess(), base.chunkGenerator(), base.biomeSource(),
                base.randomState(), base.structureTemplateManager(), base.seed(), chunk, base.heightAccessor(),
                s.biomes()::contains);
    }

    private static List<BoundingBox> compute(Index idx, int rank, ChunkPos chunk, Structure.GenerationContext base) {
        Key key = new Key(rank, chunk.pack(), base.seed());
        List<BoundingBox> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        List<BoundingBox> boxes = build(idx.entries.get(rank).structure.value(), chunk, base);
        if (!boxes.isEmpty()) {
            search:
            for (int h = 0; h <= rank; h++) {
                Entry other = idx.entries.get(h);
                if (other == null) continue;
                RandomSpreadStructurePlacement p = other.placement;
                int sp = p.spacing();
                int x0 = Math.floorDiv(chunk.x() - RANGE_CHUNKS, sp), x1 = Math.floorDiv(chunk.x() + RANGE_CHUNKS, sp);
                int z0 = Math.floorDiv(chunk.z() - RANGE_CHUNKS, sp), z1 = Math.floorDiv(chunk.z() + RANGE_CHUNKS, sp);
                for (int cx = x0; cx <= x1; cx++) {
                    for (int cz = z0; cz <= z1; cz++) {
                        ChunkPos cand = p.getPotentialStructureChunk(base.seed(), cx * sp, cz * sp);
                        if (Math.abs(cand.x() - chunk.x()) > RANGE_CHUNKS || Math.abs(cand.z() - chunk.z()) > RANGE_CHUNKS) continue;
                        if (h == rank && !before(cand, chunk)) continue;   // same structure: earlier chunk wins
                        List<BoundingBox> theirs = compute(idx, h, cand, base);
                        if (touches(boxes, theirs)) {
                            boxes = NONE;
                            break search;
                        }
                    }
                }
            }
        }
        if (CACHE.size() > 200_000) {
            CACHE.clear();
        }
        CACHE.put(key, boxes);
        return boxes;
    }

    private static boolean before(ChunkPos a, ChunkPos b) {
        return a.x() < b.x() || (a.x() == b.x() && a.z() < b.z());
    }

    private static List<BoundingBox> build(Structure s, ChunkPos chunk, Structure.GenerationContext base) {
        BYPASS.set(BYPASS.get() + 1);
        try {
            Optional<Structure.GenerationStub> stub = s.findValidGenerationPoint(context(base, s, chunk));
            if (stub.isEmpty()) return NONE;
            List<BoundingBox> out = new ArrayList<>();
            for (StructurePiece piece : stub.get().getPiecesBuilder().build().pieces()) {
                out.add(piece.getBoundingBox());
            }
            return out.isEmpty() ? NONE : List.copyOf(out);
        } finally {
            BYPASS.set(BYPASS.get() - 1);
        }
    }

    static boolean touches(List<BoundingBox> a, List<BoundingBox> b) {
        for (BoundingBox x : a) {
            for (BoundingBox y : b) {
                if (x.minX() <= y.maxX() && y.minX() <= x.maxX() && x.minZ() <= y.maxZ() && y.minZ() <= x.maxZ()) {
                    return true;
                }
            }
        }
        return false;
    }
}
