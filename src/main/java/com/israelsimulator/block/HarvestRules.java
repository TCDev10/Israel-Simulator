package com.israelsimulator.block;

/**
 * Pure harvest/growth rules shared by the mod's harvestable plants (fruiting leaves and
 * Mediterranean herbs). Kept free of Minecraft types so it can be unit-tested.
 *
 * <p>Like vanilla sweet berries: right-click only harvests a mature plant, which then drops
 * back to {@link #RESET_AGE} and has to regrow through random ticks or bone meal.</p>
 */
public final class HarvestRules {
    /** Max age of fruiting leaves and herbs (matches {@code BlockStateProperties.AGE_3}). */
    public static final int MAX_AGE = 3;
    /** Age a plant falls back to after a harvest. */
    public static final int RESET_AGE = 1;
    /** One in GROWTH_CHANCE random ticks advances the age (sweet berry bush uses 5). */
    public static final int GROWTH_CHANCE = 5;

    private HarvestRules() {}

    public static boolean canHarvest(int age) {
        return age >= MAX_AGE;
    }

    /** Age after a right-click: reset if mature, unchanged (no harvest) otherwise. */
    public static int ageAfterHarvest(int age) {
        return canHarvest(age) ? RESET_AGE : age;
    }

    public static boolean canGrow(int age) {
        return age < MAX_AGE;
    }

    /** Age after one successful growth step (random tick or bone meal). */
    public static int ageAfterGrowth(int age) {
        return Math.min(MAX_AGE, age + 1);
    }
}
