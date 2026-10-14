package com.israelsimulator.entity.event;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Loads the 1034 blocks of the plane schematic (plane1-e1160.schem)
 * with their relative offsets (dx, dy, dz) centered around the fuselage.
 */
public final class PlaneModelData {

    public record BlockEntry(int dx, int dy, int dz, BlockState state) {}

    private static List<BlockEntry> BLOCKS;

    private PlaneModelData() {}

    public static synchronized List<BlockEntry> getBlocks() {
        if (BLOCKS == null) {
            BLOCKS = loadBlocks();
        }
        return BLOCKS;
    }

    private static List<BlockEntry> loadBlocks() {
        List<BlockEntry> list = new ArrayList<>();
        try (InputStream in = PlaneModelData.class.getResourceAsStream("/assets/israel_simulator/data/plane_model_blocks.txt")) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || !line.contains("=")) continue;
                        String[] parts = line.split("=");
                        String[] coords = parts[0].split(",");
                        int dx = Integer.parseInt(coords[0].trim());
                        int dy = Integer.parseInt(coords[1].trim());
                        int dz = Integer.parseInt(coords[2].trim());
                        String blockStr = parts[1].trim();
                        BlockState state = parseBlockState(blockStr);
                        list.add(new BlockEntry(dx, dy, dz, state));
                    }
                }
            }
        } catch (Exception e) {
            // Fallback: minimal plane fuselage if file reading fails
            for (int dx = -10; dx <= 10; dx++) {
                list.add(new BlockEntry(dx, 0, 0, Blocks.CONCRETE.white().defaultBlockState()));
            }
        }
        return Collections.unmodifiableList(list);
    }

    private static BlockState parseBlockState(String blockStr) {
        String name = blockStr;
        if (blockStr.contains("[")) {
            name = blockStr.substring(0, blockStr.indexOf('['));
        }
        try {
            Block block = BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(name));
            if (block != null) {
                return block.defaultBlockState();
            }
        } catch (Throwable ignored) {}
        try {
            return Blocks.CONCRETE.white().defaultBlockState();
        } catch (Throwable ignored) {
            return null;
        }
    }
}

