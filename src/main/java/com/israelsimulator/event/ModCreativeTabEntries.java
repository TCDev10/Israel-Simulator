package com.israelsimulator.event;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Adds mod food items to the vanilla Food &amp; Drinks creative tab.
 */
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID)
public final class ModCreativeTabEntries {
    private ModCreativeTabEntries() {}

    @SubscribeEvent
    public static void addFoodToVanillaTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.FALAFEL.get());
            event.accept(ModItems.HUMMUS.get());
            event.accept(ModItems.SHAKSHUKA.get());
            event.accept(ModItems.SABICH.get());
            event.accept(ModItems.CHALLAH.get());
            event.accept(ModItems.RUGELACH.get());
            event.accept(ModItems.TAHINI.get());
            event.accept(ModItems.DATES.get());
            event.accept(ModItems.OLIVES.get());
            event.accept(ModItems.CITRUS.get());
        }
    }
}
