package com.israelsimulator.datagen;

import com.israelsimulator.registry.ModBlocks;
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
        // Tahini is prepared from sesame seeds.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.TAHINI.get())
                .requires(ModItems.SESAME_SEEDS.get(), 2)
                .requires(Items.BOWL)
                .unlockedBy("has_sesame_seeds", has(ModItems.SESAME_SEEDS.get()))
                .save(output);

        // Falafel: chickpeas, herbs, and wheat.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.FOOD, ModItems.FALAFEL.get(), 3)
                .pattern(" H ")
                .pattern("WCW")
                .pattern(" W ")
                .define('H', ModBlocks.MEDITERRANEAN_HERBS_ITEM.get())
                .define('W', Items.WHEAT)
                .define('C', ModItems.CHICKPEAS.get())
                .unlockedBy("has_chickpeas", has(ModItems.CHICKPEAS.get()))
                .save(output);

        // Hummus: chickpeas and tahini, served in a bowl.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.HUMMUS.get())
                .requires(ModItems.TAHINI.get())
                .requires(ModItems.CHICKPEAS.get())
                .requires(Items.BOWL)
                .unlockedBy("has_tahini", has(ModItems.TAHINI.get()))
                .save(output);

        // Shakshuka: eggs, tomatoes, and Mediterranean herbs in a bowl.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.SHAKSHUKA.get())
                .requires(Items.EGG)
                .requires(ModItems.TOMATO.get())
                .requires(ModBlocks.MEDITERRANEAN_HERBS_ITEM.get())
                .requires(Items.BOWL)
                .unlockedBy("has_egg", has(Items.EGG))
                .save(output);

        // Sabich: pita, egg, eggplant, and tahini.
        ShapelessRecipeBuilder.shapeless(this.items, RecipeCategory.FOOD, ModItems.SABICH.get())
                .requires(Items.BREAD)
                .requires(Items.EGG)
                .requires(ModItems.EGGPLANT.get())
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
