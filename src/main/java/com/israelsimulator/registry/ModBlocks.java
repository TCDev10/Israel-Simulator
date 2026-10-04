package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.block.FruitingLeavesBlock;
import com.israelsimulator.block.GrapevineBlock;
import com.israelsimulator.block.MediterraneanHerbBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block registry for Israel-Simulator custom blocks.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IsraelSimulator.MOD_ID);

    // Fruit-bearing trees
    public static final DeferredBlock<FruitingLeavesBlock> OLIVE_LEAVES = BLOCKS.registerBlock(
            "olive_leaves",
            p -> new FruitingLeavesBlock(0.05F, ModItems.OLIVES, p),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
    );
    public static final DeferredItem<BlockItem> OLIVE_LEAVES_ITEM = ModItems.ITEMS.registerSimpleBlockItem("olive_leaves", OLIVE_LEAVES);

    public static final DeferredBlock<FruitingLeavesBlock> DATE_PALM_LEAVES = BLOCKS.registerBlock(
            "date_palm_leaves",
            p -> new FruitingLeavesBlock(0.05F, ModItems.DATES, p),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_LEAVES)
    );
    public static final DeferredItem<BlockItem> DATE_PALM_LEAVES_ITEM = ModItems.ITEMS.registerSimpleBlockItem("date_palm_leaves", DATE_PALM_LEAVES);

    public static final DeferredBlock<FruitingLeavesBlock> CITRUS_LEAVES = BLOCKS.registerBlock(
            "citrus_leaves",
            p -> new FruitingLeavesBlock(0.05F, ModItems.CITRUS, p),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AZALEA_LEAVES)
    );
    public static final DeferredItem<BlockItem> CITRUS_LEAVES_ITEM = ModItems.ITEMS.registerSimpleBlockItem("citrus_leaves", CITRUS_LEAVES);

    // Agricultural crops and vegetation
    public static final DeferredBlock<GrapevineBlock> GRAPEVINE = BLOCKS.registerBlock(
            "grapevine",
            GrapevineBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
    );

    public static final DeferredBlock<MediterraneanHerbBlock> MEDITERRANEAN_HERBS = BLOCKS.registerBlock(
            "mediterranean_herbs",
            MediterraneanHerbBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).sound(SoundType.GRASS)
    );
    public static final DeferredItem<BlockItem> MEDITERRANEAN_HERBS_ITEM = ModItems.ITEMS.registerSimpleBlockItem("mediterranean_herbs", MEDITERRANEAN_HERBS);

    private ModBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}

