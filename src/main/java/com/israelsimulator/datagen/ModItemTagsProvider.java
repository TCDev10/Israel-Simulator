package com.israelsimulator.datagen;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.item.ModItemTags;
import com.israelsimulator.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

/**
 * Item tags: kosher categories (meat/dairy/pareve) for mod foods.
 */
public final class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, IsraelSimulator.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModItemTags.PAREVE)
                .add(ModItems.FALAFEL.getKey())
                .add(ModItems.HUMMUS.getKey())
                .add(ModItems.SHAKSHUKA.getKey())
                .add(ModItems.SABICH.getKey())
                .add(ModItems.CHALLAH.getKey())
                .add(ModItems.RUGELACH.getKey())
                .add(ModItems.TAHINI.getKey())
                .add(ModItems.DATES.getKey())
                .add(ModItems.OLIVES.getKey())
                .add(ModItems.CITRUS.getKey());
        tag(ModItemTags.MEAT);
        tag(ModItemTags.DAIRY);
    }
}
