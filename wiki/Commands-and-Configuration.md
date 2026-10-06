# Commands and Configuration

Israel-Simulator provides full server customization options through NeoForge's configuration framework and standard in-game commands.

---

## 1. Configuration File: `config/israel_simulator-common.toml`

The configuration file is automatically created in your server or client's `config/` directory.

### `[effects]`
```toml
# Duration of the Blessed effect in ticks (default 1200 = 60s)
blessedDurationTicks = 1200
# Amplifier of the Blessed effect (0 = Level I)
blessedAmplifier = 0
```

### `[food]`
```toml
# Enable/disable the Kosher system (meat/dairy digestion separation and mixing penalties)
kosherSystemEnabled = true
# Waiting period in ticks after eating meat before dairy is allowed (default 3600 = 3 minutes)
meatDigestionTicks = 3600
# Waiting period in ticks after eating dairy before meat is allowed (default 1200 = 1 minute)
dairyDigestionTicks = 1200
```

### `[boss]`
```toml
# Maximum health of the Bibi Boss (default 10,000 HP)
bibiBossMaxHealth = 10000.0
# Base attack damage of the Bibi Boss
bibiBossBaseDamage = 18.0
# Maximum simultaneous Coalition Guards summoned
bibiBossMaxGuards = 4
# Boss arena leashing radius in blocks
bibiBossArenaRadius = 48
```

### `[worldgen]`
```toml
# Average chunk spacing between major cities (Tel Aviv, Jerusalem)
citySpacingChunks = 34
# Average chunk spacing between rare monuments (Western Wall, Ancient Sanctuary)
rareStructureSpacingChunks = 48
```

### `[cooldowns]`
```toml
# Western Wall prayer cooldown in ticks (default 24000 = 1 in-game day)
prayerCooldownTicks = 24000
# Shofar horn cooldown in ticks (default 600 = 30s)
shofarCooldownTicks = 600
# Transport stop boarding cooldown in ticks (default 60 = 3s)
transitCooldownTicks = 60
```

### `[performance]`
```toml
# Maximum NPCs allowed per chunk to prevent mob runaway lag
maxNpcPerChunk = 20
# Maximum particles spawned per world event
maxParticlesPerEvent = 200
# Enable automatic entity throttling under low TPS
enablePerformanceThrottling = true
```

---

## 2. In-Game Admin & Testing Commands

### Teleport to Biomes
```text
/locate biome israel_simulator:mediterranean_coast
/locate biome israel_simulator:urban_area
/locate biome israel_simulator:israeli_agriculture
/locate biome israel_simulator:jerusalem
/locate biome israel_simulator:judean_desert
/locate biome israel_simulator:dead_sea
```

### Teleport to Structures
```text
/locate structure israel_simulator:western_wall
/locate structure israel_simulator:jerusalem_city
/locate structure israel_simulator:great_synagogue
/locate structure israel_simulator:tel_aviv_city
/locate structure israel_simulator:jaffa_port
/locate structure israel_simulator:agricultural_farm
/locate structure israel_simulator:dead_sea_resort
/locate structure israel_simulator:ein_gedi_oasis
/locate structure israel_simulator:ancient_sanctuary
```

### Summon Entities & Bosses
```text
/summon israel_simulator:bibi_boss
/summon israel_simulator:bibi_guard
/summon israel_simulator:bicycle
```

### Apply Mob Effects
```text
/effect give @p israel_simulator:blessed 30 0
/effect give @p israel_simulator:blessed_trader 30 0
/effect give @p israel_simulator:freedom 30 0
/effect give @p israel_simulator:meat_digestion 30 0
/effect give @p israel_simulator:dairy_digestion 30 0
```
