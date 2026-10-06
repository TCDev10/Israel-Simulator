# Biomes and World Generation

Israel-Simulator injects a realistic geographic climate gradient into Minecraft's multi-noise generation pipeline. Biomes transition organically from West to East:

```text
[Mediterranean Coast] ➔ [Urban Area] ➔ [Israeli Agriculture] ➔ [Jerusalem] ➔ [Judean Desert] ➔ [Dead Sea]
      (Sea Level)         (Plains)          (Valley)            (Hills)          (Plateau)       (Depression)
```

---

## 1. The 6 Israeli Biomes

### 🌊 Mediterranean Coast (`israel_simulator:mediterranean_coast`)
* **Geography:** Sandy coastal shores, gentle dunes, warm seawater, sea turtles and seagulls.
* **Flora:** Coastal scrub and beach palms.
* **Structures:** Ancient Jaffa Port (`jaffa_port`), Mediterranean Coastal Villages (`mediterranean_village`).
* **Locate:** `/locate biome israel_simulator:mediterranean_coast`

### 🏙️ Urban Area (`israel_simulator:urban_area`)
* **Geography:** Flat coastal plain designed for modern city life and high-tech architecture.
* **Atmosphere:** Urban soundscape (`ambient.city.tel_aviv`), street cats, cafes, and cross-quarter transitions.
* **Vegetation Protection:** Built-in worldgen rules prevent wild trees, vines, or weeds from generating on roofs, sidewalks, or building interiors.
* **Structures:** Tel Aviv Metropolis (`tel_aviv_city`), Startup Tech Office (`startup_office`), Government Assembly (`government_building`).
* **Locate:** `/locate biome israel_simulator:urban_area`

### 🌾 Israeli Agricultural Valley (`israel_simulator:israeli_agriculture`)
* **Geography:** Fertile valleys, irrigated terraces, and agricultural kibbutzim.
* **Flora:** Olive groves (`olive_tree`), citrus orchards (`citrus_orchard`), grapevine vineyards (`grapevine_patch`), and Mediterranean herbs.
* **Economy:** Farmers buy wheat, carrots, and potatoes for silver Shekels.
* **Structures:** Agricultural Kibbutz Farm (`agricultural_farm`).
* **Locate:** `/locate biome israel_simulator:israeli_agriculture`

### 🏛️ Jerusalem (`israel_simulator:jerusalem`)
* **Geography:** High rocky hills made of authentic Jerusalem Stone (`jerusalem_stone`), stone terraces, and historic pine groves.
* **Atmosphere:** Historic and spiritual ambiance (`ambient.city.jerusalem`).
* **Structures:** Old City of Jerusalem (`jerusalem_city`), Western Wall (`western_wall`), Great Synagogue (`great_synagogue`), Community Synagogue (`synagogue`), Historical Houses (`historical_house`), Grand Shuk Market (`grand_market`).
* **Locate:** `/locate biome israel_simulator:jerusalem`

### 🏜️ Judean Desert (`israel_simulator:judean_desert`)
* **Geography:** Rolling arid dunes, sandstone cliffs, wind-sculpted rock mounds, and natural ravines.
* **Hazards:** Daylight heatstroke if uncovered (wear a Kippah for shade protection).
* **Structures:** Ein Gedi Oasis (`ein_gedi_oasis`), Desert Ruins (`desert_ruins`), Ancient Desert Sanctuary (`ancient_sanctuary`).
* **Locate:** `/locate biome israel_simulator:judean_desert`

### 🧂 Dead Sea (`israel_simulator:dead_sea`)
* **Geography:** Below sea level depression, white salt beaches (`salt_block`), mineral crusts, and hyper-saline water.
* **Mechanics:** Natural buoyancy prevents sinking; therapeutic Dead Sea Mud (`dead_sea_mud`).
* **Structures:** Dead Sea Spa & Wellness Resort (`dead_sea_resort`).
* **Locate:** `/locate biome israel_simulator:dead_sea`

---

## 2. Worldgen Flora & Clean Building Placement

All custom trees, vineyards, and herbs utilize strict block predicate filters to preserve structure aesthetics:
* **No Trees on Roofs:** Olive trees and citrus trees only generate on natural soil (`grass_block`, `dirt`, `coarse_dirt`, `podzol`).
* **No Vines on Walls:** Grapevine crops only generate on farmland and natural ground.
* **Clean Buildings:** Urban and historical stone blocks (`jerusalem_stone`, `paved_road`, `western_wall_stone`, concrete, terracotta, wood) are immune to wild vegetation overgrowth.
