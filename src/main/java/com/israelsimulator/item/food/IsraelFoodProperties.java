package com.israelsimulator.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Food properties and consuming effects for Israel-Simulator cuisine (GAME_DESIGN.md §17).
 *
 * <p>Every food has defined nutrition, saturation, and optional gameplay status effects.</p>
 */
public final class IsraelFoodProperties {
    private IsraelFoodProperties() {}

    // --- Ingredients / Snacks (Light Pareve) ---

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

    // --- Prepared Foods with Gameplay Effects (§17) ---

    // Falafel: street food snack granting brief Speed
    public static final FoodProperties FALAFEL = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.7F).build();
    public static final Consumable FALAFEL_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SPEED, 200, 0), 0.4F))
            .build();

    // Hummus: hearty chickpea dip granting Resistance
    public static final FoodProperties HUMMUS = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6F).build();
    public static final Consumable HUMMUS_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0), 0.3F))
            .build();

    // Shakshuka: warm spiced skillet eggs granting Regeneration
    public static final FoodProperties SHAKSHUKA = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.8F).build();
    public static final Consumable SHAKSHUKA_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0), 0.5F))
            .build();

    // Sabich: rich pita sandwich with eggplant and tahini granting Absorption
    public static final FoodProperties SABICH = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.8F).build();
    public static final Consumable SABICH_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 0), 0.4F))
            .build();

    // Rugelach: sweet festive pastry granting Luck
    public static final FoodProperties RUGELACH = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.5F).build();
    public static final Consumable RUGELACH_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.LUCK, 600, 0), 0.3F))
            .build();

    // Matzo: unleavened bread for Passover granting Freedom
    public static final FoodProperties MATZO = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.5F).build();
    public static final Consumable MATZO_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(com.israelsimulator.effect.ModEffects.FREEDOM, 600, 0), 1.0F))
            .build();

    // Sufganiyah: holiday jelly doughnut granting Speed
    public static final FoodProperties SUFGANIYAH = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6F).build();
    public static final Consumable SUFGANIYAH_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SPEED, 400, 0), 0.7F))
            .build();

    // Hamantash: sweet filled pastry for Purim granting Luck
    public static final FoodProperties HAMANTASH = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.5F).build();
    public static final Consumable HAMANTASH_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.LUCK, 600, 0), 0.6F))
            .build();
}
