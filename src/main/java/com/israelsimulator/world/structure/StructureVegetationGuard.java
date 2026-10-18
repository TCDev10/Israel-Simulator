package com.israelsimulator.world.structure;

import com.israelsimulator.IsraelSimulator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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
 *   <li>refuses every VEGETAL_DECORATION feature whose origin is inside a piece box grown by
 *       {@link #MARGIN} blocks (trees, grass/flower patches, bushes, vines...);</li>
 *   <li>at the end removes, inside those boxes, any plant or vine of this chunk that cannot survive
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
            int removed = cleanUp(level, chunk, state.boxes);
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

    /** True when a vegetation feature at {@code origin} would touch a mod structure. */
    public static boolean blocks(BlockPos origin) {
        State state = STATE.get();
        return state != null && state.vegetationStep && nearAny(state.boxes, origin);
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

    private static int cleanUp(WorldGenLevel level, ChunkAccess chunk, List<BoundingBox> boxes) {
        ChunkPos cp = chunk.getPos();
        int x0 = cp.getMinBlockX(), z0 = cp.getMinBlockZ();
        int removed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (BoundingBox b : boxes) {
            int minX = Math.max(x0, b.minX() - MARGIN), maxX = Math.min(x0 + 15, b.maxX() + MARGIN);
            int minZ = Math.max(z0, b.minZ() - MARGIN), maxZ = Math.min(z0 + 15, b.maxZ() + MARGIN);
            if (minX > maxX || minZ > maxZ) {
                continue;
            }
            int minY = Math.max(level.getMinY() + 1, b.minY());
            int maxY = Math.min(level.getMaxY() - 1, b.maxY() + 8);
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean edge = (x & 15) == 0 || (x & 15) == 15 || (z & 15) == 0 || (z & 15) == 15;
                    for (int y = minY; y <= maxY; y++) {
                        pos.set(x, y, z);
                        BlockState state = chunk.getBlockState(pos);
                        if (shouldRemove(level, pos, state, edge)) {
                            removePlant(level, pos.immutable(), state);
                            removed++;
                        }
                    }
                }
            }
        }
        return removed;
    }

    /** A plant or vine that the game itself would pop off at the first block update. */
    static boolean shouldRemove(WorldGenLevel level, BlockPos pos, BlockState state, boolean chunkEdge) {
        Block block = state.getBlock();
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (block instanceof VineBlock) {
            // a vine on the chunk border may cling to a wall the neighbour chunk has not placed yet
            return !chunkEdge && !state.canSurvive(level, pos);
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
