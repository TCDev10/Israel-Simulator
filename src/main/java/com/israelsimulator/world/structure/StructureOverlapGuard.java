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
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/**
 * Strict no-overlap between all Israel-Simulator structures, without biome-blind exclusion zones.
 *
 * <p>Every mod structure has a fixed priority ({@link #PRIORITY}, first = highest). When a start is
 * about to be created (real generation and {@code /locate} both go through
 * {@code Structure.findValidGenerationPoint}), the guard rebuilds this start's pieces on a fresh,
 * identical generation context and looks at every candidate start of every higher-priority mod
 * structure (and of the same structure in an earlier chunk) whose {@link #REACH} can hit this one. A candidate
 * counts if it passes its own vanilla checks (right biome); whether it is in turn cancelled by an even
 * higher one is deliberately not asked, so there is no recursion and no chain of dependencies across
 * the world (that made {@code /locate} hang). The price: rarely, both of two overlapping starts lose.
 * If any of its piece boxes touches one of ours in X/Z, after inflating both by their terrain
 * adaptation margin (beard/bury/encapsulate reshape 12 blocks around a piece), this start is cancelled.
 *
 * <p>Everything is a pure function of seed + chunk, so the result is the same in every
 * chunk-generation order and for {@code /locate}. Raw piece boxes and decisions are cached per (structure, chunk, seed).
 */
public final class StructureOverlapGuard {

    /** Highest priority first: landmarks and big cities keep their spot, small fillers yield. */
    public static final List<String> PRIORITY = List.of(
            "tropical_island", "twin_towers", "jerusalem_city", "tel_aviv_city", "jaffa_port", "gaza_strip",
            "western_wall", "dead_sea_resort", "agricultural_farm", "grand_market", "mediterranean_village",
            "desert_ruins", "ancient_sanctuary", "government_building", "great_synagogue", "startup_office",
            "ein_gedi_oasis", "synagogue", "historical_house");

    /**
     * Upper bound, in blocks, of how far each structure's pieces can reach from its start chunk's
     * min corner in X/Z (jigsaw: max_distance_from_center + half the start template; single
     * template: longest side - 1). StructureNoOverlapTest checks these against the data. Used as a
     * cheap precheck so far-away candidates are never rebuilt.
     */
    public static final Map<String, Integer> REACH = Map.ofEntries(
            Map.entry("tropical_island", 248),
            Map.entry("twin_towers", 88),
            Map.entry("jerusalem_city", 96),
            Map.entry("tel_aviv_city", 96),
            Map.entry("jaffa_port", 96),
            Map.entry("gaza_strip", 104),
            Map.entry("western_wall", 72),
            Map.entry("dead_sea_resort", 80),
            Map.entry("agricultural_farm", 96),
            Map.entry("grand_market", 80),
            Map.entry("mediterranean_village", 96),
            Map.entry("desert_ruins", 80),
            Map.entry("ancient_sanctuary", 56),
            Map.entry("government_building", 56),
            Map.entry("great_synagogue", 48),
            Map.entry("startup_office", 40),
            Map.entry("ein_gedi_oasis", 88),
            Map.entry("synagogue", 40),
            Map.entry("historical_house", 32));

    /** Vanilla beard/bury/encapsulate reshapes terrain up to 12 blocks around every piece (Beardifier kernel). */
    public static final int TERRAIN_MARGIN = 12;

    private static final ThreadLocal<Integer> BYPASS = ThreadLocal.withInitial(() -> 0);
    /** Raw piece boxes per (structure, chunk, seed): what the start would build without the guard. */
    private static final Map<Key, List<BoundingBox>> RAW = new ConcurrentHashMap<>();
    /** Final decision per (structure, chunk, seed). */
    private static final Map<Key, Boolean> DECISION = new ConcurrentHashMap<>();
    private static final List<BoundingBox> NONE = List.of();
    private static volatile Index index;

    private record Key(int structure, long chunk, long seed) {}

    /** reach = REACH + terrain margin: the farthest X/Z block this structure can touch or reshape. */
    private record Entry(Holder<Structure> structure, RandomSpreadStructurePlacement placement, int margin, int reach) {}

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
        return decide(idx, rank, ctx.chunkPos(), ctx);
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
                            int margin = margin(e.structure().value());
                            found = new Entry(e.structure(), rs, margin, REACH.getOrDefault(id, 512) + margin);
                        }
                    }
                }
                rank.put(found == null ? null : found.structure.value(), entries.size());
                entries.add(found);
            }
            rank.remove(null);
            RAW.clear();
            DECISION.clear();
            index = new Index(access, rank, entries);
            return index;
        }
    }

    private static Structure.GenerationContext context(Structure.GenerationContext base, Structure s, ChunkPos chunk) {
        return new Structure.GenerationContext(base.registryAccess(), base.chunkGenerator(), base.biomeSource(),
                base.randomState(), base.structureTemplateManager(), base.seed(), chunk, base.heightAccessor(),
                s.biomes()::contains);
    }

    private static boolean decide(Index idx, int rank, ChunkPos chunk, Structure.GenerationContext base) {
        Key key = new Key(rank, chunk.pack(), base.seed());
        Boolean cached = DECISION.get(key);
        if (cached != null) {
            return cached;
        }
        Structure self = idx.entries.get(rank).structure.value();
        List<BoundingBox> boxes = raw(idx, rank, chunk, base);
        boolean ok = !boxes.isEmpty();
        if (ok) {
            // our footprint incl. terrain reshaping
            int m = margin(self);
            int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (BoundingBox b : boxes) {
                minX = Math.min(minX, b.minX() - m); maxX = Math.max(maxX, b.maxX() + m);
                minZ = Math.min(minZ, b.minZ() - m); maxZ = Math.max(maxZ, b.maxZ() + m);
            }
            search:
            for (int h = 0; h <= rank; h++) {
                Entry other = idx.entries.get(h);
                if (other == null) continue;
                RandomSpreadStructurePlacement p = other.placement;
                int sp = p.spacing();
                int r = other.reach;
                // candidate start corners whose reach box can hit our footprint
                int cx0 = Math.floorDiv(minX - r, 16), cx1 = Math.floorDiv(maxX + r, 16);
                int cz0 = Math.floorDiv(minZ - r, 16), cz1 = Math.floorDiv(maxZ + r, 16);
                for (int gx = Math.floorDiv(cx0, sp); gx <= Math.floorDiv(cx1, sp); gx++) {
                    for (int gz = Math.floorDiv(cz0, sp); gz <= Math.floorDiv(cz1, sp); gz++) {
                        ChunkPos cand = p.getPotentialStructureChunk(base.seed(), gx * sp, gz * sp);
                        int bx = cand.getMinBlockX(), bz = cand.getMinBlockZ();
                        if (bx + r < minX || bx - r > maxX || bz + r < minZ || bz - r > maxZ) continue;  // cheap precheck
                        if (h == rank && !before(cand, chunk)) continue;   // same structure: earlier chunk wins
                        List<BoundingBox> theirs = raw(idx, h, cand, base);
                        if (touches(boxes, m, theirs, other.margin)) {
                            ok = false;
                            break search;
                        }
                    }
                }
            }
        }
        if (DECISION.size() > 200_000) {
            DECISION.clear();
        }
        DECISION.put(key, ok);
        return ok;
    }

    private static List<BoundingBox> raw(Index idx, int rank, ChunkPos chunk, Structure.GenerationContext base) {
        Key key = new Key(rank, chunk.pack(), base.seed());
        List<BoundingBox> cached = RAW.get(key);
        if (cached != null) {
            return cached;
        }
        List<BoundingBox> boxes = build(idx.entries.get(rank).structure.value(), chunk, base);
        if (RAW.size() > 200_000) {
            RAW.clear();
        }
        RAW.put(key, boxes);
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

    static int margin(Structure s) {
        return s.terrainAdaptation() != TerrainAdjustment.NONE ? TERRAIN_MARGIN : 0;
    }

    /** X/Z touch test after inflating every piece of a by ma and of b by mb (terrain-adaptation margins). */
    static boolean touches(List<BoundingBox> a, int ma, List<BoundingBox> b, int mb) {
        int g = ma + mb;
        for (BoundingBox x : a) {
            for (BoundingBox y : b) {
                if (x.minX() - g <= y.maxX() && y.minX() <= x.maxX() + g && x.minZ() - g <= y.maxZ() && y.minZ() <= x.maxZ() + g) {
                    return true;
                }
            }
        }
        return false;
    }
}
