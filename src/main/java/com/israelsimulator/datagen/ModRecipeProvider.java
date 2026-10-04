package com.israelsimulator.datagen;

import com.israelsimulator.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Food recipe generator.
 */
public final class ModRecipeProvider extends RecipeProvider {
    private final HolderGetter<Item> items;

    private ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.items = registries.lookupOrThrow(Registries.ITEM);
    }

    @Override
    protected void buildRecipes() {
        // Tahini: grind wheat seeds
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.TAHINI.get())
                .requires(Items.WHEAT_SEEDS, 3)
                .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                .save(output);

        // Falafel: wheat seeds + wheat + beetroot
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.FOOD, ModItems.FALAFEL.get(), 3)
                .pattern(" S ")
                .pattern("WBW")
                .pattern(" W ")
                .define('S', Items.WHEAT_SEEDS)
                .define('W', Items.WHEAT)
                .define('B', Items.BEETROOT)
                .unlockedBy("has_beetroot", has(Items.BEETROOT))
                .save(output);

        // Hummus
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.HUMMUS.get())
                .requires(ModItems.TAHINI.get())
                .requires(Items.BEETROOT)
                .requires(Items.BOWL)
                .unlockedBy("has_tahini", has(ModItems.TAHINI.get()))
                .save(output);

        // Shakshuka: egg + beetroot in a bowl
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.SHAKSHUKA.get())
                .requires(Items.EGG)
                .requires(Items.BEETROOT)
                .requires(Items.BOWL)
                .unlockedBy("has_egg", has(Items.EGG))
                .save(output);

        // Sabich: bread + egg + carrot + tahini
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.SABICH.get())
                .requires(Items.BREAD)
                .requires(Items.EGG)
                .requires(Items.CARROT)
                .requires(ModItems.TAHINI.get())
                .unlockedBy("has_tahini", has(ModItems.TAHINI.get()))
                .save(output);

        // Challah: enriched braided bread
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.CHALLAH.get())
                .requires(Items.WHEAT, 3)
                .requires(Items.EGG)
                .requires(Items.SUGAR)
                .unlockedBy("has_wheat", has(Items.WHEAT))
                .save(output);

        // Rugelach
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.RUGELACH.get(), 2)
                .requires(Items.WHEAT)
                .requires(Items.SUGAR)
                .requires(Items.COCOA_BEANS)
                .unlockedBy("has_cocoa_beans", has(Items.COCOA_BEANS))
                .save(output);

        // Dates
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.DATES.get())
                .requires(Items.SWEET_BERRIES)
                .requires(Items.SUGAR)
                .unlockedBy("has_sweet_berries", has(Items.SWEET_BERRIES))
                .save(output);

        // Olives
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.OLIVES.get())
                .requires(Items.KELP)
                .requires(Items.WHEAT_SEEDS)
                .unlockedBy("has_kelp", has(Items.KELP))
                .save(output);

        // Citrus
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.CITRUS.get())
                .requires(Items.GLOW_BERRIES)
                .unlockedBy("has_glow_berries", has(Items.GLOW_BERRIES))
                .save(output);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Israel-Simulator recipes";
        }
    }
}
