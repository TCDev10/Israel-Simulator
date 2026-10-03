package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative-mode tab registry. Tabs are added when item/block content exists.
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IsraelSimulator.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ISRAEL_SIMULATOR_TAB =
            CREATIVE_MODE_TABS.register("israel_simulator", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.israel_simulator"))
                    .icon(() -> new ItemStack(ModItems.KIPPAH.get()))
                    .displayItems((params, output) -> {
                        // Cultural
                        output.accept(ModItems.KIPPAH.get());
                        output.accept(ModItems.TALIT.get());
                        output.accept(ModItems.TEFILLIN.get());
                        output.accept(ModItems.PRAYER_NOTE.get());
                        output.accept(ModItems.FIRST_AMENDMENT.get());
                        output.accept(ModItems.RABBIS_CROWN.get());
                        output.accept(ModItems.HAVA_NAGILA_DISC.get());

                        // Food
                        output.accept(ModItems.TAHINI.get());
                        output.accept(ModItems.DATES.get());
                        output.accept(ModItems.OLIVES.get());
                        output.accept(ModItems.CITRUS.get());
                        output.accept(ModItems.CHALLAH.get());
                        output.accept(ModItems.RUGELACH.get());
                        output.accept(ModItems.FALAFEL.get());
                        output.accept(ModItems.HUMMUS.get());
                        output.accept(ModItems.SHAKSHUKA.get());
                        output.accept(ModItems.SABICH.get());

                        // Collectibles
                        output.accept(ModItems.MEZUZAH.get());
                        output.accept(ModItems.STAR_OF_DAVID.get());
                        output.accept(ModItems.OLIVE_WOOD_CARVING.get());
                        output.accept(ModItems.ANCIENT_COIN.get());
                        output.accept(ModItems.DEAD_SEA_SCROLL_FRAGMENT.get());

                        // Currency
                        output.accept(ModItems.SHEKEL.get());
                        output.accept(ModItems.AGORA.get());

                        // Festival
                        output.accept(ModItems.MATZO.get());
                        output.accept(ModItems.SUFGANIYAH.get());
                        output.accept(ModItems.DREIDEL.get());
                        output.accept(ModItems.HAMANTASH.get());
                        output.accept(ModItems.SHOFAR.get());

                        // Technology
                        output.accept(ModItems.SMARTPHONE.get());
                        output.accept(ModItems.LAPTOP.get());
                        output.accept(ModItems.DRONE_PART.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
