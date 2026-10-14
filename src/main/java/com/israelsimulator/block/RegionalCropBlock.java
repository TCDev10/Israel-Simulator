package com.israelsimulator.block;

import java.util.function.Supplier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Farmland crop whose edible produce can also be replanted as its seed.
 */
public final class RegionalCropBlock extends CropBlock {
    private final Supplier<? extends ItemLike> seed;

    public RegionalCropBlock(BlockBehaviour.Properties properties, Supplier<? extends ItemLike> seed) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }
}
