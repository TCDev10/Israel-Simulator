package com.israelsimulator.transport;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

/**
 * Walking shoes providing sustained exploration speed for long journeys (GAME_DESIGN.md §32, TODO §47).
 */
public class WalkingShoesItem extends Item {

    public WalkingShoesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (entity instanceof Player player) {
            // Speed boost while equipped in hands or active slots
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, false, false, false));
        }
        super.inventoryTick(stack, level, entity, slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.walking_shoes.desc").withStyle(ChatFormatting.GRAY));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.walking_shoes.effect").withStyle(ChatFormatting.BLUE));
    }
}
