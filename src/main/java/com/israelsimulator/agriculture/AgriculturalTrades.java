package com.israelsimulator.agriculture;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Handles farming-related trades and Farmer NPC interaction.
 *
 * <p>Server-authoritative per AGENTS.md §8 and GAME_DESIGN.md §19, §29.</p>
 */
public final class AgriculturalTrades {
    private AgriculturalTrades() {}

    /**
     * Attempts to execute an agricultural trade when a player interacts with a villager.
     *
     * @return true if an agricultural trade was successfully conducted
     */
    public static boolean tryTrade(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player == null || villager == null || hand == null) return false;

        ItemStack held = player.getItemInHand(hand);
        var maybeResult = AgriculturalEconomy.calculateTrade(held);
        if (maybeResult.isEmpty()) return false;

        var result = maybeResult.get();
        if (result == null || result.itemsConsumed() <= 0) return false;

        if (!player.level().isClientSide()) {
            held.shrink(result.itemsConsumed());

            if (result.shekelsEarned() > 0) {
                ItemStack shekels = AgriculturalEconomy.createShekelPayout(result.shekelsEarned());
                if (!player.getInventory().add(shekels)) {
                    player.drop(shekels, false);
                }
            }

            if (result.agorotEarned() > 0) {
                ItemStack agorot = AgriculturalEconomy.createAgoraPayout(result.agorotEarned());
                if (!player.getInventory().add(agorot)) {
                    player.drop(agorot, false);
                }
            }

            // Audio-visual feedback
            ServerLevel serverLevel = (ServerLevel) player.level();
            serverLevel.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    villager.getX(), villager.getY() + 1.0D, villager.getZ(),
                    8, 0.3D, 0.5D, 0.3D, 0.02D);

            // Chat notification
            String rewardText = result.shekelsEarned() > 0
                    ? result.shekelsEarned() + " Shekel(s)"
                    : result.agorotEarned() + " Agorot";
            player.sendSystemMessage(Component.literal("Traded " + result.itemsConsumed() + " "
                    + result.commodityName() + " for " + rewardText + "!"));
        }

        return true;
    }
}

