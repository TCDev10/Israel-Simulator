package com.israelsimulator.world.structure;

import com.israelsimulator.IsraelSimulator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.BambooFeature;
import net.minecraft.world.level.levelgen.feature.BlockColumnFeature;
import net.minecraft.world.level.levelgen.feature.FallenTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.VinesFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Keeps world-generated vegetation from colliding with Israel-Simulator structures.
 *
 * <p>Root cause of the floating / clipping plants: biome decoration runs per chunk, and a tree or
 * grass patch decorated in one chunk can reach into a neighbouring chunk whose structure pieces are
 * placed later (their template air then cuts the trunk and leaves a floating canopy), or it can be
 * placed after the structure and grow its canopy through windows, roofs and streets. Template plants
 * can also lose their soil when a piece is laid over a different block.
 *
 * <p>While a chunk is decorated ({@code ChunkGenerator#applyBiomeDecoration}) this guard:
 * <ol>
 *   <li>collects the bounding boxes of every mod structure piece near the chunk (structure starts are
 *       known long before any piece is placed, so it works for pieces in not-yet-decorated chunks);</li>
 *   <li>refuses every tree-like VEGETAL_DECORATION feature (trees, fallen logs, huge mushrooms,
 *       bamboo, cactus / sugar cane columns, vines) whose origin is inside a piece box grown by
 *       {@link #MARGIN} blocks; grass and flowers still generate;</li>
 *   <li>at the end removes, inside those boxes, every tree part (leaves and the logs joined to them) that
 *       touches nothing solid any more, typically a palm of one structure whose trunk was cut away by the
 *       template air of another structure laid over it;</li>
 *   <li>then removes, inside those boxes, any plant or vine of this chunk that cannot survive
 *       where it ended up (no soil below, no wall to cling to, a broken double plant).</li>
 * </ol>
 * Outside the boxes and outside world generation (saplings, bone meal) nothing changes.
 */
public final class StructureVegetationGuard {
    /** Horizontal clearance around each piece: larger than a tree canopy radius. */
    public static final int MARGIN = 6;

    private static final ThreadLocal<State> STATE = new ThreadLocal<>();

    private static final class State {
        final List<BoundingBox> boxes;
        boolean vegetationStep;

        State(List<BoundingBox> boxes) {
            this.boxes = boxes;
        }
    }

    private StructureVegetationGuard() {}

    public static void begin(WorldGenLevel level, ChunkAccess chunk) {
        List<BoundingBox> boxes = collectBoxes(level, chunk.getPos());
        STATE.set(boxes.isEmpty() ? null : new State(boxes));
    }

    public static void end(WorldGenLevel level, ChunkAccess chunk) {
        State state = STATE.get();
        STATE.remove();
        if (state != null) {
            Area area = new Area(level, chunk.getPos(), state.boxes);
            int removed = removeFloatingTrees(level, chunk, area) + cleanUp(level, area);
            if (removed > 0) {
                IsraelSimulator.LOGGER.debug("Removed {} unsupported plants near structures in chunk {}",
                        removed, chunk.getPos());
            }
        }
    }

    public static void setVegetationStep(boolean active) {
        State state = STATE.get();
        if (state != null) {
            state.vegetationStep = active;
        }
    }

    /**
     * True when a tree-like vegetation feature at {@code origin} would touch a mod structure. Grass,
     * flower and bush patches still generate: they only land on soil, and whatever ends up without
     * support is removed by the clean-up at the end of the chunk.
     */
    public static boolean blocks(BlockPos origin, Feature<?> feature) {
        State state = STATE.get();
        return state != null && state.vegetationStep && isBulky(feature) && nearAny(state.boxes, origin);
    }

    /** Features that grow trunks, canopies, columns or hanging vines through whatever is around them. */
    static boolean isBulky(Feature<?> feature) {
        return feature instanceof TreeFeature || feature instanceof FallenTreeFeature
                || feature instanceof AbstractHugeMushroomFeature || feature instanceof HugeFungusFeature
                || feature instanceof BambooFeature || feature instanceof BlockColumnFeature
                || feature instanceof VinesFeature;
    }

    static boolean nearAny(List<BoundingBox> boxes, BlockPos p) {
        for (BoundingBox b : boxes) {
            if (near(b, p.getX(), p.getY(), p.getZ())) {
                return true;
            }
        }
        return false;
    }

    /** Inside the box grown by MARGIN horizontally, from well below it up to just above its top. */
    public static boolean near(BoundingBox b, int x, int y, int z) {
        return x >= b.minX() - MARGIN && x <= b.maxX() + MARGIN
                && z >= b.minZ() - MARGIN && z <= b.maxZ() + MARGIN
                && y >= b.minY() - 12 && y <= b.maxY() + 2;
    }

    private static List<BoundingBox> collectBoxes(WorldGenLevel level, ChunkPos center) {
        List<BoundingBox> boxes = new ArrayList<>();
        Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        List<StructureStart> seen = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = center.x() + dx, cz = center.z() + dz;
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                ChunkAccess near = level.getChunk(cx, cz, ChunkStatus.STRUCTURE_REFERENCES, true);
                if (near == null) {
                    continue;
                }
                for (Map.Entry<Structure, LongSet> e : near.getAllReferences().entrySet()) {
                    Identifier id = registry.getKey(e.getKey());
                    if (id == null || !IsraelSimulator.MOD_ID.equals(id.getNamespace())) {
                        continue;
                    }
                    for (long ref : e.getValue()) {
                        ChunkPos startPos = ChunkPos.unpack(ref);
                        // FEATURES may read structure starts up to 8 chunks away; never ask further
                        if (center.getChessboardDistance(startPos) > 8 || !level.hasChunk(startPos.x(), startPos.z())) {
                            continue;
                        }
                        ChunkAccess startChunk = level.getChunk(startPos.x(), startPos.z(), ChunkStatus.STRUCTURE_STARTS, true);
                        StructureStart start = startChunk == null ? null : startChunk.getStartForStructure(e.getKey());
                        if (start == null || !start.isValid() || seen.contains(start)) {
                            continue;
                        }
                        seen.add(start);
                        for (StructurePiece piece : start.getPieces()) {
                            boxes.add(piece.getBoundingBox());
                        }
                    }
                }
            }
        }
        return boxes;
    }

    /**
     * Removes plants and vines that cannot survive, inside the piece boxes, in this chunk and in the
     * already decorated chunks around it: decoration of a later neighbour (a rock mound, a patch) can
     * still change the soil or the wall a plant of this chunk relies on, and the other way round.
     */
    private static int cleanUp(WorldGenLevel level, Area area) {
        int removed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = area.x0; x < area.x0 + 48; x++) {
            for (int z = area.z0; z < area.z0 + 48; z++) {
                int i = area.index(x, z);
                if (area.lo[i] > area.hi[i] || !area.decided(x, z)) {
                    continue;
                }
                ChunkAccess column = level.getChunk(x >> 4, z >> 4);
                // top-down, so a vine left hanging by the removal of the one above goes too
                for (int y = area.hi[i]; y >= area.lo[i]; y--) {
                    pos.set(x, y, z);
                    BlockState state = column.getBlockState(pos);
                    if (shouldRemove(level, pos, state)) {
                        removePlant(level, pos.immutable(), state);
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    /**
     * The 3x3 chunks around the decorated one: for every column the height range covered by a piece
     * box (grown by MARGIN), and which of the chunks are already decorated, so final.
     */
    static final class Area {
        final int x0, z0;
        final int[] lo = new int[48 * 48], hi = new int[48 * 48];
        final boolean[] done = new boolean[9];

        Area(WorldGenLevel level, ChunkPos center, List<BoundingBox> boxes) {
            x0 = center.getMinBlockX() - 16;
            z0 = center.getMinBlockZ() - 16;
            Arrays.fill(lo, Integer.MAX_VALUE);
            Arrays.fill(hi, Integer.MIN_VALUE);
            for (BoundingBox b : boxes) {
                int minX = Math.max(x0, b.minX() - MARGIN), maxX = Math.min(x0 + 47, b.maxX() + MARGIN);
                int minZ = Math.max(z0, b.minZ() - MARGIN), maxZ = Math.min(z0 + 47, b.maxZ() + MARGIN);
                int minY = Math.max(level.getMinY() + 1, b.minY());
                int maxY = Math.min(level.getMaxY() - 1, b.maxY() + 8);
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        int i = index(x, z);
                        lo[i] = Math.min(lo[i], minY);
                        hi[i] = Math.max(hi[i], maxY);
                    }
                }
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    done[(dx + 1) * 3 + dz + 1] = (dx == 0 && dz == 0) || level.getChunk(center.x() + dx, center.z() + dz)
                            .getPersistedStatus().isOrAfter(ChunkStatus.FEATURES);
                }
            }
        }

        int index(int x, int z) {
            return (x - x0) * 48 + (z - z0);
        }

        boolean inside(int x, int z) {
            return x >= x0 && x < x0 + 48 && z >= z0 && z < z0 + 48;
        }

        /** The chunk holding this column is inside the area and decorated. */
        boolean decorated(int x, int z) {
            return inside(x, z) && done[((x - x0) >> 4) * 3 + ((z - z0) >> 4)];
        }

        /** This column and the four next to it are final. */
        boolean decided(int x, int z) {
            return decorated(x, z) && decorated(x + 1, z) && decorated(x - 1, z) && decorated(x, z + 1) && decorated(x, z - 1);
        }
    }

    /** Largest tree part examined; anything bigger is a building, not a tree, and is left alone. */
    static final int MAX_TREE = 2048;

    /**
     * Structure pieces are clipped to the chunk being decorated, so once a chunk is decorated its
     * structure blocks are final. A tree part is judged only when every chunk it touches is decorated;
     * until then the last of those chunks to be decorated judges it.
     */
    private static int removeFloatingTrees(WorldGenLevel level, ChunkAccess chunk, Area area) {
        ChunkPos cp = chunk.getPos();
        Set<BlockPos> visited = new HashSet<>();
        int removed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                int i = area.index(x, z);
                for (int y = area.lo[i]; y <= area.hi[i]; y++) {
                    pos.set(x, y, z);
                    if (!visited.contains(pos) && isTree(chunk.getBlockState(pos))) {
                        List<BlockPos> part = floatingTreePart(level, area, pos.immutable(), visited);
                        if (part != null) {
                            for (BlockPos q : part) {
                                level.setBlock(q, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                            }
                            removed += part.size();
                        }
                    }
                }
            }
        }
        return removed;
    }

    static boolean isTree(BlockState state) {
        if (state.is(BlockTags.LEAVES)) {
            return true;
        }
        if (!state.is(BlockTags.LOGS)) {
            return false;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return !id.getPath().startsWith("stripped_");
    }

    /** Vegetation and air: what a tree part can rest on without being held up. */
    private static boolean holdsNothing(BlockState state) {
        return state.isAir() || state.getBlock() instanceof VegetationBlock || state.getBlock() instanceof VineBlock
                || state.is(Blocks.WATER);
    }

    /** The 26-connected tree part through {@code start}, if it contains leaves and touches nothing solid. */
    private static List<BlockPos> floatingTreePart(WorldGenLevel level, Area area, BlockPos start, Set<BlockPos> visited) {
        List<BlockPos> part = new ArrayList<>();
        ArrayDeque<BlockPos> todo = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        todo.add(start);
        seen.add(start);
        boolean anchored = false, leaves = false, undecided = false;
        while (!todo.isEmpty()) {
            BlockPos p = todo.poll();
            part.add(p);
            visited.add(p);
            if (part.size() > MAX_TREE) {
                anchored = true;
            }
            if (!area.inside(p.getX(), p.getZ())) {
                undecided = true;         // reaches outside the area this chunk may look at
                continue;
            }
            if (!area.decorated(p.getX(), p.getZ())) {
                undecided = true;         // that chunk has not placed its structure pieces yet
            }
            BlockState state = level.getBlockState(p);
            leaves |= state.is(BlockTags.LEAVES);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        BlockPos q = p.offset(dx, dy, dz);
                        if (q.getY() < level.getMinY() || q.getY() > level.getMaxY()) {
                            continue;
                        }
                        boolean face = Math.abs(dx) + Math.abs(dy) + Math.abs(dz) == 1;
                        if (!area.inside(q.getX(), q.getZ())) {
                            if (face) {
                                undecided = true;
                            }
                            continue;
                        }
                        BlockState n = level.getBlockState(q);
                        if (isTree(n)) {
                            if (part.size() + todo.size() <= MAX_TREE && seen.add(q)) {
                                todo.add(q);
                            }
                        } else if (face && !holdsNothing(n)) {
                            anchored = true;
                        }
                    }
                }
            }
        }
        return anchored || undecided || !leaves ? null : part;
    }

    /** A plant or vine that the game itself would pop off at the first block update. */
    static boolean shouldRemove(WorldGenLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (block instanceof VineBlock) {
            return !state.canSurvive(level, pos);
        }
        if (block instanceof CropBlock) {
            // crops also want light, which is not computed yet during world generation: soil only
            return !(level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock);
        }
        return block instanceof VegetationBlock && !state.canSurvive(level, pos);
    }

    private static void removePlant(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        if (state.getBlock() instanceof DoublePlantBlock && state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            BlockPos other = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                    ? pos.above() : pos.below();
            if (level.getBlockState(other).is(state.getBlock())) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
