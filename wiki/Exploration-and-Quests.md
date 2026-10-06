# Exploration, Quests and Factions

Israel-Simulator rewards geographic exploration, site discovery, and regional reputation.

---

## 1. The Interactive Israel Map (`israel_simulator:israel_map`)

* Holding the Israel Map in hand or off-hand tracks the player's spatial coordinates across the country.
* **Landmark Detection:** When entering within **48 blocks** of a major historical landmark (Kotel, Jaffa Clock Tower, Ein Gedi, Masada, Azrieli, etc.):
  * Plays a triumphant discovery audio fanfare (`ui.landmark_discovered`).
  * Displays a golden chat toast: `★ Landmark Discovered: <Name> (<Region>)`.
  * Grants **+100 Experience Points**.
  * Unlocks the landmark on the map's discovery log (e.g., `4/8 Landmarks Discovered`).

---

## 2. The 5 Regional Factions & Reputation

Player actions modify standing with five distinct factions:

| Faction | Sphere of Influence | Actions that Increase Standing | Benefits |
|---|---|---|---|
| **Religious** | Jerusalem & Synagogues | Praying at the Kotel, lighting Menorahs, wearing Kippah. | Clergy blessings, access to sacred scrolls and relic caches. |
| **Merchant** | Shuk Markets & Ports | Trading goods at Carmel & Machane Yehuda, spending Shekels. | Deeper commercial discounts, rare trade offers. |
| **Tech District** | Tel Aviv & Sarona Hubs | Completing startup tasks, crafting laptops & smartphones. | Advanced tech components, automated transit perks. |
| **Village / Kibbutz** | Agricultural Valleys | Selling crops, harvesting groves, cultivating grapevines. | Bulk agricultural bartering, farming equipment. |
| **Desert Nomads** | Judean Desert & Dead Sea | Rescuing oasis travelers, extracting Dead Sea mud. | Desert survival guidance, ancient coin barters. |

---

## 3. Advancements Guide (All 13 Advancements)

* **`cultural/shalom`:** Wear a Kippah on your head for the first time.
* **`cultural/freedom_of_speech`:** Exercise free expression with the First Amendment in Tel Aviv.
* **`economy/five_diamonds`:** Complete a prayer at the Kotel and receive 5 diamonds.
* **`economy/blessed_trader`:** Trade with merchants while radiating the Blessed Trader discount aura.
* **`exploration/welcome_to_israel`:** Take your first step into any Israeli biome.
* **`exploration/visit_jerusalem`:** Arrive in the holy city of Jerusalem.
* **`exploration/jaffa`:** Explore the docks of the Ancient Jaffa Port.
* **`exploration/tel_aviv_nights`:** Experience the vibrant nightlife of Tel Aviv after dark.
* **`exploration/dead_sea_tourist`:** Float on the Dead Sea and apply therapeutic mud.
* **`exploration/master_explorer`:** Uncover all landmarks across the map of Israel.
* **`combat/hava_nagila`:** Defeat the Bibi Boss and obtain the legendary folk music disc.
* **`technology/startup_founder`:** Program a laptop or smartphone in the Startup District.
* **`secret/hummus_connoisseur`:** Taste all authentic street foods (Hummus, Falafel, Shakshuka, Sabich).
* **`secret/secret_kippah_cat`:** Place a Kippah on a pet cat.
