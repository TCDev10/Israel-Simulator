package com.israelsimulator.event;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.effect.ModEffects;
import com.israelsimulator.item.ModItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * Server-authoritative event handling for the simplified kosher dietary system
 * (GAME_DESIGN.md §18).
 *
 * <p>Enforces waiting periods between consuming meat and dairy when enabled.
 * Pareve foods are neutral and do not trigger waiting periods or penalties.</p>
 */
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID)
public final class KosherEvents {
    private KosherEvents() {}

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !IsraelSimulatorConfig.kosherSystemEnabled()) {
            return;
        }

        ItemStack stack = event.getItem();
        boolean isMeat = stack.is(ModItemTags.MEAT);
        boolean isDairy = stack.is(ModItemTags.DAIRY);

        if (isMeat) {
            if (entity.hasEffect(ModEffects.DAIRY_DIGESTION)) {
                // Violation: meat consumed while dairy waiting period is active
                applyMixingPenalty(entity);
                entity.removeEffect(ModEffects.DAIRY_DIGESTION);
            } else {
                entity.addEffect(new MobEffectInstance(
                        ModEffects.MEAT_DIGESTION,
                        IsraelSimulatorConfig.meatDigestionTicks(),
                        0));
            }
        } else if (isDairy) {
            if (entity.hasEffect(ModEffects.MEAT_DIGESTION)) {
                // Violation: dairy consumed while meat waiting period is active
                applyMixingPenalty(entity);
                entity.removeEffect(ModEffects.MEAT_DIGESTION);
            } else {
                entity.addEffect(new MobEffectInstance(
                        ModEffects.DAIRY_DIGESTION,
                        IsraelSimulatorConfig.dairyDigestionTicks(),
                        0));
            }
        }
    }

    private static void applyMixingPenalty(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0));
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
        if (entity instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.kosher_violation"));
        }
    }
}
