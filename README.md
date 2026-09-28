# Israel-Simulator

> A Minecraft Java Edition sandbox expansion focused on exploration, culture, cities, dynamic NPCs, events, rare discoveries, and chaotic endgame content.

**Israel-Simulator** is a Minecraft Java Edition mod that transforms the vanilla world into a large open-world sandbox inspired by Israel, Israeli culture, Jewish traditions, Mediterranean environments, modern cities, historical locations, food, commerce, festivals, and fictional/comedic events.

The project combines two distinct experiences:

* **Cultural Simulation** — cities, architecture, food, traditions, festivals, NPCs, landmarks, transportation, and everyday-world simulation.
* **Chaotic Sandbox** — rare events, powerful items, secret structures, boss encounters, easter eggs, absurd NPCs, and unexpected discoveries.

The goal is not to create a linear campaign. The player should be able to explore freely and continuously discover new systems, locations, characters, and events.

---

## Features

### 🌍 Open World

The world contains multiple distinct environments and regions:

* Mediterranean coast
* Tel Aviv
* Jaffa
* Jerusalem
* Dead Sea
* Desert regions
* Agricultural areas
* Villages
* Technology districts
* Historical areas
* Rare structures
* Secret locations

Different regions have their own architecture, NPC behavior, activities, resources, structures, and atmosphere.

---

### 🏙️ Dynamic Cities

Cities are intended to feel like living environments rather than static structures.

NPCs can:

* Walk around
* Work
* Trade
* Eat
* Socialize
* Enter buildings
* Use transportation
* Participate in events
* Follow daily schedules

Major cities have different architectural and gameplay identities.

#### Tel Aviv

Includes concepts such as:

* Modern skyscrapers
* Beaches
* Hotels
* Restaurants
* Cafés
* Shops
* Rothschild Boulevard
* Florentin
* Sarona
* White City / Bauhaus architecture
* Technology district
* Offices
* Nightlife
* Public transportation

#### Jaffa

A historical coastal area featuring:

* Old city streets
* Port
* Markets
* Restaurants
* Shops
* Clock tower
* Flea market
* Mediterranean architecture

#### Jerusalem

A major exploration and endgame location containing:

* Old City
* Markets
* Historical buildings
* Synagogues
* Religious landmarks
* Modern districts
* Rare structures
* Special encounters

---

## 🗺️ Exploration & Landmarks

Exploration is one of the core gameplay systems.

Major discoveries include:

* Western Wall
* Synagogues
* Historical buildings
* Markets
* Desert ruins
* Dead Sea resorts
* Government buildings
* Technology offices
* Rare religious structures
* Secret easter-egg structures

The world map progressively records discovered landmarks.

---

## 🕍 Cultural Content

The mod includes optional cultural and religious content that can be discovered through exploration and interaction.

Planned content includes:

* Kippah
* Talit
* Tefillin
* Menorah
* Synagogues
* Prayer Notes
* Jewish festivals
* Hebrew signage
* Religious NPCs
* Cultural objects
* Traditional food

These systems are optional and are not intended to force the player into a linear religious progression.

---

## 🧢 Equipment & Rare Items

Israel-Simulator features a rarity system:

```text
COMMON
UNCOMMON
RARE
EPIC
LEGENDARY
MYTHIC
```

Some items are intentionally extremely difficult to obtain.

### Kippah

Head equipment with:

* Custom model
* Texture
* Third-person rendering
* Multiplayer synchronization
* Compatibility with certain interactions

### Talit

Special equipment providing smaller temporary or passive bonuses.

### Tefillin

A contextual interaction item featuring:

* Animation
* Cooldown
* Temporary bonus
* Achievement

### Rabbi's Crown

A **MYTHIC** endgame item.

The item features a complete custom model including:

* Rabbi-style hat
* Long beard
* Payot
* Decorative details

Stats:

```text
Armor: +20
```

It also provides the permanent:

```text
Blessed Trader
```

effect.

Compatible villager trades can be reduced to their configured minimum cost, such as:

```text
1 Emerald
```

The item is not normally craftable and is intended to be one of the rarest items in the game.

---

## 🧱 Western Wall Interaction

The Western Wall is a generated landmark rather than a decorative placeholder.

Players can interact with it using a **Prayer Note**.

A valid interaction can trigger:

1. Player approaches the Wall.
2. Player faces the landmark.
3. Interaction animation begins.
4. Particles and audio play.
5. Prayer Note is placed.
6. Player receives a temporary blessing.

Example reward:

```text
+5 Diamonds
+ Blessed Effect
```

The reward uses a cooldown to prevent infinite farming.

The **Blessed Effect** can provide temporary bonuses such as:

* Regeneration
* Resistance
* Luck

---

## 🍴 Food & Cooking

The mod introduces an expanded food system inspired by Israeli and Jewish cuisine.

Planned foods include:

* Falafel
* Hummus
* Shakshuka
* Sabich
* Tahini
* Challah
* Rugelach
* Dates
* Olives
* Citrus fruits

Each food item can define:

* Recipe
* Texture
* Model
* Food value
* Saturation
* Optional effects

A lightweight kosher cooking system may also provide ingredient and recipe tagging without turning the mechanic into an unnecessarily complex simulation.

---

## 🎉 Festivals & Events

The world contains a configurable event system.

Planned festivals include:

* Shabbat
* Rosh Hashanah
* Yom Kippur
* Sukkot
* Hanukkah
* Purim
* Pesach

Festivals can modify:

* NPC schedules
* Decorations
* Food
* Audio
* Structures
* Particles
* Events
* Rewards

### Hanukkah

The Menorah can progressively illuminate during the festival:

```text
Day 1 → 1 candle
Day 2 → 2 candles
...
Day 8 → 8 candles
```

---

## 🌊 Dead Sea

The Dead Sea is a dedicated region with unique environmental mechanics.

Planned features include:

* Increased buoyancy
* Unique water behavior
* Salt resources
* Minerals
* Unique terrain
* Tourism
* Special NPCs
* Resort structures

---

## 🏜️ Desert

Desert regions provide more dangerous exploration.

Features include:

* Sand
* Rock formations
* Canyons
* Limited vegetation
* Unique resources
* Rare structures
* Environmental hazards
* Special mobs
* Hidden discoveries

---

## 💻 Technology District

Tel Aviv contains a dedicated technology/startup district.

Possible locations include:

* Startup offices
* Coworking spaces
* Server rooms
* Laboratories
* Conference rooms
* Offices
* Technology companies

NPC professions include:

```text
Developer
Founder
Investor
Engineer
```

---

## 🚕 Transportation

The world includes multiple transportation systems:

* Walking
* Bicycles
* Buses
* Trains
* Taxis
* Boats

Major cities and regions can be connected through the transportation network.

---

## 🧑‍🤝‍🧑 NPC System

NPCs have professions, schedules, trades, dialogue, and preferred locations.

Examples:

```text
Merchant
Rabbi
Farmer
Fisherman
Chef
Artisan
Developer
Taxi Driver
Tourist
Musician
Historian
Shopkeeper
Founder
Investor
Engineer
```

A typical NPC schedule may look like:

```text
06:00 → Wake
08:00 → Work
12:00 → Lunch
14:00 → Work
18:00 → Social
22:00 → Home
```

Schedules can change during festivals and world events.

---

## 💰 Economy & Trading

The economy expands Minecraft's vanilla trading systems.

Possible economic locations include:

* Markets
* Shops
* Restaurants
* Farms
* Technology businesses
* Cultural stores
* Agricultural regions

Different regions have different economic identities.

Urban areas focus more heavily on commerce and technology, while rural regions focus on agriculture and food production.

---

## 🎭 World Events

The world has a dynamic event system.

Possible events include:

```text
Public Speech
Market Day
Festival
Concert
Beach Event
Religious Event
Food Festival
Technology Conference
Rare NPC Spawn
Boss Event
```

Events define:

* Spawn conditions
* Duration
* Participants
* Rewards
* Cooldowns
* Configuration

Events should create unexpected situations while exploring the world.

---

## 🧑‍⚖️ Public Speech Event

A fictional/satirical public event can occur around a gazebo or public stage.

The structure can contain:

* Gazebo
* Stage
* Microphone
* Speaker NPC
* Crowd
* Signs
* Seating
* Public area

NPCs react dynamically through:

* Applause
* Booing
* Movement
* Dialogue
* Idle animations

After participating for at least:

```text
60 seconds
```

the player can complete the event.

Reward:

### First Amendment

A rare collectible featuring:

* Custom lore
* Achievement
* Optional gameplay effect

The event and characters are presented as fictional/satirical game content.

---

## 🧟 Bibi Boss

Israel-Simulator includes a fictional fantasy boss inspired by Benjamin Netanyahu.

The encounter is designed as a comedic gameplay event rather than a representation of real-world events.

### Boss Stats

The boss has extremely high configurable health.

Example:

```text
HP: 10,000+
```

### Boss States

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

Possible abilities include:

* Ranged attacks
* Defensive abilities
* Special attacks
* Summoning guards
* Escape mechanics

All combat mechanics are fictional fantasy gameplay.

### Boss Locations

Possible spawn locations:

* Government building
* Rare structure
* Special world event
* Dedicated boss arena

### Boss Reward

Defeating the boss can reward:

```text
Music Disc — Hava Nagila
```

The included recording must use an appropriately licensed recording or an original recording created for the project.

---

## 🎵 Music

The mod can contain music for:

* Jewish cultural themes
* Israeli-inspired environments
* City ambience
* Festivals
* Special events
* Boss encounters

All included audio must have compatible licensing.

Commercial recordings must not be redistributed without permission.

---

## 🏆 Achievements

Examples include:

| Achievement       | Requirement                              |
| ----------------- | ---------------------------------------- |
| Welcome to Israel | Enter the main region for the first time |
| Shalom            | Interact with your first NPC             |
| Visit Jerusalem   | Discover Jerusalem                       |
| Tel Aviv Nights   | Complete a nighttime Tel Aviv event      |
| Jaffa             | Visit Jaffa                              |
| Dead Sea Tourist  | Enter the Dead Sea region                |
| Five Diamonds     | Complete the Western Wall interaction    |
| Blessed Trader    | Obtain the Rabbi's Crown                 |
| Hava Nagila       | Obtain the music disc                    |
| Freedom of Speech | Complete the Public Speech Event         |
| Startup Founder   | Visit the Technology District            |
| Master Explorer   | Discover all major landmarks             |

---

## ⭐ Reputation

Players can build reputation with different groups and locations.

Possible reputation categories:

```text
Merchants
Cities
Villages
Religious NPCs
Technology District
Special Factions
```

Reputation can affect:

* Prices
* Dialogue
* Access
* Events
* Rare trades

---

## 🥚 Easter Eggs

The world contains optional hidden content.

Examples include:

* Secret structures
* Rare NPCs
* Random dialogue
* Meme items
* Hidden achievements
* Ultra-rare events
* Procedural dialogue
* Hidden interactions

Easter eggs are intended to complement the main world rather than replace it.

---

## 🗺️ Core Gameplay Loop

Israel-Simulator is designed around discovery rather than a mandatory questline.

```text
EXPLORE
   ↓
DISCOVER
   ↓
INTERACT
   ↓
TRADE / CRAFT
   ↓
COMPLETE EVENTS
   ↓
OBTAIN RARE ITEMS
   ↓
EXPLORE RARER / MORE DANGEROUS AREAS
   ↓
DISCOVER ENDGAME CONTENT
```

Players can ignore many systems and simply explore the world.

---

## 🧭 Progression

There is no mandatory linear campaign.

Progression comes from:

* Exploration
* Discoveries
* Optional events
* Collections
* Achievements
* Reputation
* Rare structures
* Rare equipment
* Boss encounters

The world should remain playable as a sandbox even after the major content has been discovered.

---

## 🌐 Multiplayer

Israel-Simulator is designed to support multiplayer.

Important gameplay systems should be server-authoritative:

* Rewards
* Trading
* Boss encounters
* Events
* Cooldowns
* Items
* Progression

The implementation must prevent:

* Item duplication
* Reward abuse
* Client-side exploits
* Desynchronization

---

## ⚙️ Configuration

Major systems should be configurable.

Configuration options include:

* World generation
* City frequency
* Structure generation
* NPC spawning
* Event frequency
* Rewards
* Boss HP
* Boss damage
* Item rarity
* Cooldowns
* Festival calendar
* Music
* Particles

---

## 🚀 Performance

Large cities and procedural generation must be implemented with performance in mind.

Avoid:

* Heavy global tick handlers
* Unnecessary pathfinding
* Excessive entity AI
* Particle spam
* Expensive chunk generation
* Memory leaks

Large structures should use controlled generation and efficient entity management.

---

## 🏗️ Architecture

The project should remain modular.

Suggested package structure:

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

Avoid giant classes and tightly coupled systems.

Where possible, content should use a data-driven approach:

* JSON
* Tags
* Loot tables
* Recipes
* Configuration files
* Datapack-compatible data

---

## 🧪 Testing

The project should test:

### Items

* Kippah
* Rabbi's Crown
* Talit
* Tefillin
* Prayer Note
* First Amendment
* Music Disc

### Gameplay

* Western Wall interaction
* Cooldowns
* Rewards
* Villager discounts
* Boss encounter
* Public Speech
* Festivals

### World

* Biomes
* Cities
* Structures
* Landmarks
* Generation boundaries

### Multiplayer

* Synchronization
* Rewards
* Bosses
* Events
* Trading
* Item duplication

---

## 📦 Development Roadmap

### Phase 1 — Foundation

* Mod setup
* Registries
* Configuration
* Networking
* Basic items

### Phase 2 — World

* Biomes
* Terrain
* Vegetation
* Coast
* Desert
* Dead Sea

### Phase 3 — Cities

* Tel Aviv
* Jaffa
* Jerusalem
* Villages

### Phase 4 — Culture

* Kippah
* Talit
* Tefillin
* Menorah
* Synagogues
* Food
* Hebrew signage

### Phase 5 — Gameplay

* Western Wall
* Prayer Notes
* Blessed Effect
* Trading
* Reputation
* Events

### Phase 6 — Endgame

* Rabbi's Crown
* Rare structures
* Bibi Boss
* Music Disc

### Phase 7 — Events

* Festivals
* Public Speech
* City events
* Concerts
* Markets

### Phase 8 — Polish

* Animations
* Sounds
* Particles
* UI
* Achievements
* Performance

### Phase 9 — QA

* Multiplayer
* Exploit testing
* World generation
* Compatibility
* Performance

---

## 📜 Asset & Licensing Policy

Israel-Simulator should only use:

* Original assets
* Procedurally generated assets
* Assets with compatible licenses
* Assets explicitly permitted for commercial use

External assets must be documented.

The repository should maintain:

```text
CREDITS.md
ASSET_LICENSES.md
```

Audio must follow the same licensing requirements.

In particular, commercial recordings must not be bundled without redistribution rights.

---

## 🎯 Design Principles

### Exploration First

The world should continuously provide reasons to explore.

### Discovery

Important content should not always be immediately visible.

### Variety

Different cities and regions must feel meaningfully different.

### Optionality

Cultural and religious systems should be discoverable without becoming a mandatory progression path.

### Comedy

Comedic content should primarily appear through optional events, easter eggs, NPCs, and rare encounters.

### Rare Rewards

The strongest and most unusual items should require meaningful exploration or difficult encounters.

### Replayability

Procedural generation, dynamic NPCs, events, rare structures, and random encounters should create different experiences across worlds.

---

## ✅ Definition of Done

The mod is not considered complete if it contains:

* Placeholder content
* Unresolved TODOs
* Items without textures
* NPCs without required AI
* Fake structures
* Non-functional rewards
* Missing animations where actual animations are required
* Systems that exist only in documentation
* Non-compiling code

A release candidate must satisfy:

```text
BUILD SUCCESS
+
GAME LAUNCHES
+
WORLD GENERATES
+
FEATURES FUNCTION
+
MULTIPLAYER WORKS
+
NO CRITICAL CRASHES
+
NO MAJOR DUPLICATION EXPLOITS
+
ASSETS LICENSED
+
CONTENT TESTED
```

---

## 🎮 Intended Player Experience

A typical long-term playthrough may look like:

```text
Spawn
  ↓
Village
  ↓
Mediterranean Coast
  ↓
Tel Aviv
  ↓
Jaffa
  ↓
Technology District
  ↓
Jerusalem
  ↓
Western Wall
  ↓
Prayer Note
  ↓
Blessed Effect
  ↓
Synagogues & Rare Structures
  ↓
Festivals & World Events
  ↓
Dead Sea
  ↓
Desert
  ↓
Rare Structures
  ↓
Endgame Equipment
  ↓
Rabbi's Crown
  ↓
Secret Events
  ↓
Bibi Boss
  ↓
Hava Nagila
  ↓
Public Speech
  ↓
First Amendment
  ↓
Master Explorer
  ↓
Continue Exploring
```

The player should be able to deviate from this path at almost any point.

---

## 📌 Project Vision

Israel-Simulator is intended to be more than a collection of Minecraft items.

It combines:

**Open-world exploration**

**Cultural simulation**

**Dynamic cities**

**NPC life**

**Procedural discovery**

**Events**

**Rare items**

**Boss encounters**

**Easter eggs**

**Multiplayer sandbox gameplay**

The central experience is:

```text
WORLD
 ↓
EXPLORATION
 ↓
DISCOVERY
 ↓
INTERACTION
 ↓
COLLECTION
 ↓
RARE EVENTS
 ↓
ENDGAME
 ↓
MORE EXPLORATION
```

The objective is to create a world where a player can start by simply exploring a Mediterranean coastline and, hours later, discover a rare structure, obtain an extremely uncommon item, participate in a dynamic public event, encounter an absurd boss, or uncover a secret that they had no reason to expect.

---

## 🔧 Build, CI/CD & Installation

### Build Requirements

```text
Java:       25 (toolchain provisioned automatically via Foojay resolver)
Minecraft:  26.2
Mod loader: NeoForge 26.2 (26.2.0.88)
Build:      ModDevGradle 2.0.147, Gradle wrapper 9.2.1 (committed)
```

### Build Instructions

```bash
./gradlew build
```

The mod `.jar` is produced at `build/libs/israel_simulator-<version>.jar`.

### CI/CD

GitHub Actions (`.github/workflows/build.yml`) runs on every push and pull request and:

1. Compiles the mod with the Gradle wrapper on JDK 25 (Temurin).
2. Verifies the produced `.jar` contains the expanded `neoforge.mods.toml` and the mod classes.
3. Publishes the `.jar` as a downloadable artifact named `israel-simulator-<version>-<commit>`.
4. Runs a non-blocking dedicated-server smoke test and uploads logs on failure.

A build is not considered complete if compilation, verification, or artifact publication fails.

### Installation

Install [NeoForge 26.2](https://neoforged.net/) and drop the downloaded mod `.jar` into the
instance's `mods` folder. A prebuilt `.jar` can be downloaded from any successful CI run
(GitHub → Actions → latest `build` run → Artifacts).

### Currently Implemented vs. Planned

This repository is at the **foundation milestone**: build system, mod metadata, mod
entrypoints, configuration infrastructure, and the CI/CD pipeline. Gameplay content
(items, biomes, cities, NPCs, events, bosses, ...) is **not yet implemented**; the
roadmap lives in [`TODO.md`](TODO.md) and [`PLAN.md`](PLAN.md).
