package com.israelsimulator.desert;

import com.israelsimulator.registry.ModItems;
import com.israelsimulator.world.biome.ModBiomes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Bedouin nomad and desert explorer trading interactions (GAME_DESIGN.md §21).
 */
public final class DesertTrades {
    private DesertTrades() {}

    public static boolean tryTrade(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        if (!player.level().getBiome(villager.blockPosition()).is(ModBiomes.JUDEAN_DESERT)) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);

        // Buy Saddle: Offer 10 Shekels -> Receive Saddle
        if (held.is(ModItems.SHEKEL.get()) && held.getCount() >= 10) {
            held.shrink(10);
            giveOrDrop(player, new ItemStack(Items.SADDLE, 1));
            completeTrade(player, villager);
            return true;
        }

        // Sell 8 Dates -> Receive 1 Shekel
        if (held.is(ModItems.DATES.get()) && held.getCount() >= 8) {
            held.shrink(8);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 1));
            completeTrade(player, villager);
            return true;
        }

        // Sell 4 Wool -> Receive 2 Agorot
        if (held.is(ItemTags.WOOL) && held.getCount() >= 4) {
            held.shrink(4);
            giveOrDrop(player, new ItemStack(ModItems.AGORA.get(), 2));
            completeTrade(player, villager);
            return true;
        }

        // Sell 1 Ancient Coin -> Receive 5 Shekels
        if (held.is(ModItems.ANCIENT_COIN.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 5));
            completeTrade(player, villager);
            return true;
        }

        // Sell 1 Dead Sea Scroll Fragment -> Receive 10 Shekels
        if (held.is(ModItems.DEAD_SEA_SCROLL_FRAGMENT.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 10));
            completeTrade(player, villager);
            return true;
        }

        return false;
    }

    private static void completeTrade(Player player, AbstractVillager villager) {
        player.level().playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 0.95F);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    villager.getX(), villager.getY() + 1.2, villager.getZ(),
                    6, 0.25, 0.25, 0.25, 0.05);
        }
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
