package com.israelsimulator.item.cultural;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Traditional Jewish prayer shawl with tzitzit fringes (GAME_DESIGN.md §13, TODO §23).
 *
 * <p>Equippable in the chest slot via {@link net.minecraft.world.item.equipment.Equippable}.
 * Grants spiritual focus, mild damage resistance, and luck while worn.
 * Does not take durability damage on hurt.</p>
 */
public class TalitItem extends Item {

    public TalitItem(Properties properties) {
        super(properties);
    }

    /**
     * Checks if the given entity is currently wearing a Talit in their chest slot.
     */
    public static boolean isWearingTalit(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TalitItem;
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.CHEST && entity instanceof LivingEntity living) {
            // Refresh mild resistance and luck every 2 seconds
            if (level.getGameTime() % 40 == 0) {
                living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0, false, false, true));
                living.addEffect(new MobEffectInstance(MobEffects.LUCK, 80, 0, false, false, true));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.talit.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.talit.bonus")
                .withStyle(ChatFormatting.AQUA));
    }
}
