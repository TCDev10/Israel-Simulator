# Israel-Simulator — Game Design Document

> **Project:** Israel-Simulator
> **Platform:** Minecraft Java Edition
> **Primary Mod Loader:** NeoForge 26.2
> **Compatibility:** Previous NeoForge/Minecraft versions may be supported only when technically feasible and explicitly verified
> **Genre:** Open-world sandbox / exploration / cultural simulation / comedy / adventure
> **Multiplayer:** Supported
> **Design Philosophy:** Exploration, discovery, interaction, collection, emergent events, rare content and chaotic sandbox gameplay

---

# 1. Game Overview

## 1.1 Concept

**Israel-Simulator** is a Minecraft Java Edition total-conversion-style mod that transforms the Minecraft world into a large, explorable sandbox inspired by Israel, its cities, landscapes, food, culture, technology, landmarks, transportation, festivals and everyday life.

The experience combines:

* Open-world exploration.
* Procedural world generation.
* Large cities.
* Dynamic NPCs.
* Economy and trading.
* Cultural interactions.
* Food and cooking.
* Festivals.
* Random events.
* Rare structures.
* Collectible items.
* Reputation.
* Transportation.
* Endgame encounters.
* Satirical and absurd events.
* Multiplayer cooperation and chaos.

The project should feel like a **playable world first**, rather than a linear Minecraft adventure map.

The player should be able to explore freely, discover systems naturally and create their own stories.

---

# 2. Design Pillars

The game is built around the following pillars.

## 2.1 Exploration

The world should constantly provide reasons to explore.

Players should discover:

* Cities.
* Villages.
* Religious structures.
* Markets.
* Beaches.
* Farms.
* Desert areas.
* Dead Sea locations.
* Historical locations.
* Technology districts.
* Rare structures.
* Hidden structures.
* Easter eggs.
* Rare NPCs.
* World events.

Exploration should be meaningful without requiring a traditional linear questline.

---

## 2.2 Discovery

Important content should often be discovered rather than explicitly explained.

Examples:

* Finding a rare synagogue.
* Discovering a hidden desert structure.
* Encountering an unusual NPC.
* Finding a rare event.
* Discovering a secret interaction.
* Finding an extremely rare item.
* Discovering an easter egg.

The player should frequently have the feeling:

> "I didn't know this existed."

---

## 2.3 Interaction

The world should be interactive.

Players should be able to:

* Talk to NPCs.
* Trade.
* Eat.
* Cook.
* Pray.
* Attend events.
* Participate in festivals.
* Use transportation.
* Explore buildings.
* Interact with landmarks.
* Participate in markets.
* Discover hidden mechanics.
* Fight special enemies/bosses.

---

## 2.4 Collection

Rare objects and discoveries provide long-term goals.

Examples:

* Cultural equipment.
* Rare collectibles.
* Music discs.
* Legendary items.
* Mythic equipment.
* Achievement progression.
* Rare structures.
* Event rewards.

Collection should encourage exploration rather than require repetitive grinding.

---

## 2.5 Emergent Chaos

The mod should retain Minecraft's sandbox nature.

Systems should be capable of interacting in unexpected ways:

* NPCs gathering during events.
* Players discovering rare structures while travelling.
* Markets becoming crowded.
* Events happening while players are exploring cities.
* Boss encounters interrupting normal gameplay.
* Rare NPCs appearing unexpectedly.
* Festivals changing normal NPC routines.

The game should produce memorable situations without requiring scripted cutscenes for everything.

---

# 3. Core Gameplay Loop

The primary gameplay loop is:

```text
EXPLORE
   ↓
DISCOVER
   ↓
INTERACT
   ↓
TRADE / CRAFT / COOK
   ↓
COMPLETE EVENTS
   ↓
OBTAIN RARE ITEMS
   ↓
EXPLORE MORE DANGEROUS / RARE AREAS
   ↓
DISCOVER MORE CONTENT
```

There is no mandatory linear campaign.

Players should be able to enter the world and immediately choose their own direction.

---

# 4. Player Progression

Progression is primarily **horizontal and discovery-driven**, rather than a traditional RPG progression system.

The player should become more capable through:

* Equipment.
* Knowledge of the world.
* Reputation.
* Access to rare trades.
* Discoveries.
* Achievements.
* Rare items.
* Transportation.
* Access to special events.
* Exploration of increasingly unusual areas.

The mod should not depend on a conventional:

```text
Level 1 → Level 2 → Level 3 → Level 4
```

system.

Minecraft's existing progression can coexist with the mod.

---

# 5. World Structure

The world should contain several major geographical and cultural regions.

Primary regions:

* Mediterranean Coast.
* Tel Aviv.
* Jaffa.
* Jerusalem.
* Dead Sea.
* Desert.
* Agricultural regions.
* Villages.
* Technology Districts.
* Rural areas.
* Historical areas.

These regions should feel meaningfully different.

---

# 6. Mediterranean Coast

The Mediterranean coastline should include:

* Beaches.
* Coastal terrain.
* Tourist areas.
* Restaurants.
* Shops.
* Ports.
* NPC activity.
* Beach events.
* Boats.
* Coastal vegetation.
* Urban areas.

The coast should act as both a geographical region and a gameplay area.

---

# 7. Tel Aviv

Tel Aviv is intended to be one of the largest and most dynamic urban areas.

## 7.1 Architecture

The city can include:

* Skyscrapers.
* Apartment buildings.
* Offices.
* Hotels.
* Shops.
* Restaurants.
* Cafes.
* Bars.
* Streets.
* Parks.
* Beaches.
* Modern buildings.
* Bauhaus-inspired architecture.

---

## 7.2 Areas

Potential recognizable areas include:

* Rothschild Boulevard-inspired area.
* Florentin-inspired area.
* Sarona-inspired area.
* White City/Bauhaus-inspired area.
* Startup/technology district.
* Beachfront.
* Nightlife areas.

These areas should have different NPC populations and gameplay characteristics.

---

## 7.3 Tel Aviv Gameplay

Tel Aviv should emphasize:

* Urban exploration.
* Shopping.
* Restaurants.
* Nightlife.
* Technology.
* Transportation.
* Dynamic NPC activity.
* Events.
* Rare urban structures.

---

# 8. Jaffa

Jaffa should have a distinct identity from Tel Aviv.

Features:

* Port.
* Old city.
* Narrow alleys.
* Markets.
* Historical buildings.
* Restaurants.
* Shops.
* Coast.
* Clock tower.
* Flea market.

Jaffa should feel older, denser and more historical than the modern areas of Tel Aviv.

---

# 9. Jerusalem

Jerusalem should contain:

* Old City.
* Narrow streets.
* Markets.
* Neighborhoods.
* Historical buildings.
* Religious sites.
* Synagogues.
* Modern areas.
* Rare structures.
* NPCs.
* Shops.
* Restaurants.

Jerusalem should have a stronger focus on:

* History.
* Culture.
* Religious structures.
* Exploration.
* Rare landmarks.
* Special interactions.

---

# 10. Western Wall

The Western Wall is a major landmark and gameplay location.

It should be represented as a generated landmark rather than a simple decorative structure.

It should include:

* Landmark architecture.
* NPCs.
* Lighting.
* Decorations.
* Collision.
* Interaction points.
* Prayer-related interactions.
* Particles.
* Animation.

---

# 11. Western Wall Interaction

A special prayer interaction should require:

* Kippah.
* Prayer Note.

The player interacts with the designated location.

The interaction should:

1. Validate the player server-side.
2. Verify the required equipment.
3. Verify the Prayer Note.
4. Start the interaction sequence.
5. Play appropriate animation.
6. Display appropriate particles/effects.
7. Consume the required resource where applicable.
8. Grant the reward.
9. Apply the Blessed Effect.
10. Start a cooldown.

Reward:

* **5 Diamonds**
* **Blessed Effect**

The interaction must have server-side anti-abuse protection.

Players must not be able to repeatedly trigger the reward through packet manipulation, reconnecting or other multiplayer exploits.

---

# 12. Kippah

The Kippah is a wearable cultural item.

Requirements:

* Head equipment.
* Custom model.
* Texture.
* First-person representation where appropriate.
* Third-person representation.
* Multiplayer synchronization.
* Interaction compatibility.

The Kippah can act as a requirement for certain cultural interactions.

It should not provide excessive combat power.

---

# 13. Talit

The Talit is a wearable cultural item.

Possible gameplay effects:

* Spiritual bonus.
* Resistance.
* Luck.
* Contextual interactions.

The Talit should be less powerful than the Rabbi's Crown.

Its primary purpose is cultural/gameplay interaction rather than raw combat power.

---

# 14. Tefillin

Tefillin should be implemented as a contextual interaction item.

Possible behavior:

* Player interaction.
* Animation.
* Temporary bonus.
* Cooldown.
* Achievement.

The system should be simple enough to remain fun rather than attempting to simulate every real-world religious detail.

---

# 15. Payot

Payot can appear as:

* Cosmetic elements.
* Part of the Rabbi's Crown.
* Optional character/equipment details.

Variants may include:

* Short.
* Medium.
* Long.
* Ornate.

Payot should primarily affect visual identity rather than become a separate combat mechanic.

---

# 16. Rabbi's Crown

The Rabbi's Crown is one of the rarest items in the game.

### Rarity

**MYTHIC**

It should be extremely difficult to obtain.

It should not be:

* Normally craftable.
* Common chest loot.
* Easily farmable.
* Obtainable through ordinary villager trading.

Possible acquisition sources:

* Extremely rare structures.
* Endgame events.
* Special encounters.
* Ultra-rare loot.
* Hidden content.

---

## 16.1 Appearance

The Crown should include:

* Headwear.
* Long beard.
* Payot.
* Decorative details.
* Custom 3D model.
* Custom texture.

It should be visually obvious that the item is exceptionally rare.

---

## 16.2 Combat Value

The Crown provides:

**+20 Armor**

This makes it an extremely powerful equipment item.

---

## 16.3 Blessed Trader

While equipped, the player receives the permanent:

**Blessed Trader**

effect.

The effect modifies compatible villager trading behavior.

Possible minimum trade values can be configured to values such as:

**1 Emerald**

The implementation must avoid:

* Infinite trade loops.
* Item duplication.
* Negative prices.
* Exploit-based generation.
* Invalid trade states.

The effect must be server-authoritative.

---

# 17. Food System

Food is a major part of the world.

Foods include:

* Falafel.
* Hummus.
* Shakshuka.
* Sabich.
* Tahini.
* Challah.
* Rugelach.
* Dates.
* Olives.
* Citrus.

Additional foods can be added when they meaningfully contribute to the gameplay.

Every food should have:

* Recipe.
* Ingredients.
* Nutrition.
* Saturation.
* Texture/model.
* Optional gameplay effect.

---

# 18. Kosher System

The mod should include a **simplified gameplay-oriented kosher system**.

It should use:

* Ingredient tags.
* Recipe tags.
* Tool/utensil compatibility where necessary.
* Food categories.

The system should not attempt to become a complete simulation of Jewish dietary law.

The objective is to create gameplay interactions inspired by the concept while keeping the mechanic understandable and fun.

---

# 19. Agriculture

Agricultural regions should provide:

* Farms.
* Crops.
* Villages.
* Farmer NPCs.
* Markets.
* Agricultural products.

Resources include:

* Olives.
* Dates.
* Citrus.
* Wheat.
* Vegetables.
* Grapes/vineyards.

Agriculture should connect to:

* Food.
* Economy.
* Trading.
* NPC routines.
* Villages.
* Exploration.

---

# 20. Dead Sea

The Dead Sea is a unique environmental region.

Features:

* Distinct water.
* High buoyancy.
* Salt.
* Minerals.
* Unique terrain.
* Tourism.
* Resorts.
* Tourist NPCs.
* Rare resources.
* Special landmarks.

The Dead Sea should feel mechanically different from normal Minecraft water.

---

# 21. Desert

The desert should be a more dangerous exploration region.

Features:

* Sand.
* Rocks.
* Canyons.
* Limited vegetation.
* Rare structures.
* Ruins.
* Special resources.
* Environmental hazards.
* Desert-specific NPCs/entities.

The desert should provide a reason to travel away from cities.

---

# 22. Synagogues

Synagogues should be generated structures.

Possible contents:

* Prayer area.
* Seats.
* Decorations.
* Lighting.
* Library.
* Ritual objects.
* NPCs.
* Interaction points.

Some synagogues should be:

* Small/common.
* Medium.
* Large/rare.

Large synagogues can function as rare exploration locations.

---

# 23. NPC System

NPCs are an important part of the simulation.

Planned professions:

* Merchant.
* Rabbi.
* Farmer.
* Fisherman.
* Chef.
* Artisan.
* Developer.
* Taxi Driver.
* Tourist.
* Musician.
* Historian.
* Shopkeeper.
* Founder.
* Investor.
* Engineer.

NPCs should be functional entities, not static decorations.

---

# 24. NPC Behavior

NPCs should be able to:

* Walk.
* Work.
* Trade.
* Eat.
* Socialize.
* Enter buildings.
* Use appropriate locations.
* Participate in events.
* Follow schedules.
* React to festivals.
* Interact with players.

NPC behavior should remain performance-conscious.

---

# 25. NPC Schedules

A baseline schedule can be:

| Time  | Activity |
| ----- | -------- |
| 06:00 | Wake     |
| 08:00 | Work     |
| 12:00 | Lunch    |
| 14:00 | Work     |
| 18:00 | Social   |
| 22:00 | Home     |

This schedule is a baseline rather than a universal rigid rule.

Different professions and events may override it.

Examples:

* Restaurant workers work later.
* Taxi drivers can have extended shifts.
* Musicians may appear during events.
* Festival behavior can override normal routines.
* Shabbat can modify schedules.

---

# 26. Dynamic City Life

Cities should feel alive.

NPCs should:

* Walk through streets.
* Work.
* Shop.
* Eat.
* Socialize.
* Enter buildings.
* Use transportation.
* Attend events.

Large cities must have population limits and AI optimizations to avoid excessive server load.

---

# 27. Economy

The economy should connect:

```text
Farming
   ↓
Resources
   ↓
Food / Products
   ↓
Markets / Shops
   ↓
NPCs
   ↓
Trading
   ↓
Reputation
```

Different regions should have different economic characteristics.

---

# 28. Urban Economy

Urban areas should emphasize:

* Restaurants.
* Shops.
* Technology.
* Tourism.
* Services.
* Nightlife.
* Transportation.

Tel Aviv should have a stronger modern/technology-oriented economy.

---

# 29. Rural Economy

Rural areas should emphasize:

* Farming.
* Crops.
* Food production.
* Agricultural trading.
* Villages.
* Local markets.

---

# 30. Reputation

Reputation categories can include:

* Merchants.
* Cities.
* Villages.
* Religious NPCs.
* Technology District.
* Special factions.

Reputation can affect:

* Prices.
* Dialogue.
* Access.
* Events.
* Rare trades.
* NPC reactions.

Reputation should not become a mandatory grind.

---

# 31. Technology District

The Technology District represents Israel's technology/startup culture in a fictionalized Minecraft environment.

Structures:

* Startup offices.
* Offices.
* Servers.
* Labs.
* Coworking spaces.
* Conference rooms.
* Technology facilities.

NPCs:

* Developer.
* Founder.
* Investor.
* Engineer.

Gameplay can include:

* Technology-themed events.
* Trading.
* Rare items.
* Offices.
* Hidden rooms.
* Easter eggs.

---

# 32. Transportation

Transportation should connect the world.

Systems may include:

* Walking.
* Bicycle.
* Bus.
* Train.
* Taxi.
* Boat.

Transportation should primarily solve:

* Long-distance travel.
* City connectivity.
* NPC movement.
* Exploration.

Cities should not become isolated map fragments.

---

# 33. World Events

The world should contain dynamic events.

Planned events:

* Public Speech.
* Market Day.
* Festival.
* Concert.
* Beach Event.
* Religious Event.
* Food Festival.
* Technology Conference.
* Rare NPC Spawn.
* Boss Event.

Every event should have:

* Conditions.
* Start.
* Active state.
* Duration.
* Participants.
* Rewards.
* Cooldown.
* Cleanup.

---

# 34. Public Speech Event

The Public Speech is a fictional/satirical world event.

It can contain:

* Gazebo.
* Stage.
* Microphone.
* Speakers.
* Speaker NPC.
* Crowd.
* Signs.
* Chairs.

Crowd behavior can include:

* Applause.
* Booing.
* Movement.
* Dialogue.
* Idle behavior.

Participation for at least **60 seconds** can grant:

**First Amendment**

The event should remain clearly fictional and game-oriented.

It must not represent fictional gameplay events as factual depictions of real-world political events.

---

# 35. First Amendment

The First Amendment is a rare collectible.

### Rarity

**LEGENDARY**

Potential behavior:

* Collectible.
* Event reward.
* Optional Freedom effect.

It should primarily function as a rare achievement/collection item.

---

# 36. Festivals

Planned festivals/events include:

* Shabbat.
* Rosh Hashanah.
* Yom Kippur.
* Sukkot.
* Hanukkah.
* Purim.
* Pesach.

Festival systems should be:

* Configurable.
* Optional where appropriate.
* Non-intrusive.
* Server-authoritative.

Festivals can modify:

* NPC routines.
* Decorations.
* Food.
* Structures.
* Audio.
* Events.
* Rewards.

---

# 37. Shabbat

Shabbat can affect the simulated world.

Possible effects:

* NPC schedules change.
* Certain activities become less common.
* Social behavior changes.
* Structures become active in different ways.
* Special food appears.
* Atmosphere changes.

The mechanic should remain a gameplay abstraction and should not force players to follow religious rules.

---

# 38. Hanukkah

Hanukkah should include:

* Menorah.
* Lighting.
* Decorations.
* Food.
* NPC behavior.
* Event behavior.
* Rewards.

The Menorah can have a candle progression from:

**1 → 8 candles**

depending on the festival day/state.

---

# 39. Menorah

The Menorah should be:

* A placeable object.
* Interactive.
* Visually recognizable.
* Festival-aware.

Possible interactions:

* Lighting candles.
* Producing light.
* Particles.
* Festival progression.
* Achievements.

Particle usage must be optimized.

---

# 40. Other Festivals

Other festivals should use the same generic event architecture.

Each festival can define:

* Date/condition.
* Decorations.
* NPC behavior.
* Food.
* Structures.
* Audio.
* Events.
* Rewards.
* Achievements.

The system should be extensible so additional festivals do not require rewriting the event framework.

---

# 41. Bibi Boss

The mod contains a fictional/satirical boss inspired by Benjamin Netanyahu.

This is a **game character**, not a factual simulation of the real person.

The boss should exist as exaggerated Minecraft gameplay.

---

## 41.1 Boss Statistics

Default health:

**10,000+ HP**

Health and damage should be configurable.

---

## 41.2 Boss States

The boss uses:

```text
IDLE
 ↓
ALERT
 ↓
COMBAT
 ↓
ENRAGED
 ↓
DEFEATED
```

---

## 41.3 Combat

Possible mechanics:

* Ranged attacks.
* Defensive behavior.
* Special attacks.
* Guards.
* Escape behavior.
* Enrage phase.
* Boss arena/area.
* Boss bar.

Combat should be challenging enough to qualify as an endgame encounter.

---

# 42. Boss Spawning

Possible spawn sources:

* Government building.
* Special event.
* Rare structure.
* Dedicated boss encounter.

The boss should not spawn constantly.

Boss spawning should be configurable.

---

# 43. Boss Guards

The boss may have guard entities.

Guards should:

* Protect the boss.
* Attack players.
* Use simple coordinated AI.
* Avoid excessive entity spawning.

The system must prevent runaway mob population.

---

# 44. Boss Rewards

Primary planned reward:

**Hava Nagila music disc**

### Rarity

**LEGENDARY**

The audio must use:

* Original music.
* Compatible licensed music.
* Or an appropriately licensed recording.

A commercial copyrighted recording must not be bundled without redistribution rights.

---

# 45. Music

The soundtrack can include:

* Jewish cultural-inspired music.
* Israeli-inspired music.
* City ambience.
* Festival music.
* Event music.
* Boss music.
* Environmental ambience.

Music should reinforce the identity of each region.

---

# 46. Achievements

Planned achievements:

* **Welcome to Israel**
* **Shalom**
* **Visit Jerusalem**
* **Tel Aviv Nights**
* **Jaffa**
* **Dead Sea Tourist**
* **Five Diamonds**
* **Blessed Trader**
* **Hava Nagila**
* **Freedom of Speech**
* **Startup Founder**
* **Master Explorer**

Achievements should encourage exploration and interaction rather than repetitive grinding.

---

# 47. Rarity System

The game uses:

```text
COMMON
UNCOMMON
RARE
EPIC
LEGENDARY
MYTHIC
```

Important items:

| Item            | Rarity    |
| --------------- | --------- |
| Rabbi's Crown   | MYTHIC    |
| First Amendment | LEGENDARY |
| Hava Nagila     | LEGENDARY |

Rarity must have actual gameplay meaning.

A MYTHIC item should not become effectively common through an unintended farming loop.

---

# 48. Rare Structures

Rare structures can include:

* Large synagogue.
* Historical house.
* Market.
* Desert ruins.
* Dead Sea resort.
* Startup office.
* Government building.
* Rare religious structure.
* Secret easter-egg structure.

Rare structures should provide exploration rewards.

---

# 49. Easter Eggs

The game should contain hidden content.

Examples:

* Random NPC dialogue.
* Absurd events.
* Hidden structures.
* Meme objects.
* Secret achievements.
* Ultra-rare events.
* Procedural dialogue.
* Hidden interactions.

Easter eggs should reward curiosity.

---

# 50. Quest Design

The mod should **not** depend on a mandatory linear questline.

Instead, progression comes from:

* Exploration.
* Discoveries.
* Events.
* Achievements.
* Collections.
* Reputation.
* Rare structures.
* Rare items.

Optional quests can exist, but the player should remain free to ignore them.

---

# 51. Exploration Difficulty

The world should contain a natural progression of risk.

Example:

```text
Safe areas
   ↓
Cities
   ↓
Rural areas
   ↓
Desert / dangerous regions
   ↓
Rare structures
   ↓
Special events
   ↓
Endgame encounters
```

This does not require traditional level scaling.

---

# 52. Multiplayer

Multiplayer is a first-class feature.

Players should be able to:

* Explore together.
* Trade.
* Attend events.
* Fight bosses.
* Discover structures.
* Participate in festivals.
* Build around cities.
* Collect rare items.

---

# 53. Server Authority

Important gameplay decisions must be controlled by the server.

Server authority applies to:

* Rewards.
* Trades.
* Currency.
* Reputation.
* Events.
* Bosses.
* Cooldowns.
* Rare items.
* Achievements.
* Prayer interactions.
* Festival state.
* Progression.

Clients must not be trusted to determine important rewards.

---

# 54. Multiplayer Exploit Prevention

The game must protect against:

* Item duplication.
* Reward duplication.
* Trade duplication.
* Cooldown bypass.
* Event farming.
* Boss reward farming.
* Prayer reward farming.
* Invalid packets.
* Client-side reward manipulation.
* Reconnect exploits.
* Death/reward exploits.

---

# 55. Performance Philosophy

The mod should support large cities and dynamic NPCs without making the game unnecessarily expensive.

Avoid:

* Heavy global tick handlers.
* Excessive pathfinding.
* Excessive NPC populations.
* Excessive particles.
* Expensive chunk generation.
* Unbounded event systems.
* Memory leaks.
* Constant recalculation of static data.

Large systems should use:

* Configurable limits.
* Distance checks.
* Event-driven logic.
* Cached data.
* Efficient AI.
* Chunk-aware processing.

---

# 56. World Generation Philosophy

World generation must be:

* Deterministic.
* Seed-compatible.
* Chunk-safe.
* Multiplayer-safe.
* Performance-conscious.

Structures should not generate excessively close together.

Cities should feel large without forcing every chunk to contain expensive custom generation.

---

# 57. City Design Philosophy

Each major city should have its own identity.

### Tel Aviv

Modern, urban, technological, commercial and nightlife-focused.

### Jaffa

Historical, coastal, dense, market-oriented and older in architectural identity.

### Jerusalem

Historical, religious, cultural and landmark-focused.

Cities should not simply be reskinned copies of one another.

---

# 58. Cultural Design Philosophy

The mod is inspired by real cultural elements but is ultimately a game.

Cultural content should:

* Be recognizable.
* Be respectful.
* Avoid reducing groups to stereotypes.
* Provide actual gameplay value.
* Avoid requiring real-world religious observance from players.
* Clearly distinguish fictional game mechanics from real-world facts.

---

# 59. Satirical Content

Satirical content is allowed and is part of the project's tone.

Real-world public figures may inspire fictional game characters.

However:

* Game characters should be clearly fictionalized.
* Fictional events must not be presented as real events.
* Real-world claims should not be invented.
* Gameplay exaggeration should be obvious.
* Satire should remain within the game's fictional world.

---

# 60. Art Direction

The visual identity should remain compatible with Minecraft while providing enough custom content to establish a distinct world.

Priorities:

1. Recognizable silhouettes.
2. Strong regional identity.
3. Functional structures.
4. Readable items.
5. Consistent textures.
6. Performance.
7. Multiplayer visibility.

Custom models should be used where they meaningfully improve the experience.

---

# 61. Asset Policy

The project should use only:

* Original assets.
* Procedurally generated assets.
* Assets with compatible licenses.
* Assets explicitly allowing commercial redistribution where required.

Do not use unauthorized:

* Models.
* Textures.
* Music.
* Sounds.
* Fonts.
* Character assets.

Every external asset should be documented.

Required documentation:

* `CREDITS.md`
* `ASSET_LICENSES.md`

---

# 62. No AI-Generated Game Assets

The project should not rely on AI-generated game assets.

AI tools may be used as development assistance for:

* Programming.
* Documentation.
* Planning.
* Debugging.
* Technical research.

Game assets themselves should come from:

* Original work.
* Procedural generation.
* Properly licensed existing assets.

---

# 63. Configuration Philosophy

Important gameplay parameters should be configurable where practical.

Examples:

* World generation frequency.
* City frequency.
* Structure frequency.
* NPC spawn rates.
* Event frequency.
* Event duration.
* Rewards.
* Boss HP.
* Boss damage.
* Item rarity.
* Cooldowns.
* Festival behavior.
* Music.
* Particles.
* Population limits.

Configuration should not make every tiny gameplay value unnecessarily complicated.

---

# 64. Data-Driven Design

Where appropriate, content should be data-driven.

Candidates:

* Recipes.
* Loot tables.
* Tags.
* Achievements.
* Structures.
* Worldgen.
* Item metadata.
* Events.
* NPC configuration.
* Festival definitions.

Hardcoded logic should be reserved for systems that genuinely require code.

---

# 65. Technical Target

The primary technical target is:

**Minecraft Java Edition + NeoForge 26.2**

The project must be designed around the APIs and architecture actually available in the selected NeoForge version.

The implementation must not assume:

* Fabric APIs.
* Forge APIs.
* Older Minecraft APIs.
* APIs from unrelated mod loaders.

If compatibility with previous versions is desired, it must be verified rather than assumed.

---

# 66. CI/CD Requirement

CI/CD is a core project requirement.

The project must have an automated pipeline that:

```text
Checkout repository
        ↓
Install required Java
        ↓
Configure Gradle
        ↓
Compile mod
        ↓
Run tests
        ↓
Validate resources/data
        ↓
Perform available game/server validation
        ↓
Package mod
        ↓
Produce JAR
        ↓
Upload JAR as artifact
```

A source tree that works only on the developer's local machine is not considered sufficient.

The resulting `.jar` must be downloadable from the CI run.

---

# 67. Build Verification

A valid build must demonstrate:

* Successful compilation.
* Successful tests.
* Successful resource processing.
* Successful mod packaging.
* Valid mod metadata.
* Valid JAR.
* Successful NeoForge loading where automated validation is available.

The final JAR should be tested in a clean NeoForge installation.

---

# 68. Mod Architecture

The implementation should be logically separated into systems such as:

```text
world/
biomes/
structures/
cities/
blocks/
items/
equipment/
entities/
npc/
bosses/
animation/
events/
festivals/
food/
trading/
economy/
transport/
quests/
achievements/
network/
client/
server/
config/
audio/
```

The exact package structure may change if the repository architecture provides a better solution.

The important principle is separation of responsibilities.

---

# 69. System Relationships

Major systems should connect naturally.

```text
WORLD
 │
 ├── BIOMES
 │     ├── DESERT
 │     ├── COAST
 │     ├── AGRICULTURE
 │     └── DEAD SEA
 │
 ├── CITIES
 │     ├── TEL AVIV
 │     ├── JAFFA
 │     └── JERUSALEM
 │
 ├── NPCs
 │     ├── ECONOMY
 │     ├── TRADING
 │     ├── EVENTS
 │     └── FESTIVALS
 │
 ├── STRUCTURES
 │     ├── SYNAGOGUES
 │     ├── MARKETS
 │     ├── OFFICES
 │     └── RARE STRUCTURES
 │
 ├── CULTURE
 │     ├── KIPPAH
 │     ├── TALIT
 │     ├── TEFILLIN
 │     ├── MENORAH
 │     └── WESTERN WALL
 │
 ├── FOOD
 │     └── ECONOMY
 │
 ├── EVENTS
 │     ├── FESTIVALS
 │     ├── PUBLIC SPEECH
 │     └── BOSS EVENTS
 │
 └── ENDGAME
       ├── RARE ITEMS
       ├── BOSS
       └── LEGENDARY / MYTHIC CONTENT
```

---

# 70. Progression Architecture

The intended progression is:

```text
Spawn
  ↓
Explore nearby area
  ↓
Discover first city/village
  ↓
Interact with NPCs
  ↓
Trade / cook / explore
  ↓
Discover landmarks
  ↓
Build reputation
  ↓
Participate in events
  ↓
Travel further
  ↓
Discover rare structures
  ↓
Acquire rare equipment
  ↓
Explore dangerous regions
  ↓
Encounter endgame content
  ↓
Collect legendary/mythic items
  ↓
Continue exploring
```

The loop should remain open-ended.

---

# 71. Endgame

Endgame is not a final credits sequence.

Instead, endgame consists of:

* Rare structures.
* Mythic items.
* Legendary collectibles.
* Boss encounters.
* Rare events.
* High-level reputation.
* Exploration completion.
* Achievement completion.
* Easter eggs.
* Multiplayer challenges.

After reaching endgame, players should still have reasons to explore.

---

# 72. Replayability

Replayability should come primarily from:

* Procedural world generation.
* Different world seeds.
* Random events.
* Rare structures.
* Rare NPCs.
* Dynamic city life.
* Multiplayer interactions.
* Randomized discovery.
* Rare loot.
* Easter eggs.

The game should avoid relying exclusively on artificial grinding to create replayability.

---

# 73. Player Freedom

The player should generally be able to decide:

* Where to travel.
* Which city to visit.
* Which activities to perform.
* Which NPCs to interact with.
* Whether to participate in events.
* Whether to pursue rare items.
* Whether to focus on exploration.
* Whether to focus on economy.
* Whether to focus on combat.
* Whether to play cooperatively.

---

# 74. What the Mod Should NOT Become

The project should avoid becoming:

* A linear quest-only RPG.
* A generic survival overhaul.
* A farming simulator.
* A pure combat mod.
* A static museum.
* A collection of disconnected memes.
* A city-building simulator.
* An unnecessarily complex religious simulator.
* An NPC-heavy lag generator.
* A collection of decorative structures with no gameplay.
* A progression system based entirely on grinding.

---

# 75. Design Priority

When trade-offs are necessary, prioritize:

1. **Working gameplay**
2. **World exploration**
3. **System integration**
4. **Multiplayer correctness**
5. **Performance**
6. **Rare-content integrity**
7. **Visual quality**
8. **Additional polish**

A visually impressive feature that does not function correctly is not considered complete.

---

# 76. Feature Quality Standard

A feature is considered complete only when it has all relevant components.

For example, an item is not complete merely because the Java registry entry exists.

A complete item may require:

```text
Registry
+ Properties
+ Model
+ Texture
+ Localization
+ Recipe/Loot
+ Behavior
+ Rendering
+ Multiplayer synchronization
+ Tests
```

The exact requirements depend on the item.

The same principle applies to:

* NPCs.
* Structures.
* Events.
* Bosses.
* Cities.
* Worldgen.
* Festivals.

---

# 77. Testing Philosophy

Testing must happen throughout development rather than only before release.

Important systems should be tested in:

* Singleplayer.
* Dedicated server.
* Multiplayer.
* New worlds.
* Existing worlds where applicable.
* Chunk unload/reload.
* Server restart.
* Player reconnect.
* Multiple players.

---

# 78. Performance Requirements

Performance should be considered during implementation.

Especially sensitive systems:

* Large cities.
* NPC AI.
* Pathfinding.
* World generation.
* Dynamic events.
* Boss AI.
* Transportation.
* Particles.
* Networking.

The mod should avoid introducing expensive operations on every tick when event-driven or cached alternatives exist.

---

# 79. Multiplayer Design Requirements

Every multiplayer-critical feature must answer:

* Who owns the state?
* Where is the state stored?
* Who validates the action?
* What happens if the player disconnects?
* What happens if the chunk unloads?
* What happens after server restart?
* Can two players trigger it simultaneously?
* Can the reward be duplicated?
* Can the client fake the interaction?

If these questions cannot be answered, the feature is not multiplayer-ready.

---

# 80. Save/Persistence

Persistent gameplay state may include:

* Reputation.
* Achievements.
* Event state where required.
* Cooldowns where required.
* Special progression.
* Rare world state.
* NPC state where required.

Persistence must survive:

* Player logout.
* Player reconnect.
* Server restart.
* Chunk unload/load.

---

# 81. Configuration vs Gameplay Data

Use configuration for:

* Server-owner preferences.
* Frequencies.
* Difficulty.
* Population.
* Rewards.
* Performance limits.

Use data-driven resources for:

* Recipes.
* Loot.
* Tags.
* Static content definitions.
* Achievements.
* Structures where supported.

Use code for:

* Complex behavior.
* AI.
* Combat.
* Dynamic systems.
* Networking.
* Stateful simulation.

---

# 82. Development Philosophy

Development should proceed incrementally.

Each subsystem should reach a working state before large amounts of dependent content are built on top of it.

Preferred progression:

```text
Foundation
    ↓
Core Registries
    ↓
Items / Blocks / Effects
    ↓
World Generation
    ↓
Structures
    ↓
NPC Framework
    ↓
Economy
    ↓
Cities
    ↓
Culture
    ↓
Events
    ↓
Rare Content
    ↓
Boss / Endgame
    ↓
Multiplayer Hardening
    ↓
Performance
    ↓
QA
    ↓
Release
```

---

# 83. Definition of Done

The overall project is complete when:

* NeoForge 26.2 is supported.
* The project builds from a clean environment.
* CI/CD works.
* CI/CD produces a downloadable `.jar`.
* The mod launches.
* Dedicated server launches.
* World generation works.
* Major regions work.
* Tel Aviv works.
* Jaffa works.
* Jerusalem works.
* Western Wall interaction works.
* Dead Sea works.
* Desert works.
* Agriculture works.
* NPCs work.
* NPC schedules work.
* Economy works.
* Trading works.
* Reputation works.
* Food works.
* Festivals work.
* Events work.
* Public Speech works.
* First Amendment works.
* Rabbi's Crown works.
* Blessed Trader works.
* Bibi boss works.
* Boss rewards work.
* Transportation works.
* Achievements work.
* Easter eggs exist.
* Multiplayer works.
* Major exploits are addressed.
* Performance is acceptable.
* Assets are properly licensed.
* Documentation is complete.
* Release JAR has been tested in a clean environment.

---

# 84. Intended Player Experience

The ideal experience should feel approximately like:

```text
"I spawned in a Minecraft world."

        ↓

"I found a strange city."

        ↓

"Why are there NPCs actually living here?"

        ↓

"I can trade with them."

        ↓

"There's a market over there."

        ↓

"Wait, there's a huge city beyond it."

        ↓

"I found Jaffa."

        ↓

"There's a rare structure here."

        ↓

"I need to go to Jerusalem."

        ↓

"What's that event happening?"

        ↓

"I got a weird collectible."

        ↓

"How rare is this thing?"

        ↓

"I found the Dead Sea."

        ↓

"Why am I floating?"

        ↓

"I found something in the desert."

        ↓

"There's a boss here?"

        ↓

"WHAT IS THIS."

        ↓

"I need that item."

        ↓

"Let's go explore again."
```

The game should continuously generate moments of curiosity, discovery and surprise.

---

# 85. Final Design Principle

**Israel-Simulator is a world, not a checklist.**

The systems should reinforce one another.

Cities should contain NPCs.

NPCs should participate in economies.

Economies should connect to food and agriculture.

Structures should contain discoveries.

Discoveries should lead to rare content.

Events should change the world temporarily.

Festivals should affect NPC behavior.

Transportation should connect regions.

Rare items should encourage exploration.

Multiplayer should amplify emergent situations.

The ultimate objective is to create a Minecraft world where the player can continuously say:

> **"Let's see what's over there."**
