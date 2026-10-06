# Western Wall (Kotel) and Prayer Mechanics

The Western Wall (`western_wall`) in Jerusalem is the spiritual center of the mod. It features custom 3D animations, interactive prayer sessions, rewards, and server-authoritative anti-exploit persistence.

---

## 1. Prerequisites for Prayer

To pray at the Western Wall, a player must fulfill two conditions:
1. **Wear a Kippah:** The player must have `israel_simulator:kippah` equipped in their helmet slot. Praying bareheaded triggers the warning:  
   `"You must wear a Kippah to pray at the Western Wall"`.
2. **Hold a Prayer Note:** The player must hold at least one `israel_simulator:prayer_note` in their main hand.

---

## 2. Prayer Session Lifecycle

```text
[Right-Click Kotel] ➔ [3-Second Session (60 ticks)] ➔ [Rewards Granted & Cooldown Set]
  (Checks Kippah &       (3D Bowing Pose, Head Bowed       (5 Diamonds, Happy Particles,
   Prayer Note)           Must Stand Still)                 24,000 tick cooldown)
```

1. **Initiation:** Right-click on any `israel_simulator:western_wall_stone` block meeting prerequisites.
2. **Animation:**
   * In 3rd-person perspective (F5), the player's head bows forward at 35°, and arms extend toward the wall.
   * In 1st-person perspective, camera gently locks orientation toward the ancient stone.
3. **Movement Check:**
   * If the player jumps, walks away, or takes damage during the 3 seconds, the prayer is immediately cancelled.
   * No items are consumed and no rewards are given if interrupted.
4. **Completion:**
   * Consumes 1x `prayer_note`.
   * Grants **5 Diamonds** (`minecraft:diamond`).
   * Spawns radiant `VILLAGER_HAPPY` particles and plays `ENTITY_PLAYER_LEVELUP`.
   * Unlocks the `"Five Diamonds"` advancement.

---

## 3. Server-Authoritative Cooldown & Anti-Exploit

* **Cooldown Duration:** 24,000 game ticks (exactly 1 in-game Minecraft day).
* **Server Persistence:** The cooldown is stored in the world's saved data (`western_wall_prayers.dat`).
  * Restarting the dedicated server (`/stop` and relaunch) **does not reset** the timer.
  * Reconnecting, dying, or changing dimensions **does not bypass** the cooldown.
  * Clicking again while on cooldown displays the remaining wait time in seconds/minutes.
