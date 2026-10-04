package com.israelsimulator.city.jaffa;

import com.israelsimulator.registry.ModItems;
import com.israelsimulator.world.biome.ModBiomes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Historical trading and flea market transactions in Jaffa (GAME_DESIGN.md §8).
 */
public final class JaffaTrades {
    private JaffaTrades() {}

    public static boolean tryTrade(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        if (!player.level().getBiome(villager.blockPosition()).is(ModBiomes.MEDITERRANEAN_COAST)) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);

        // Flea market antique sale: Ancient Coin -> 6 Shekels
        if (held.is(ModItems.ANCIENT_COIN.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 6));
            completeTrade(player, villager);
            return true;
        }

        // Flea market wood carving sale: Olive Wood Carving -> 8 Shekels
        if (held.is(ModItems.OLIVE_WOOD_CARVING.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 8));
            completeTrade(player, villager);
            return true;
        }

        // Port citrus export: 1 Shekel -> 6 Jaffa Oranges (Citrus)
        if (held.is(ModItems.SHEKEL.get()) && player.isCrouching()) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.CITRUS.get(), 6));
            completeTrade(player, villager);
            return true;
        }

        // Port fish market: 1 Shekel -> 4 Cooked Cod
        if (held.is(ModItems.SHEKEL.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(Items.COOKED_COD, 4));
            completeTrade(player, villager);
            return true;
        }

        return false;
    }

    private static void completeTrade(Player player, AbstractVillager villager) {
        player.level().playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 0.9F);

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
