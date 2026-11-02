package com.israelsimulator.advancement;

/**
 * Names of the {@code israel_simulator:mod_event} criteria. Every advancement that uses the custom
 * trigger must name one of these (checked by AchievementsAndProgressionTest).
 */
public final class AdvancementEvents {
    public static final String WESTERN_WALL_PRAYER = "western_wall_prayer";
    public static final String ARK_PRAYER = "ark_prayer";
    public static final String TEFILLIN_BOUND = "tefillin_bound";
    public static final String SHOFAR_BLOW = "shofar_blow";
    public static final String KIPPAH_CAT = "kippah_cat";
    public static final String DEAD_SEA_FLOAT = "dead_sea_float";
    public static final String MUD_BATH = "mud_bath";
    public static final String SHABBAT_REST = "shabbat_rest";
    public static final String SHABBAT_WORK_BLOCKED = "shabbat_work_blocked";
    public static final String WORK_SESSION = "work_session";
    public static final String STARTUP_EXIT = "startup_exit";
    public static final String DELIVERY_GIG = "delivery_gig";
    public static final String DRONE_LAUNCH = "drone_launch";
    public static final String TRAIN_RIDE = "train_ride";
    public static final String TRAIN_RIDE_RAV_KAV = "train_ride_rav_kav";
    public static final String MOSSAD_MISSION = "mossad_mission";
    public static final String MOSSAD_VETERAN = "mossad_veteran";
    public static final String BIBI_SUMMON = "bibi_summon";
    public static final String PLANE_CRASH = "plane_crash";
    /** Prefix of the per-festival events: {@code festival_shabbat}, {@code festival_hanukkah}, ... */
    public static final String FESTIVAL_PREFIX = "festival_";

    /** Missions a player must complete for {@link #MOSSAD_VETERAN}. */
    public static final int MOSSAD_VETERAN_MISSIONS = 5;

    private AdvancementEvents() {}

    public static String festival(String festivalId) {
        return FESTIVAL_PREFIX + festivalId;
    }
}
