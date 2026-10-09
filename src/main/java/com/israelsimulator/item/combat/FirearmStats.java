package com.israelsimulator.item.combat;

import java.util.List;

/**
 * Ballistic values for the mod's firearms, balanced against vanilla weapons.
 *
 * <p>Reference points: a netherite sword deals 8 damage at 1.6 attacks/s (12.8 DPS), a fully drawn
 * bow 6-10, a crossbow bolt 6-11. Guns trade higher burst for ammo cost, reload pauses, spread and
 * durability. Damage is per hit; fire interval is in ticks (20 ticks = 1 s); spread is the half-angle
 * of the random cone in degrees (scoped spread applies only to the sniper while aiming).</p>
 */
public record FirearmStats(
        String id,
        float damage,
        int fireIntervalTicks,
        float spreadDegrees,
        float scopedSpreadDegrees,
        double range,
        int magazineSize,
        int reloadTicks,
        int durability,
        String ammoId) {

    /** Tavor-style bullpup assault rifle: automatic while the use key is held. */
    public static final FirearmStats ASSAULT_RIFLE =
            new FirearmStats("assault_rifle", 4.0F, 4, 2.0F, 2.0F, 64.0, 30, 50, 900, "rifle_ammo");
    /** Uzi-style submachine gun: fast, inaccurate, short range. */
    public static final FirearmStats SMG =
            new FirearmStats("smg", 2.5F, 4, 6.0F, 6.0F, 24.0, 32, 40, 700, "smg_ammo");
    /** Bolt-action sniper rifle: hold use to aim through the scope, release to fire. */
    public static final FirearmStats SNIPER_RIFLE =
            new FirearmStats("sniper_rifle", 20.0F, 30, 5.0F, 0.0F, 160.0, 5, 70, 400, "sniper_ammo");

    public static final List<FirearmStats> ALL = List.of(ASSAULT_RIFLE, SMG, SNIPER_RIFLE);

    /** Ticks the sniper must be held aimed before the scoped spread applies. */
    public static final int SCOPE_STEADY_TICKS = 10;

    /** Sustained damage per second while the magazine lasts. */
    public double burstDps() {
        return damage * 20.0 / fireIntervalTicks;
    }

    /** Damage per second including the reload pause of each magazine. */
    public double sustainedDps() {
        double magazineTicks = magazineSize * (double) fireIntervalTicks + reloadTicks;
        return damage * magazineSize * 20.0 / magazineTicks;
    }

    public int shotsPerMinute() {
        return 1200 / fireIntervalTicks;
    }

    /** Rounds loaded by a reload: what is missing from the magazine, limited by the available ammo. */
    public static int roundsToLoad(int loaded, int capacity, int available) {
        if (loaded >= capacity || available <= 0) {
            return 0;
        }
        return Math.min(capacity - Math.max(0, loaded), available);
    }
}
