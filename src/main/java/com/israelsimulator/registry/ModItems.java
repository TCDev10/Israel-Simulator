package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.item.collectible.CollectibleItems;
import com.israelsimulator.item.cultural.CulturalItems;
import com.israelsimulator.item.cultural.KippahItem;
import com.israelsimulator.item.cultural.TalitItem;
import com.israelsimulator.item.cultural.TefillinItem;
import com.israelsimulator.item.currency.CurrencyItems;
import com.israelsimulator.item.festival.FestivalItems;
import com.israelsimulator.item.food.IsraelFoodProperties;
import com.israelsimulator.item.technology.TechnologyItems;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry for all Israel-Simulator items (GAME_DESIGN.md §7, §17, §47).
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IsraelSimulator.MOD_ID);

    // Cultural items
    public static final DeferredHolder<Item, KippahItem> KIPPAH = ITEMS.registerItem("kippah", p -> new KippahItem(CulturalItems.kippah(p)));
    public static final DeferredHolder<Item, TalitItem> TALIT = ITEMS.registerItem("talit", p -> new TalitItem(CulturalItems.talit(p)));
    public static final DeferredHolder<Item, TefillinItem> TEFILLIN = ITEMS.registerItem("tefillin", p -> new TefillinItem(CulturalItems.tefillin(p)));
    public static final DeferredHolder<Item, Item> RABBIS_CROWN = ITEMS.registerItem("rabbis_crown", p -> new Item(CulturalItems.rabbisCrown(p)));
    public static final DeferredHolder<Item, Item> PRAYER_NOTE = ITEMS.registerItem("prayer_note", p -> new Item(CulturalItems.prayerNote(p)));
    public static final DeferredHolder<Item, Item> FIRST_AMENDMENT = ITEMS.registerItem("first_amendment", p -> new Item(CulturalItems.firstAmendment(p)));
    public static final DeferredHolder<Item, Item> HAVA_NAGILA_DISC = ITEMS.registerItem("hava_nagila_disc", p -> new Item(CulturalItems.havaNagilaDisc(p)));

    // Food items - ingredients
    public static final DeferredHolder<Item, Item> TAHINI = ITEMS.registerSimpleItem("tahini", p -> p.food(IsraelFoodProperties.TAHINI));
    public static final DeferredHolder<Item, Item> DATES = ITEMS.registerSimpleItem("dates", p -> p.food(IsraelFoodProperties.DATES));
    public static final DeferredHolder<Item, Item> OLIVES = ITEMS.registerSimpleItem("olives", p -> p.food(IsraelFoodProperties.OLIVES));
    public static final DeferredHolder<Item, Item> CITRUS = ITEMS.registerSimpleItem("citrus", p -> p.food(IsraelFoodProperties.CITRUS));
    public static final DeferredHolder<Item, Item> CHALLAH = ITEMS.registerSimpleItem("challah", p -> p.food(IsraelFoodProperties.CHALLAH));
    public static final DeferredHolder<Item, Item> RUGELACH = ITEMS.registerSimpleItem("rugelach", p -> p.food(IsraelFoodProperties.RUGELACH, IsraelFoodProperties.RUGELACH_CONSUMABLE));

    // Food items - prepared foods with optional gameplay effects (§17)
    public static final DeferredHolder<Item, Item> FALAFEL = ITEMS.registerSimpleItem("falafel", p -> p.food(IsraelFoodProperties.FALAFEL, IsraelFoodProperties.FALAFEL_CONSUMABLE));
    public static final DeferredHolder<Item, Item> HUMMUS = ITEMS.registerSimpleItem("hummus", p -> p.food(IsraelFoodProperties.HUMMUS, IsraelFoodProperties.HUMMUS_CONSUMABLE));
    public static final DeferredHolder<Item, Item> SHAKSHUKA = ITEMS.registerSimpleItem("shakshuka", p -> p.food(IsraelFoodProperties.SHAKSHUKA, IsraelFoodProperties.SHAKSHUKA_CONSUMABLE));
    public static final DeferredHolder<Item, Item> SABICH = ITEMS.registerSimpleItem("sabich", p -> p.food(IsraelFoodProperties.SABICH, IsraelFoodProperties.SABICH_CONSUMABLE));

    // Collectible items
    public static final DeferredHolder<Item, Item> MEZUZAH = ITEMS.registerItem("mezuzah", p -> new Item(CollectibleItems.mezuzah(p)));
    public static final DeferredHolder<Item, Item> STAR_OF_DAVID = ITEMS.registerItem("star_of_david", p -> new Item(CollectibleItems.starOfDavid(p)));
    public static final DeferredHolder<Item, Item> OLIVE_WOOD_CARVING = ITEMS.registerItem("olive_wood_carving", p -> new Item(CollectibleItems.oliveWoodCarving(p)));
    public static final DeferredHolder<Item, Item> ANCIENT_COIN = ITEMS.registerItem("ancient_coin", p -> new Item(CollectibleItems.ancientCoin(p)));
    public static final DeferredHolder<Item, Item> DEAD_SEA_SCROLL_FRAGMENT = ITEMS.registerItem("dead_sea_scroll_fragment", p -> new Item(CollectibleItems.deadSeaScrollFragment(p)));
    public static final DeferredHolder<Item, Item> DEAD_SEA_MUD = ITEMS.registerItem("dead_sea_mud", p -> new com.israelsimulator.item.deadsea.DeadSeaMudItem(p.stacksTo(16)));

    // Currency items
    public static final DeferredHolder<Item, Item> SHEKEL = ITEMS.registerItem("shekel", p -> new Item(CurrencyItems.shekel(p)));
    public static final DeferredHolder<Item, Item> AGORA = ITEMS.registerItem("agora", p -> new Item(CurrencyItems.agora(p)));

    // Festival items
    public static final DeferredHolder<Item, Item> MATZO = ITEMS.registerItem("matzo", p -> new Item(FestivalItems.matzo(p)));
    public static final DeferredHolder<Item, Item> SUFGANIYAH = ITEMS.registerItem("sufganiyah", p -> new Item(FestivalItems.sufganiyah(p)));
    public static final DeferredHolder<Item, Item> DREIDEL = ITEMS.registerItem("dreidel", p -> new Item(FestivalItems.dreidel(p)));
    public static final DeferredHolder<Item, Item> HAMANTASH = ITEMS.registerItem("hamantash", p -> new Item(FestivalItems.hamantash(p)));
    public static final DeferredHolder<Item, Item> SHOFAR = ITEMS.registerItem("shofar", p -> new Item(FestivalItems.shofar(p)));

    // Technology items
    public static final DeferredHolder<Item, Item> SMARTPHONE = ITEMS.registerItem("smartphone", p -> new Item(TechnologyItems.smartphone(p)));
    public static final DeferredHolder<Item, Item> LAPTOP = ITEMS.registerItem("laptop", p -> new Item(TechnologyItems.laptop(p)));
    public static final DeferredHolder<Item, Item> DRONE_PART = ITEMS.registerItem("drone_part", p -> new Item(TechnologyItems.dronePart(p)));

    private ModItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
