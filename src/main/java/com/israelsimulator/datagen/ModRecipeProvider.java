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
        // Tahini: no sesame crop. Wheat seeds stand in; a bowl holds the paste.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.TAHINI.get())
                .requires(Items.WHEAT_SEEDS, 2)
                .requires(Items.BOWL)
                .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                .save(output);

        // Falafel: wheat seeds + wheat + beetroot (chickpea stand-in)
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.FOOD, ModItems.FALAFEL.get(), 3)
                .pattern(" S ")
                .pattern("WBW")
                .pattern(" W ")
                .define('S', Items.WHEAT_SEEDS)
                .define('W', Items.WHEAT)
                .define('B', Items.BEETROOT)
                .unlockedBy("has_beetroot", has(Items.BEETROOT))
                .save(output);

        // Hummus: tahini, beetroot as chickpeas, served in a bowl
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.HUMMUS.get())
                .requires(ModItems.TAHINI.get())
                .requires(Items.BEETROOT)
                .requires(Items.BOWL)
                .unlockedBy("has_tahini", has(ModItems.TAHINI.get()))
                .save(output);

        // Shakshuka: egg + beetroot (tomato stand-in) in a bowl
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.SHAKSHUKA.get())
                .requires(Items.EGG)
                .requires(Items.BEETROOT)
                .requires(Items.BOWL)
                .unlockedBy("has_egg", has(Items.EGG))
                .save(output);

        // Sabich: pita, egg, potato, tahini. No eggplant item; potato is the fried filling.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.SABICH.get())
                .requires(Items.BREAD)
                .requires(Items.EGG)
                .requires(Items.POTATO)
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

        // Dates: no date-craft from the palm fruit itself; berries plus sugar.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.DATES.get())
                .requires(Items.SWEET_BERRIES)
                .requires(Items.SUGAR)
                .unlockedBy("has_sweet_berries", has(Items.SWEET_BERRIES))
                .save(output);

        // Olives: no olive crop item to press. Kelp plus seeds is the existing stand-in.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.OLIVES.get())
                .requires(Items.KELP)
                .requires(Items.WHEAT_SEEDS)
                .unlockedBy("has_kelp", has(Items.KELP))
                .save(output);

        // Citrus: glow berries stand in for the fruit. Sugar keeps this from a one-item recipe.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.CITRUS.get())
                .requires(Items.GLOW_BERRIES)
                .requires(Items.SUGAR)
                .unlockedBy("has_glow_berries", has(Items.GLOW_BERRIES))
                .save(output);

        // Matzo: unleavened wheat and water. The bucket is returned as a crafting remainder.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.MATZO.get())
                .requires(Items.WHEAT, 2)
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_wheat", has(Items.WHEAT))
                .save(output);

        // Sufganiyah: dough, sugar, and berry jam
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.SUFGANIYAH.get())
                .requires(Items.WHEAT)
                .requires(Items.SUGAR)
                .requires(Items.SWEET_BERRIES)
                .unlockedBy("has_sugar", has(Items.SUGAR))
                .save(output);

        // Hamantash: dough, sugar, and date filling
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.HAMANTASH.get())
                .requires(Items.WHEAT)
                .requires(Items.SUGAR)
                .requires(ModItems.DATES.get())
                .unlockedBy("has_dates", has(ModItems.DATES.get()))
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
