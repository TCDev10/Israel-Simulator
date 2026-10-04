package com.israelsimulator.item.cultural;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Traditional cultural head covering (GAME_DESIGN.md §12, TODO §22).
 *
 * <p>Equippable in the head/helmet slot via {@link net.minecraft.world.item.equipment.Equippable}.
 * Does not take damage on hurt to preserve cultural wearability without excessive combat power.
 * Required for sacred prayer interactions at the Western Wall.</p>
 */
public class KippahItem extends Item {

    public KippahItem(Properties properties) {
        super(properties);
    }

    /**
     * Checks if the given entity is currently wearing a Kippah in their head slot.
     */
    public static boolean isWearingKippah(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof KippahItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.kippah.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.kippah.requirement")
                .withStyle(ChatFormatting.GOLD));
    }
}
