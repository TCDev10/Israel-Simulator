package com.israelsimulator.item.cultural;

import com.israelsimulator.effect.ModEffects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Rabbi's Crown (GAME_DESIGN.md §16, TODO §40).
 * MYTHIC head equipment offering +20 Armor, sacred sage aesthetic (hat, payot, beard),
 * and bestowing the permanent Blessed Trader effect while equipped.
 */
public class RabbisCrownItem extends Item {

    public RabbisCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (entity instanceof Player player) {
            ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);
            // Verify if the Crown is currently equipped on the head
            if (headItem == stack || headItem.is(this)) {
                if (level.getGameTime() % 20 == 0) {
                    // Apply / refresh permanent Blessed Trader effect and Blessed effect
                    if (ModEffects.BLESSED_TRADER != null) {
                        player.addEffect(new MobEffectInstance(ModEffects.BLESSED_TRADER, 60, 0, true, false, true));
                    }
                    if (ModEffects.BLESSED != null) {
                        player.addEffect(new MobEffectInstance(ModEffects.BLESSED, 60, 1, true, false, true));
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.rabbis_crown.desc")
                .withStyle(ChatFormatting.GOLD));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.rabbis_crown.armor")
                .withStyle(ChatFormatting.BLUE));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.rabbis_crown.effect")
                .withStyle(ChatFormatting.YELLOW));
    }
}
