# Judaica and Sacred Items

Israel-Simulator introduces traditional cultural and sacred Judaica items, each providing distinct gameplay bonuses, 3D armor rendering, or interactive rituals.

---

## 1. Cultural Armor & Wearables

### 🧢 Kippah (`israel_simulator:kippah`)
* **Slot:** Head (`EQUIPMENT_SLOT_HEAD`).
* **Visuals:** 3D woven skullcap rendered directly on the player model with custom stitching.
* **Gameplay Functions:**
  * **Prerequisite for Prayer:** Mandatory to pray at the Western Wall.
  * **Desert Sun Protection:** Nullifies daytime heatstroke penalties in the Judean Desert.
  * **Advancement:** Unlocks the `"Shalom"` advancement when equipped.

### 🧣 Talit (`israel_simulator:talit`)
* **Slot:** Chest (`EQUIPMENT_SLOT_CHEST`).
* **Visuals:** Traditional white wool prayer shawl with tekhelet cobalt blue stripes, atarah neckband, and knotted tzitzit corner fringes.
* **Gameplay Functions:**
  * Grants **+4 Armor** points.
  * Provides passive Spiritual Resistance and Luck against negative environmental debuffs.

### 👑 Rabbi's Crown (`israel_simulator:rabbis_crown`) — *MYTHIC ITEM*
* **Slot:** Head (`EQUIPMENT_SLOT_HEAD`).
* **Visuals:** Magnificent traditional fur streimel/crown model complete with animated flowing beard and sidecurls (payot).
* **Stats:** Grants **+20 Armor** (fills the complete armor bar).
* **Mythic Passives:**
  * **Blessed Trader Status:** Grants the permanent `israel_simulator:blessed_trader` status aura, lowering all villager and merchant trading costs by **15%**.
  * **Divine Protection:** Protects against fatal damage (totem-like revival effect with internal cooldown).

---

## 2. Interactive Sacred Artifacts

### 📜 Tefillin (`israel_simulator:tefillin`)
* **Type:** Interactive Ritual Phylacteries.
* **Usage:** Right-click during daylight morning hours (Minecraft time 0–6000 ticks) while wearing a Kippah.
* **Ritual Effect:**
  * Bounds the leather straps on the player's arm and forehead.
  * Bestows the **Blessed** (`israel_simulator:blessed`) status effect (Health Regeneration, Strength, and Resistance).
  * Generates holy particles and sound effects.
  * Nighttime usage is rejected following traditional Jewish practice.

### 📝 Prayer Note / Kvitlach (`israel_simulator:prayer_note`)
* **Type:** Parchment note for petitions.
* **Usage:** Held in main hand when right-clicking the Western Wall.
* **Obtaining:** Purchased from Jerusalem scribes for 1 Shekel, or discovered in synagogue arks.

### 📜 First Amendment (`israel_simulator:first_amendment`) — *LEGENDARY ITEM*
* **Type:** Historic document of free speech and liberty.
* **Usage:** Right-click in urban districts.
* **Effect:** Clears all Slowness and Mining Fatigue, conferring the **Freedom** (`israel_simulator:freedom`) status effect (Speed II, Jump Boost II, Resistance I).
* **Advancement:** Unlocks `"Freedom of Speech"`.

### 🕎 Menorah (`israel_simulator:menorah`)
* **Type:** Ceremonial candelabrum block.
* **Mechanics:** Right-click with a flint and steel or torch up to 8 times to sequentially light all 8 candles and the central Shamash.
* **Light Level:** Light output dynamically scales from level 4 up to max level 15. Drops intact when mined.
