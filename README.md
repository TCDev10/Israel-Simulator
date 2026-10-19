# Israel-Simulator

<p align="center">
  <img src="israel_simulator.png" alt="Israel-Simulator Logo" width="160" height="160" />
</p>

<p align="center">
  <strong>Israel in Minecraft: Jerusalem, Tel Aviv, the desert, the Dead Sea, falafel, Shekels and a satirical boss fight.</strong>
</p>

<p align="center">
  <a href="https://neoforged.net/"><img src="https://img.shields.io/badge/NeoForge-26.2.0.88-ea580c?style=for-the-badge" alt="NeoForge 26.2" /></a>
  <a href="https://minecraft.net/"><img src="https://img.shields.io/badge/Minecraft-26.2-248046?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft 26.2" /></a>
  <a href="https://adoptium.net/"><img src="https://img.shields.io/badge/Java-25-b07219?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-AGPL--3.0-blue?style=for-the-badge" alt="AGPL-3.0" /></a>
  <a href="https://github.com/TCDev10/Israel-Simulator/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/TCDev10/Israel-Simulator/build.yml?branch=main&label=CI%20Build&style=for-the-badge" alt="CI Build" /></a>
  <a href="https://github.com/TCDev10/Israel-Simulator/releases"><img src="https://img.shields.io/github/v/release/TCDev10/Israel-Simulator?logo=github&label=Release&style=for-the-badge" alt="Latest Release" /></a>
</p>

---

Full docs are in the [wiki](wiki/Home.md) (in Italian).

## Contents

- [What is this](#what-is-this)
- [Features](#features)
  - [Biomes](#biomes)
  - [Structures](#structures)
  - [Tel Aviv districts](#tel-aviv-districts)
  - [Religion and holidays](#religion-and-holidays)
  - [Money, food and the train](#money-food-and-the-train)
  - [Bosses and other stuff](#bosses-and-other-stuff)
- [Installing](#installing)
- [Building from source](#building-from-source)
- [Project layout](#project-layout)
- [Contributing](#contributing)
- [Translations](#translations)
- [License and credits](#license-and-credits)

---

## What is this

Israel-Simulator is a NeoForge mod for Minecraft 26.2 (Java 25). It adds Israeli biomes and places to normal world generation, so you just start a new world and find them while exploring. No adventure map, no scripted tour.

You can walk around the Old City of Jerusalem, pray at the Western Wall with a Kippah, light a Menorah, eat falafel and sabich, pay in Shekels and take the train from Tel Aviv to Jerusalem. There's also a sillier side: Mossad missions, guns, a plane crash event and a satirical boss fight against Bibi.

Everything that matters (prayers, trades, rewards, bosses) runs on the server, so it works fine in multiplayer.

---

## Features

### Biomes

Six biomes, laid out roughly west to east like the real country:

- **Mediterranean Coast**: beaches, palms, warm water.
- **Urban Area**: Tel Aviv. Roads, squares, buildings.
- **Israeli Agriculture**: fields, olive groves and vineyards with a grapevine crop.
- **Jerusalem**: limestone hills and Jerusalem Stone.
- **Judean Desert**: dry valleys, canyons and the odd oasis. In daytime the sun makes you hungry fast (and slow once you're low on food) unless you wear something on your head.
- **Dead Sea**: a deep salty basin with salt blocks and mud, between Jerusalem and the desert.

### Structures

There are 19 structures. Each one spawns in a single biome, has furnished interiors and its own chest loot. Most are jigsaw builds that follow the terrain (streets go up and down with the ground, buildings sit on buried footings). The rest are single hand-built pieces, some with a few variants.

| Structure | Biome | Size / layout | What's inside |
| :--- | :--- | :--- | :--- |
| `western_wall` | Jerusalem | 64×26×48 | Herodian courses of large pale ashlars, paved prayer plaza with mechitza, Wilson's Arch prayer hall with the treasury, covered Mughrabi ramp |
| `jerusalem_city` | Jerusalem | Jigsaw city | Old City plaza, stone streets, shuk stalls, well, wall gates, synagogue, six house styles (courtyard, domed, Nachlaot, Templer, Rehavia, arched) |
| `grand_market` | Jerusalem | Jigsaw market | Mahane Yehuda-style gate plaza, covered and open lanes, spice/fruit/bakery/fish/halva shops, cafe, covered market hall |
| `great_synagogue` | Jerusalem | 36×31×36 | Stained glass hall, central bimah, Torah Ark; copper dome, twin towers, pedimented portico |
| `synagogue` | Jerusalem | 26×24×26 | Neighbourhood house of prayer and study; dome on a windowed drum, turrets, portico, rose window |
| `historical_house` | Jerusalem | 24×14×24 | Heritage limestone home with olive courtyard, dome, green shutters, external roof stair |
| `tel_aviv_city` | Urban Area | Jigsaw city | Dizengoff square, Bauhaus blocks, Florentin lofts, Sarona houses, high-rises, Rothschild kiosks, Tayelet |
| `government_building` | Urban Area | 2 variants (41×22×47, 33×32×39) | Knesset with plenary hall, or ministry tower with helipad |
| `startup_office` | Urban Area | 26×17×26 | Modern tech hub with workstations and server racks; lit canopy sign, rooftop terrace |
| `jaffa_port` | Mediterranean Coast | Jigsaw town | Ottoman clock tower square, harbour with boats, pier and lighthouse, stone alleys and stairs, flea market |
| `mediterranean_village` | Mediterranean Coast | Jigsaw village | Square with well, whitewashed houses, chapel, bakery, fisherman's hut, olive grove, market stall |
| `gaza_strip` | Mediterranean Coast | Jigsaw zone, 110 to 160 blocks a side | Rubble streets, checkpoints, ruins, watchtowers, breached walls and compounds, randomised every time |
| `agricultural_farm` | Israeli Agriculture | Jigsaw moshav | Fields, irrigation, greenhouses, date grove, farmyard and sheds |
| `ancient_sanctuary` | Judean Desert | 2 variants (31×18×43) | Hidden Tabernacle-plan temple, also half-buried, with a crypt |
| `desert_ruins` | Judean Desert | Jigsaw ruins | Qumran-style courtyard and cistern, temple ruin, scriptorium with scroll jars, pottery workshop, watchtower, mikveh |
| `ein_gedi_oasis` | Judean Desert | Jigsaw oasis | Waterfall and pools, trails, ancient mosaic synagogue, hidden cave, balsam terraces, lookout |
| `dead_sea_resort` | Dead Sea | Jigsaw resort | Mud-spa plaza, hotel tower, promenade/boardwalk, mud spa, pool club, beach bar, salt shop, salt-formation beach |
| `tropical_island` | Tropical Island (rare warm deep ocean) | 240×72×170 | A whole island with its temple |
| `twin_towers` | Plains (vanilla) | 74×200×71 | The Twin Towers |

Use `/locate structure israel_simulator:<name>` to find one.

### Tel Aviv districts

Tel Aviv is split into 6 districts. When you walk into one you get a banner on the action bar, and each has its own trade prices:

- Startup District: tech items, better laptop deals.
- Rothschild Boulevard: banking and cafes.
- Florentin: artisans and market bargains.
- Sarona: cheaper food.
- Tayelet (the beach promenade): fish and beach stuff.
- White City: Bauhaus buildings, culture bonuses.

### Religion and holidays

- **Western Wall**: wear a Kippah and right-click the Wall with a Prayer Note. You get Blessed (Luck II and Regeneration I) and 5 diamonds. Once per in-game day.
- **Talit and Tefillin**: the Talit gives Resistance and Luck while worn. Put on Tefillin during the day, with a Kippah, for Blessed and Resistance (plus Strength if you also wear the Talit).
- **Torah Ark**: pray at the Aron Kodesh in synagogues. Leave an offering for a bigger blessing.
- **Menorah**: a block you light one candle at a time.
- **Kosher rules**: after eating meat you have to wait a bit before dairy, and the other way round. Mix them and you feel sick. Can be turned off in the config.
- **Holidays**: the world has a 120-day year with Rosh Hashanah, Yom Kippur, Sukkot, Hanukkah, Purim and Pesach, plus Shabbat every in-game week. You get a chat message when one starts and ends, and the Shuk vendors change what they sell. On Shabbat and Yom Kippur the Shuk is closed. On Shabbat, if you don't break or place blocks or fight for a minute, you get a small rest bonus.
- **Festival items**: Shofar, Dreidel (bet 3 Shekels per spin), Matzo, Sufganiyah, Hamantash.

### Money, food and the train

- **Money**: Shekels and Agorot (100 Agorot = 1 Shekel).
- **Startup work**: use the laptop for work sessions that pay Shekels (double in Tel Aviv or a startup office). Every 10 sessions your startup gets acquired and you get a bonus. The smartphone does small delivery gigs in the cities. No work on Shabbat.
- **Train**: transit stops in Tel Aviv or Jerusalem (in the generated city or its biome) are stations on the Israel Railways line. Right-click one to go to the nearest station at the other end, or to the nearest generated city there if nobody has built a station yet. It costs 10 Shekels, or nothing with a Rav-Kav card.
- **Food**, all with their own textures:
  - dishes: Falafel, Shakshuka, Sabich, Hummus, Challah, Rugelach, Tahini
  - holiday food: Matzo, Sufganiyah, Hamantash
  - produce: Olives, Dates, Citrus, Grapes (from the grapevine crop), Chickpeas, Tomato, Eggplant, Sesame Seeds

### Bosses and other stuff

- **Bibi**: a satirical boss you summon with the Star of David. At half health Donald Trump shows up and shields him until you kill Trump, and below a third Jeffrey Epstein joins in. Bibi drops the Rabbi's Crown (the only way to get it), the Hava Nagila music disc, Shekels, Ancient Coins and diamonds.
- **Rabbi's Crown**: +20 armor and Blessed Trader (cheaper trades) while you wear it.
- **First Amendment**: you get it from the public speech event. Right-click for Freedom, which clears Slowness and Mining Fatigue.
- **Mossad**: talk to a Handler in a city safehouse for missions (take out a cell, recover a dossier, escort an informant).
- **Weapons**: pistol, bullpup assault rifle, Uzi, sniper rifle, frag grenades and the Palantrio Drone.
- **More**: other bosses and minibosses, a plane crash event and local animals (gazelle, ibex, hyrax). See the [wiki](wiki/Home.md).

---

## Installing

### Client

1. You need **Minecraft Java Edition 26.2**.
2. Install **[NeoForge 26.2.0.88 or newer](https://neoforged.net/)**.
3. Grab the latest `israel_simulator-x.y.z.jar` from [Releases](https://github.com/TCDev10/Israel-Simulator/releases).
4. Drop the jar into your `mods` folder:
   - **Windows**: `%appdata%\.minecraft\mods`
   - **Linux**: `~/.minecraft/mods`
   - **macOS**: `~/Library/Application Support/minecraft/mods`
5. Start the game with the NeoForge profile.

### Dedicated server

1. Set up a normal NeoForge 26.2 server.
2. Make sure it runs on **Java 25** (`java -version`).
3. Put `israel_simulator-x.y.z.jar` in the server's `mods/` folder.
4. Start it with `run.bat` or `run.sh`. The new biomes and structures show up in chunks that haven't been generated yet.

### Multiplayer

Install the mod on the server and on every client.

### Config

Settings are in `config/israel_simulator-common.toml`, created on first launch: Blessed duration and level, kosher rules and wait times, Bibi's health, damage, guards and arena size, NPC density, max trade discount, and how long world events last and how often they come.

### CI builds

Every push to `main` gets built by GitHub Actions. The jar is attached to the run as the artifact `israel-simulator-<version>-<sha>` (Actions → run → Artifacts).

### Known issues

- Only NeoForge 26.2 / Minecraft 26.2 on Java 25.
- Some structures can still overlap their neighbours. Spacing is being tuned.
- NPC daily routines exist in the code but no NPC follows them yet.
- The train is a teleport between stations with a short fade. There's no actual train on rails.

---

## Building from source

### You need

- JDK 25 or newer (for example Eclipse Temurin 25)
- Git

### Build the jar

```bash
git clone https://github.com/TCDev10/Israel-Simulator.git
cd Israel-Simulator

# compile, run the tests and package the jar
./gradlew build
```

The jar ends up in:
```text
build/libs/israel_simulator-0.1.0.jar
```

### Run a dev client or server

```bash
# client with the mod loaded
./gradlew runClient

# headless dedicated server
./gradlew runServer

# GameTest server
./gradlew runGameTestServer
```

### Tests

The JUnit tests cover worldgen data, registries, the economy and data assets:

```bash
./gradlew test
```

The report is in `build/reports/tests/test/index.html`.

For testing in game (checklist, scenarios, handy commands) see [TESTING_GUIDE.md](TESTING_GUIDE.md).

---

## Project layout

Gameplay logic lives on the server side. Rough map of the code:

```text
src/main/java/com/israelsimulator/
├── IsraelSimulator.java   # Mod entrypoint & event bus registrations
├── agriculture/ desert/ deadsea/        # Regional mechanics (crops, desert, Dead Sea buoyancy)
├── block/ item/ effect/ entity/         # Content: blocks, items, effects, mobs, bosses, projectiles
├── city/ economy/ trading/ reputation/  # Cities and Tel Aviv districts, Shekel economy, trades
├── religion/ synagogue/ westernwall/ festival/  # Prayer, Torah Ark, festival items and calendar
├── transport/ npc/ mossad/ quest/ easteregg/
├── client/                              # Client-only renderers, 3D wearables, music
├── event/ network/ config/ registry/ datagen/ mixin/ performance/ core/ audio/
└── world/                               # Biomes, climate, structures, worldgen guards
scripts/worldgen/                        # Python generators for the structure NBT templates
```

---

## Contributing

PRs are welcome: new jigsaw pieces, bug fixes, translations, whatever.

1. Fork the repo and make a branch:
   ```bash
   git checkout -b feature/my-thing
   ```
2. A few rules:
   - Gameplay runs on the server. Don't trust anything the client sends about progress.
   - Use data-driven stuff (JSON structures, loot tables, tags) where it makes sense.
   - Only use original assets or assets with an AGPL-3.0 compatible license.
3. Before you open the PR, `./gradlew test` and `./gradlew build` should both pass.
4. In the PR, say what you changed and how you tested it.

---

## Translations

The mod is in English (`en_us`) and Italian (`it_it`), and both files have the same keys.

Want to add Hebrew (`he_il`), Spanish (`es_es`) or something else? Copy `src/main/resources/assets/israel_simulator/lang/en_us.json` to your language code, translate it and open a PR.

---

## License and credits

- **Code and assets**: [GNU Affero General Public License v3.0](LICENSE) (AGPL-3.0).
- **Third-party credits**: [CREDITS.md](CREDITS.md).
- **Asset licenses**: [ASSET_LICENSES.md](ASSET_LICENSES.md) for textures and audio.
