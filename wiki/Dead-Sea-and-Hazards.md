# Dead Sea and Desert Hazards

The eastern pocket of Israel is a region of extremes, featuring the lowest point on Earth and hyper-saline waters alongside arid desert cliffs.

---

## 1. Dead Sea Mechanics (`israel_simulator:dead_sea`)

### 🌊 Natural Buoyancy (Floating Physics)
* Entering water in the Dead Sea biome triggers natural positive buoyancy:
  * The player effortlessly floats at the surface without sinking or needing to hold the Jump key.
  * Passive drowning from submersion is impossible due to the natural upward lift of the saline water.

### 🧴 Dead Sea Mud (`israel_simulator:dead_sea_mud`)
* Harvested along the mineral shoreline of the Dead Sea.
* **Usage:** Right-click to apply the therapeutic mineral mud to the player's skin.
* **Benefits:**
  * Cleanses all harmful status debuffs (Poison, Weakness, Slowness).
  * Bestows **Regeneration II** and **Absorption I** for 45 seconds.
  * Unlocks the `"Dead Sea Tourist"` advancement.

### 🧂 Salt Blocks (`israel_simulator:salt_block`)
* Pure white mineral salt formations lining the Dead Sea shoreline.
* Mined with any pickaxe, provides crisp decorative blocks and crafting salt.

---

## 2. Judean Desert Hazards (`israel_simulator:judean_desert`)

### ☀️ Heatstroke & Dehydration
* Walking under the direct desert sun at midday (time 4,000–8,000 ticks) without head covering triggers heat exhaustion:
  * Hunger bar drains rapidly.
  * May trigger temporary Fatigue or Nausea if prolonged.
* **Protection:** Equipping any headwear—especially the **Kippah** (`israel_simulator:kippah`)—or staying under shade blocks nullifies all heatstroke effects.
