package com.israelsimulator.transport;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

/**
 * Rav-Kav public transit pass card for buses, trains, and light rail (GAME_DESIGN.md §32, TODO §47).
 */
public class RavKavItem extends Item {

    public RavKavItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.sendSystemMessage(
                    Component.translatable("item.israel_simulator.rav_kav.status")
                            .withStyle(ChatFormatting.AQUA)
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.rav_kav.desc").withStyle(ChatFormatting.GRAY));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.rav_kav.unlimited").withStyle(ChatFormatting.GREEN));
    }
}
