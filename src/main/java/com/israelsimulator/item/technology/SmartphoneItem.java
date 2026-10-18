package com.israelsimulator.item.technology;

import com.israelsimulator.economy.work.StartupWork;
import com.israelsimulator.economy.work.WorkPlaces;
import com.israelsimulator.registry.ModDataComponents;
import com.israelsimulator.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Smartphone: a delivery gig in the cities (Shekels, short cooldown) and your startup's status
 * (sessions on your laptop, sessions to the next exit).
 */
public class SmartphoneItem extends Item {
    public SmartphoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        for (ItemStack s : player.getInventory().getNonEquipmentItems()) {
            if (s.is(ModItems.LAPTOP.get())) {
                int sessions = s.getOrDefault(ModDataComponents.WORK_SESSIONS.get(), 0);
                player.sendSystemMessage(Component.translatable("message.israel_simulator.work.status", sessions,
                        StartupWork.sessionsToExit(sessions)).withStyle(ChatFormatting.AQUA));
                break;
            }
        }
        if (WorkPlaces.isShabbat(level)) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.work.shabbat").withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS;
        }
        if (!WorkPlaces.isCity(sl, player.blockPosition())) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.work.no_gigs").withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }
        player.getCooldowns().addCooldown(stack, StartupWork.GIG_COOLDOWN);
        ItemStack money = new ItemStack(ModItems.SHEKEL.get(), StartupWork.GIG_PAY);
        if (!player.getInventory().add(money)) player.drop(money, false);
        player.causeFoodExhaustion(1.0F);
        player.sendSystemMessage(Component.translatable("message.israel_simulator.work.gig", StartupWork.GIG_PAY)
                .withStyle(ChatFormatting.GREEN));
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.7F, 1.6F);
        return InteractionResult.SUCCESS;
    }
}
