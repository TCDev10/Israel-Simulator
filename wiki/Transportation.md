# Transportation Network

Navigating between Israeli cities and across varied terrain is streamlined through specialized vehicles, infrastructure, and public transit.

---

## 1. Paved Roads (`israel_simulator:paved_road`)

* Modern asphalt paving stone found connecting urban districts and highway corridors.
* **Speed Boost:** Walking or running on `paved_road` grants **+40% movement speed** (Speed I) for 3 seconds.
* **Mining:** Mined with a pickaxe, drops itself intact.

---

## 2. Rideable Bicycle (`israel_simulator:bicycle`)

* **Usage:** Place the bicycle item on the ground and right-click to mount.
* **Controls:** Standard WASD navigation.
* **Bell Feature:** Press `Space` while riding to ring the bicycle bell (`entity.bicycle.bell`).
* **Dismounting:** Press `Shift` to dismount. Punching the bike converts it back into an inventory item.

---

## 3. Public Transit & Rav-Kav Pass

### 💳 Rav-Kav Transit Card (`israel_simulator:rav_kav`)
* Multi-use transit pass for buses, trains, and rapid transit hubs.
* Can also be substituted with **10 Shekels** if a player has not yet obtained a card.

### 🚏 Transport Stop (`israel_simulator:transport_stop`)
* Interactive station pillar block located across major destinations.
* **Routing Network:** Stations form a circular travel loop connecting all key regions:

```text
[Tel Aviv Central] ➔ [Jaffa Clock Tower] ➔ [Jaffa Port] ➔ [Jerusalem Navon] ➔ [Dead Sea Ein Gedi] ➔ [Galilee Hub] ➔ (Loops back to Tel Aviv)
```

* **Dynamic Spatial Routing:** Right-clicking a stop detects the nearest local hub and transports the player sequentially to the next stop in the network.
* **Sound & FX:** Plays transit travel audio (`TRANSIT_TRAVEL`).
* **Transit Cooldown:** Enforces a 3-second server cooldown between rides to prevent spam.
* **Mining:** Mined with a pickaxe, drops itself intact.

---

## 4. Walking Shoes (`israel_simulator:walking_shoes`)

* Sturdy hiking boots that increase sprint speed on rough off-road terrain (mountain trails, desert dunes) and reduce hunger depletion.
