package com.israelsimulator.deadsea;

import com.israelsimulator.registry.ModBlocks;
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

/**
 * Tourist and Spa transactions in the Dead Sea region (GAME_DESIGN.md §20).
 */
public final class DeadSeaTrades {
    private DeadSeaTrades() {}

    public static boolean tryTrade(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        if (!player.level().getBiome(villager.blockPosition()).is(ModBiomes.DEAD_SEA)) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);

        // Offer 2 Shekels -> Receive 1 Dead Sea Mud
        if (held.is(ModItems.SHEKEL.get()) && held.getCount() >= 2) {
            held.shrink(2);
            giveOrDrop(player, new ItemStack(ModItems.DEAD_SEA_MUD.get(), 1));
            completeTrade(player, villager);
            return true;
        }

        // Sell 1 Dead Sea Mud -> Receive 1 Shekel + 5 Agorot
        if (held.is(ModItems.DEAD_SEA_MUD.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 1));
            giveOrDrop(player, new ItemStack(ModItems.AGORA.get(), 5));
            completeTrade(player, villager);
            return true;
        }

        // Sell 1 Dead Sea Scroll Fragment -> Tourist pays 12 Shekels
        if (held.is(ModItems.DEAD_SEA_SCROLL_FRAGMENT.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 12));
            completeTrade(player, villager);
            return true;
        }

        // Sell 1 Salt Block -> Receive 2 Agorot
        if (held.is(ModBlocks.SALT_BLOCK_ITEM.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.AGORA.get(), 2));
            completeTrade(player, villager);
            return true;
        }

        return false;
    }

    private static void completeTrade(Player player, AbstractVillager villager) {
        player.level().playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);

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
