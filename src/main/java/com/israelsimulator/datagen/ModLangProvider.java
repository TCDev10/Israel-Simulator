package com.israelsimulator.datagen;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Additive lang provider for food/effect content.
 */
public final class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, IsraelSimulator.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("effect.israel_simulator.blessed", "Blessed");
        add("effect.israel_simulator.freedom", "Freedom");
        add("effect.israel_simulator.meat_digestion", "Waiting (Meat)");
        add("effect.israel_simulator.dairy_digestion", "Waiting (Dairy)");
        add("message.israel_simulator.kosher_violation", "You feel unwell from mixing meat and dairy.");

        add(ModItems.FALAFEL.get(), "Falafel");
        add(ModItems.HUMMUS.get(), "Hummus");
        add(ModItems.SHAKSHUKA.get(), "Shakshuka");
        add(ModItems.SABICH.get(), "Sabich");
        add(ModItems.CHALLAH.get(), "Challah");
        add(ModItems.RUGELACH.get(), "Rugelach");
        add(ModItems.TAHINI.get(), "Tahini");
        add(ModItems.DATES.get(), "Dates");
        add(ModItems.OLIVES.get(), "Olives");
        add(ModItems.CITRUS.get(), "Citrus");
    }
}
