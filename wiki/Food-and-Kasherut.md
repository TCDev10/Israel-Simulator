# Food and Kasherut (Kosher System)

Israel-Simulator features a rich Middle Eastern culinary system with **13 crafting recipes**, street food items, and a streamlined implementation of traditional **Kosher dietary laws (Kasherut)**.

---

## 1. Culinary Crafting Recipes

All foods can be prepared at a standard Crafting Table using authentic ingredients:

| Food Item | Output Item ID | Ingredients Required | Food & Saturation |
|---|---|---|---|
| **Tahini** | `israel_simulator:tahini` | 2× Wheat Seeds + 1× Bowl | 3 Hunger, 2.4 Saturation |
| **Hummus** | `israel_simulator:hummus` | 1× Tahini + 1× Beetroot + 1× Bowl | 7 Hunger, 7.2 Saturation |
| **Falafel** | 3× `israel_simulator:falafel` | 1× Wheat + 1× Beetroot + 1× Wheat Seeds | 5 Hunger, 6.0 Saturation |
| **Shakshuka** | `israel_simulator:shakshuka` | 1× Egg + 1× Beetroot + 1× Bowl | 8 Hunger, 9.6 Saturation (Warming buff) |
| **Sabich** | `israel_simulator:sabich` | 1× Wheat + 1× Egg + 1× Tahini | 8 Hunger, 8.0 Saturation (Speed buff) |
| **Challah** | `israel_simulator:challah` | 3× Wheat + 1× Egg + 1× Sugar | 10 Hunger, 12.0 Saturation |
| **Matzo** | 4× `israel_simulator:matzo` | 2× Wheat + 1× Water Bucket (returns empty bucket) | 4 Hunger, 2.0 Saturation |
| **Sufganiyah** | `israel_simulator:sufganiyah` | 1× Wheat + 1× Sugar + 1× Sweet Berries | 6 Hunger, 6.0 Saturation (Luck buff) |
| **Hamantash** | `israel_simulator:hamantash` | 1× Wheat + 1× Sugar + 1× Dates | 6 Hunger, 5.0 Saturation |
| **Rugelach** | 2× `israel_simulator:rugelach` | 1× Wheat + 1× Cocoa Beans + 1× Sugar | 4 Hunger, 4.0 Saturation |
| **Dates** | `israel_simulator:dates` | Harvested from Date Palms | 4 Hunger, 3.6 Saturation |
| **Olives** | `israel_simulator:olives` | Harvested from Olive Leaves | 3 Hunger, 2.0 Saturation |
| **Citrus** | `israel_simulator:citrus` | Harvested from Citrus Leaves | 4 Hunger, 3.0 Saturation |

---

## 2. Kasherut (Kosher Dietary System)

The mod implements the fundamental biblical principle of separating **Meat** and **Dairy** consumption:

```text
       [Meat Food]                   [Dairy Food]                  [Pareve Food]
   (Steak, Pork, Mutton)           (Milk, Cheese, Butter)     (Hummus, Falafel, Fish, Bread)
             │                              │                              │
   Starts Meat Digestion          Starts Dairy Digestion             Neutral: Safe to eat
   Timer (3 minutes / 3600t)      Timer (1 minute / 1200t)           with any meal anytime!
```

### 🥩 Meat Digestion (`israel_simulator:meat_digestion`)
* Triggered upon eating meat items (cooked beef, mutton, porkchop, chicken).
* Lasts **3 minutes (3,600 ticks)**.
* Visual: Steak icon with circular timer ring in HUD.

### 🥛 Dairy Digestion (`israel_simulator:dairy_digestion`)
* Triggered upon drinking milk or consuming dairy foods.
* Lasts **1 minute (1,200 ticks)**.
* Visual: Milk glass icon with circular timer ring in HUD.

### ⚠️ Mixing Penalty
* If a player consumes **Dairy** while their **Meat Digestion** timer is still active (or vice versa):
  * Inflicts **Nausea I** (8 seconds) and **Slowness I** (10 seconds).
  * Chat warning: `"You feel unwell from mixing meat and dairy."`

### 🥗 Pareve Foods (Neutral)
* All vegetarian Israeli street foods (**Hummus**, **Falafel**, **Shakshuka**, **Sabich**, **Challah**, **Matzo**, fruits) are classified as **Pareve**.
* Pareve foods can be consumed freely at any time without triggering mixing penalties.

### ⚙️ Configuration
Server owners who prefer vanilla food mechanics can disable the Kasherut system anytime in `config/israel_simulator-common.toml`:
```toml
[food]
kosherSystemEnabled = false
```
