package com.israelsimulator.event.world.speech;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure (Minecraft-free) layout of the temporary Public Speech gazebo/stage, relative to the
 * event centre at plaza ground level (dy = 0 is the first block above the ground).
 * The stage is at the north side (negative z), the audience sits south of it facing north.
 */
public final class SpeechStageLayout {
    public static final int MIN_X = -4, MAX_X = 4;
    public static final int MIN_Z = -6, MAX_Z = 4;
    public static final int MIN_Y = -3, MAX_Y = 6;

    /** Where Charlie Kirk stands (on top of the stage platform), relative to the centre. */
    public static final int ORATOR_X = 0, ORATOR_Y = 1, ORATOR_Z = -4;

    public enum Kind { AIR, FLOOR, SUPPORT, STAGE, STAGE_STEP, POST, ROOF, SPEAKER, SPEAKER_TOP, MIC_STAND, MIC, CHAIR, SIGN }

    public record Placement(int dx, int dy, int dz, Kind kind) {}

    private SpeechStageLayout() {}

    /** Every position the stage may modify (the snapshot box). */
    public static List<int[]> footprint() {
        List<int[]> out = new ArrayList<>();
        for (int y = MIN_Y; y <= MAX_Y; y++)
            for (int x = MIN_X; x <= MAX_X; x++)
                for (int z = MIN_Z; z <= MAX_Z; z++)
                    out.add(new int[]{x, y, z});
        return out;
    }

    public static boolean inFootprint(int dx, int dy, int dz) {
        return dx >= MIN_X && dx <= MAX_X && dy >= MIN_Y && dy <= MAX_Y && dz >= MIN_Z && dz <= MAX_Z;
    }

    /** Placements in order: clear air, ground, stage, gazebo, props. */
    public static List<Placement> placements() {
        List<Placement> p = new ArrayList<>();
        for (int y = 0; y <= MAX_Y; y++)
            for (int x = MIN_X; x <= MAX_X; x++)
                for (int z = MIN_Z; z <= MAX_Z; z++)
                    p.add(new Placement(x, y, z, Kind.AIR));
        for (int x = MIN_X; x <= MAX_X; x++)
            for (int z = MIN_Z; z <= MAX_Z; z++) {
                p.add(new Placement(x, -3, z, Kind.SUPPORT));
                p.add(new Placement(x, -2, z, Kind.SUPPORT));
                p.add(new Placement(x, -1, z, Kind.FLOOR));
            }
        // Stage platform (one block high) with a step in front of it.
        for (int x = MIN_X; x <= MAX_X; x++)
            for (int z = MIN_Z; z <= -2; z++)
                p.add(new Placement(x, 0, z, Kind.STAGE));
        p.add(new Placement(-1, 0, -1, Kind.STAGE_STEP));
        p.add(new Placement(1, 0, -1, Kind.STAGE_STEP));
        // Gazebo: four posts and a roof over the stage.
        for (int[] c : new int[][]{{MIN_X, MIN_Z}, {MAX_X, MIN_Z}, {MIN_X, -2}, {MAX_X, -2}})
            for (int y = 1; y <= 3; y++)
                p.add(new Placement(c[0], y, c[1], Kind.POST));
        for (int x = MIN_X; x <= MAX_X; x++)
            for (int z = MIN_Z; z <= -2; z++)
                p.add(new Placement(x, 4, z, Kind.ROOF));
        // Speakers (jukebox + note block) on both sides of the stage.
        p.add(new Placement(-3, 1, -5, Kind.SPEAKER));
        p.add(new Placement(-3, 2, -5, Kind.SPEAKER_TOP));
        p.add(new Placement(3, 1, -5, Kind.SPEAKER));
        p.add(new Placement(3, 2, -5, Kind.SPEAKER_TOP));
        // Microphone on a stand in front of Charlie Kirk.
        p.add(new Placement(ORATOR_X, 1, ORATOR_Z + 1, Kind.MIC_STAND));
        p.add(new Placement(ORATOR_X, 2, ORATOR_Z + 1, Kind.MIC));
        // Two rows of chairs facing the stage, with a central aisle.
        for (int z : new int[]{1, 3})
            for (int x : new int[]{-3, -2, -1, 1, 2, 3})
                p.add(new Placement(x, 0, z, Kind.CHAIR));
        // Signs at the audience entrance.
        p.add(new Placement(MIN_X, 0, MAX_Z, Kind.SIGN));
        p.add(new Placement(MAX_X, 0, MAX_Z, Kind.SIGN));
        return p;
    }

    /** Crowd standing spots (aisle and the row between chairs), relative to the centre. */
    public static List<int[]> crowdSpots() {
        return List.of(new int[]{0, 0, 1}, new int[]{0, 0, 3}, new int[]{-2, 0, 2}, new int[]{2, 0, 2},
                new int[]{-3, 0, 0}, new int[]{3, 0, 0});
    }
}
