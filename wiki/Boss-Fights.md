# Endgame Boss: Bibi, Master of Coalitions

The mod features a satirical fantasy endgame boss encounter: **Bibi, Master of Coalitions** (`israel_simulator:bibi_boss`).

---

## 1. Boss Overview & Attributes

* **Entity ID:** `israel_simulator:bibi_boss`
* **Base Health:** **10,000 HP** (Configurable in `israel_simulator-common.toml`)
* **Base Attack Damage:** 18.0
* **Boss Bar:** Custom purple boss bar labeled *"Bibi, Master of Coalitions"*.
* **Appearance:** Formal business suit and red tie.
* **Music:** Dedicated boss battle theme (`music.boss.bibi_theme`).

---

## 2. Combat Mechanics & The 3 Phases

```text
[Phase 1: Political Speeches] ➔ [Phase 2: Coalition Guards] ➔ [Phase 3: Enraged Leader]
         (100% - 70% HP)                 (70% - 30% HP)                 (< 30% HP)
```

### 🛡️ Anti-One-Shot Protection
* The boss features damage-capping protection: no single hit can deal more than **500 damage**, preventing cheese strategies with modded super-weapons.

### Phase 1: The Speechmaker (10,000 – 7,000 HP)
* The boss engages in melee combat while periodically delivering satirical speeches (`entity.bibi_boss.speech`).
* Speech sound waves push players back and grant the boss brief resistance bursts.

### Phase 2: Coalition Reinforcements (7,000 – 3,000 HP)
* The boss summons up to **4 Coalition Guards** (`israel_simulator:bibi_guard` in dark suits) to distract and attack players.
* The guard cap is strictly enforced to prevent mob runaway lag.

### Phase 3: Enraged Coalition Master (< 3,000 HP)
* When health drops below 30%, the boss triggers **Enrage Mode** (`BIBI_ENRAGE`):
  * Movement speed doubles.
  * Attack damage increases significantly.
  * Emits dark smoke and fiery charge particles.

---

## 3. Defeat & Legendary Loot

Defeating the boss plays an epic victory fanfare (`BIBI_DEATH`) and yields:
1. **Hava Nagila Music Disc (`israel_simulator:hava_nagila_disc`):** Legendary collectible disc playable in any Jukebox.
2. **Advancement:** Unlocks the combat achievement `"Hava Nagila"`.
3. **Currency Bounty:** Large pouch of silver Shekels.
