# Economy and Trading

Israel-Simulator introduces a regional, bi-metallic currency system alongside authentic Middle Eastern market mechanics.

---

## 1. Currency System

* **🪙 Shekel (`israel_simulator:shekel`):** The primary silver currency minted for trade, transport, and purchases.
* **🪙 Agora (`israel_simulator:agora`):** Fractional bronze coinage.
* **Exchange Rate:**  
  `1 Shekel = 100 Agorot`

---

## 2. Regional Market Trading

### 🚜 Agricultural Valley (Kibbutz Trading)
* When in the `israel_simulator:israeli_agriculture` biome, right-clicking farmer villagers with wheat, carrots, or potatoes conducts direct bartering:
  * Farmers buy 20 harvested crops in exchange for Shekels and Agorot.
  * Outside the agricultural biome, vanilla villager GUI trading functions normally.

### 🏛️ Jerusalem Sacred Scribes & Artisans
* Right-clicking citizens in the `israel_simulator:jerusalem` biome with Shekels:
  * **1 Shekel:** Purchases 1× `prayer_note` for the Western Wall.
  * **4 Shekels:** Purchases 1× `kippah`.
  * **12 Shekels (while crouching with Shift):** Purchases 1× `tefillin`.

### 🛍️ Shuk Markets (Carmel, Sarona & Machane Yehuda)
* Food vendors sell prepared street meals (Falafel, Hummus bowls, Shakshuka, Sabich) for Shekels.
* High-tech vendors in Tel Aviv sell tech hardware (Laptops, Smartphones, Drone parts).

---

## 3. Blessed Trader Aura & Reputation Discounts

* Equipping the **Rabbi's Crown** (`rabbis_crown`) or having the **Blessed** effect grants the `israel_simulator:blessed_trader` status.
* All merchant buy costs are immediately slashed by **15%**.
* Unlocks the `"Blessed Trader"` advancement.
* **Anti-Arbitrage Protection:** Server economy rules ensure `Purchase Price > Sell Price` for all goods, preventing infinite currency duplication loops.
