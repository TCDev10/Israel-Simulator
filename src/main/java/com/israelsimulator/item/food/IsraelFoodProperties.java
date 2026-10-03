package com.israelsimulator.item.food;

import net.minecraft.world.food.FoodProperties;

/**
 * Food properties for Israel-Simulator cuisine (GAME_DESIGN.md §17).
 *
 * <p>Values follow vanilla conventions: nutrition is hunger points, saturation modifier
 * is a multiplier (see {@link FoodProperties.Builder#saturationModifier(float)}).
 * Keep these conservative — better foods may add effects in later milestones.</p>
 */
public final class IsraelFoodProperties {
    private IsraelFoodProperties() {}

    // Ingredients / basics
    public static final FoodProperties TAHINI = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F).build();

    public static final FoodProperties DATES = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.4F).build();

    public static final FoodProperties OLIVES = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.2F).build();

    public static final FoodProperties CITRUS = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F).build();

    public static final FoodProperties CHALLAH = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6F).build();

    public static final FoodProperties RUGELACH = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.5F).build();

    // Prepared foods
    public static final FoodProperties FALAFEL = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.7F).build();

    public static final FoodProperties HUMMUS = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6F).build();

    public static final FoodProperties SHAKSHUKA = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.8F).build();

    public static final FoodProperties SABICH = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.8F).build();
}
