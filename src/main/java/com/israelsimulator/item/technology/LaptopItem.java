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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/** Laptop: hold right-click for a work session at your startup, paid in Shekels ({@link StartupWork}). */
public class LaptopItem extends Item {
    public LaptopItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        if (WorkPlaces.isShabbat(level)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.work.shabbat").withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return StartupWork.WORK_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(stack, StartupWork.LAPTOP_COOLDOWN);
            if (level instanceof ServerLevel sl && !WorkPlaces.isShabbat(level)) {
                int sessions = stack.getOrDefault(ModDataComponents.WORK_SESSIONS.get(), 0) + 1;
                stack.set(ModDataComponents.WORK_SESSIONS.get(), sessions);
                boolean hub = WorkPlaces.isTechHub(sl, player.blockPosition());
                int pay = StartupWork.sessionPay(sessions, hub);
                ItemStack money = new ItemStack(ModItems.SHEKEL.get(), pay);
                if (!player.getInventory().add(money)) player.drop(money, false);
                player.causeFoodExhaustion(StartupWork.WORK_EXHAUSTION);
                player.sendSystemMessage(Component.translatable(hub ? "message.israel_simulator.work.session_hub"
                        : "message.israel_simulator.work.session", pay, sessions).withStyle(ChatFormatting.GREEN));
                if (StartupWork.isExit(sessions)) {
                    player.sendSystemMessage(Component.translatable("message.israel_simulator.work.exit", StartupWork.EXIT_BONUS)
                            .withStyle(ChatFormatting.GOLD));
                    level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.4F);
                }
            }
        }
        return stack;
    }
}
