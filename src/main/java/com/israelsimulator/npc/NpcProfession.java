package com.israelsimulator.npc;

import com.israelsimulator.economy.CityRegion;
import com.israelsimulator.economy.ProductCategory;
import java.util.List;

/**
 * The 15 functional professions for Israel-Simulator NPCs (GAME_DESIGN.md §26, TODO §26).
 */
public enum NpcProfession {
    MERCHANT(
            "merchant", "Merchant", ProductCategory.COMMODITIES_MINERALS, CityRegion.JERUSALEM, true,
            List.of("Shalom! Welcome to my stall. Best prices in the market!", "Looking for authentic goods? Come have a look!"),
            "Shabbat Shalom! The market is closed for holy rest.",
            List.of("ancient_coin", "olive_wood_carving", "salt_block")
    ),
    RABBI(
            "rabbi", "Rabbi", ProductCategory.JUDAICA_CULTURAL, CityRegion.JERUSALEM, true,
            List.of("Peace unto you, my child. Have you prayed with intention today?", "Torah study brings light to the soul."),
            "Shabbat Shalom! May your Sabbath be filled with divine light and peace.",
            List.of("kippah", "talit", "tefillin", "prayer_note", "mezuzah")
    ),
    FARMER(
            "farmer", "Farmer", ProductCategory.AGRICULTURE, CityRegion.RURAL_GALILEE, false,
            List.of("Blessed be the harvest from the fertile soil of the valley!", "Fresh dates and olives straight from the grove!"),
            "Shabbat Shalom. The land rests alongside us today.",
            List.of("olives", "dates", "citrus")
    ),
    FISHERMAN(
            "fisherman", "Fisherman", ProductCategory.FOOD, CityRegion.JAFFA, false,
            List.of("Ahoy! Sea was generous this morning out of Jaffa port.", "Fresh catch from the Mediterranean!"),
            "Shabbat Shalom. The boats are docked until Sunday.",
            List.of("hummus", "tahini")
    ),
    CHEF(
            "chef", "Chef", ProductCategory.FOOD, CityRegion.TEL_AVIV, false,
            List.of("Warm pita, fresh falafel, and spicy shakshuka ready for you!", "Taste the finest street food in the Levant!"),
            "Shabbat Shalom! Enjoy warm Challah with your family tonight.",
            List.of("falafel", "hummus", "shakshuka", "sabich", "rugelach", "challah")
    ),
    ARTISAN(
            "artisan", "Artisan", ProductCategory.JUDAICA_CULTURAL, CityRegion.JAFFA, true,
            List.of("Handcrafted olive wood carvings and handcrafted mezuzot.", "Every piece has a story from our ancestors."),
            "Shabbat Shalom. The workshop tools rest until evening.",
            List.of("olive_wood_carving", "mezuzah", "star_of_david")
    ),
    DEVELOPER(
            "developer", "Developer", ProductCategory.TECHNOLOGY, CityRegion.TEL_AVIV, false,
            List.of("Pushing our latest algorithm build! Silicon alley never sleeps.", "Need an updated smartphone firmware or microchip?"),
            "Shabbat Shalom! Even servers could use a scheduled downtime.",
            List.of("smartphone", "laptop")
    ),
    TAXI_DRIVER(
            "taxi_driver", "Taxi Driver", ProductCategory.COMMODITIES_MINERALS, CityRegion.TEL_AVIV, false,
            List.of("Yalla! Where to? Ayalon highway has some traffic today.", "Hop in! Quickest route through the city alleys."),
            "Shabbat Shalom. Streets are quiet and peaceful today.",
            List.of("falafel", "smartphone")
    ),
    TOURIST(
            "tourist", "Tourist", ProductCategory.COMMODITIES_MINERALS, CityRegion.JERUSALEM, false,
            List.of("Incredible architecture! The history here is thousands of years old.", "Can you tell me how to reach the Kotel?"),
            "Shabbat Shalom! What a magical atmosphere in the ancient streets.",
            List.of("camera", "ancient_coin", "kippah")
    ),
    MUSICIAN(
            "musician", "Musician", ProductCategory.JUDAICA_CULTURAL, CityRegion.TEL_AVIV, false,
            List.of("Let music bring joy to our hearts! Playing Hava Nagila and klezmer tunes.", "A song for every celebration and holiday!"),
            "Shabbat Shalom! Songs of praise and peaceful melodies today.",
            List.of("shofar", "dreidel")
    ),
    HISTORIAN(
            "historian", "Historian", ProductCategory.JUDAICA_CULTURAL, CityRegion.JERUSALEM, true,
            List.of("These stones witnessed millennia of kingdoms and prophets.", "Studying ancient scrolls found near the Dead Sea caves."),
            "Shabbat Shalom. Today we contemplate timeless wisdom.",
            List.of("ancient_coin", "prayer_note", "dead_sea_mud")
    ),
    SHOPKEEPER(
            "shopkeeper", "Shopkeeper", ProductCategory.FOOD, CityRegion.JERUSALEM, true,
            List.of("Welcome to our family grocery! Fresh produce and baked delicacies daily.", "Look around, let me know if you need anything."),
            "Shabbat Shalom! Shutter is closed until Havdalah.",
            List.of("challah", "rugelach", "sufganiyah", "tahini")
    ),
    FOUNDER(
            "founder", "Startup Founder", ProductCategory.TECHNOLOGY, CityRegion.TEL_AVIV, false,
            List.of("Disrupting classical paradigms with our new tech startup!", "Looking for early adopters and venture capital partners."),
            "Shabbat Shalom. Disconnecting from Slack for 24 hours.",
            List.of("laptop", "smartphone", "drone_part")
    ),
    INVESTOR(
            "investor", "Investor", ProductCategory.TECHNOLOGY, CityRegion.TEL_AVIV, false,
            List.of("I invest in bold Israeli innovations shaping the future.", "Pitch me your venture if you have solid traction."),
            "Shabbat Shalom. Markets are resting, time for reflection.",
            List.of("laptop", "ancient_coin")
    ),
    ENGINEER(
            "engineer", "Engineer", ProductCategory.TECHNOLOGY, CityRegion.TEL_AVIV, false,
            List.of("Drip irrigation systems and high-precision drone avionics.", "Engineering sustainable tech for arid lands."),
            "Shabbat Shalom. Clean schematics and quiet labs today.",
            List.of("drone_part", "laptop")
    );

    private final String id;
    private final String displayName;
    private final ProductCategory primaryCategory;
    private final CityRegion preferredRegion;
    private final boolean observantShabbat;
    private final List<String> greetings;
    private final String shabbatGreeting;
    private final List<String> tradedItems;

    NpcProfession(String id, String displayName, ProductCategory primaryCategory,
                  CityRegion preferredRegion, boolean observantShabbat,
                  List<String> greetings, String shabbatGreeting, List<String> tradedItems) {
        this.id = id;
        this.displayName = displayName;
        this.primaryCategory = primaryCategory;
        this.preferredRegion = preferredRegion;
        this.observantShabbat = observantShabbat;
        this.greetings = greetings;
        this.shabbatGreeting = shabbatGreeting;
        this.tradedItems = tradedItems;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ProductCategory getPrimaryCategory() {
        return primaryCategory;
    }

    public CityRegion getPreferredRegion() {
        return preferredRegion;
    }

    public boolean isObservantShabbat() {
        return observantShabbat;
    }

    public List<String> getGreetings() {
        return greetings;
    }

    public String getShabbatGreeting() {
        return shabbatGreeting;
    }

    public List<String> getTradedItems() {
        return tradedItems;
    }
}
