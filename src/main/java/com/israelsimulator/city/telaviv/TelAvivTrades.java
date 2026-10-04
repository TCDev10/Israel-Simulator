package com.israelsimulator.city.telaviv;

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
 * Urban trading interactions in Tel Aviv (GAME_DESIGN.md §7, §28).
 * Enables tech industry purchases/sales and local food dining using Shekels.
 */
public final class TelAvivTrades {
    private TelAvivTrades() {}

    public static boolean tryTrade(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        if (!player.level().getBiome(villager.blockPosition()).is(ModBiomes.URBAN_AREA)) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);

        // Tech Purchases using Shekels
        if (held.is(ModItems.SHEKEL.get())) {
            if (held.getCount() >= TelAvivEconomy.LAPTOP_BUY_PRICE && player.isCrouching()) {
                held.shrink(TelAvivEconomy.LAPTOP_BUY_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.LAPTOP.get(), 1));
                completeTrade(player, villager);
                return true;
            }
            if (held.getCount() >= TelAvivEconomy.SMARTPHONE_BUY_PRICE && player.isCrouching()) {
                held.shrink(TelAvivEconomy.SMARTPHONE_BUY_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.SMARTPHONE.get(), 1));
                completeTrade(player, villager);
                return true;
            }
            if (held.getCount() >= TelAvivEconomy.DRONE_PART_BUY_PRICE && player.isCrouching()) {
                held.shrink(TelAvivEconomy.DRONE_PART_BUY_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.DRONE_PART.get(), 1));
                completeTrade(player, villager);
                return true;
            }
            // Street food purchase
            if (held.getCount() >= TelAvivEconomy.FALAFEL_PRICE) {
                held.shrink(TelAvivEconomy.FALAFEL_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.FALAFEL.get(), 1));
                completeTrade(player, villager);
                return true;
            }
        }

        // Selling tech back to Startup founders
        if (held.is(ModItems.LAPTOP.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), TelAvivEconomy.LAPTOP_SELL_PRICE));
            completeTrade(player, villager);
            return true;
        }

        if (held.is(ModItems.SMARTPHONE.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), TelAvivEconomy.SMARTPHONE_SELL_PRICE));
            completeTrade(player, villager);
            return true;
        }

        if (held.is(ModItems.DRONE_PART.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), TelAvivEconomy.DRONE_PART_SELL_PRICE));
            completeTrade(player, villager);
            return true;
        }

        return false;
    }

    private static void completeTrade(Player player, AbstractVillager villager) {
        player.level().playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.05F);

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
