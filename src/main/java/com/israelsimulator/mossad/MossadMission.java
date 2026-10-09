package com.israelsimulator.mossad;

/**
 * Missions offered by the (fictional) Mossad Handler NPC. Game-only content; no real people or events.
 */
public enum MossadMission {
    /** Eliminate a cell of hostile agents spawned 30-45 blocks away. */
    ELIMINATE("eliminate", 3, 10),
    /** Bring back a Sealed Dossier found in Judean desert ruins chests. */
    RETRIEVE("retrieve", 1, 15),
    /** Escort an informant to a drop point 120-180 blocks away; two agents lie in ambush there. */
    ESCORT("escort", 1, 20);

    private final String id;
    private final int goal;
    private final int reputation;

    MossadMission(String id, int goal, int reputation) {
        this.id = id;
        this.goal = goal;
        this.reputation = reputation;
    }

    public String id() {
        return id;
    }

    /** Kills (ELIMINATE) or items/arrivals needed. */
    public int goal() {
        return goal;
    }

    /** Reputation gained on completion. */
    public int reputationReward() {
        return reputation;
    }

    /** Shekels paid on completion. */
    public int shekelReward() {
        return switch (this) {
            case ELIMINATE -> 16;
            case RETRIEVE -> 24;
            case ESCORT -> 32;
        };
    }

    public String translationKey() {
        return switch (this) {
            case ELIMINATE -> "mossad.israel_simulator.mission.eliminate";
            case RETRIEVE -> "mossad.israel_simulator.mission.retrieve";
            case ESCORT -> "mossad.israel_simulator.mission.escort";
        };
    }

    public static MossadMission byId(String id) {
        for (MossadMission m : values()) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }
}
