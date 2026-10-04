package com.israelsimulator.city.jerusalem;

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
 * Historical and traditional marketplace trades in Jerusalem (GAME_DESIGN.md §9).
 * Provides access to sacred Judaica items (Kippah, Talit, Tefillin, Prayer Notes) and traditional foods.
 */
public final class JerusalemTrades {
    private JerusalemTrades() {}

    public static boolean tryTrade(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        if (!player.level().getBiome(villager.blockPosition()).is(ModBiomes.JERUSALEM)) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);

        // Buy Judaica using Shekels
        if (held.is(ModItems.SHEKEL.get())) {
            // 12 Shekels -> Tefillin
            if (held.getCount() >= JerusalemEconomy.TEFILLIN_PRICE && player.isCrouching()) {
                held.shrink(JerusalemEconomy.TEFILLIN_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.TEFILLIN.get(), 1));
                completeTrade(player, villager);
                return true;
            }
            // 8 Shekels -> Talit
            if (held.getCount() >= JerusalemEconomy.TALIT_PRICE && player.isCrouching()) {
                held.shrink(JerusalemEconomy.TALIT_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.TALIT.get(), 1));
                completeTrade(player, villager);
                return true;
            }
            // 6 Shekels -> Mezuzah
            if (held.getCount() >= JerusalemEconomy.MEZUZAH_PRICE && player.isCrouching()) {
                held.shrink(JerusalemEconomy.MEZUZAH_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.MEZUZAH.get(), 1));
                completeTrade(player, villager);
                return true;
            }
            // 4 Shekels -> Kippah
            if (held.getCount() >= JerusalemEconomy.KIPPAH_PRICE) {
                held.shrink(JerusalemEconomy.KIPPAH_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.KIPPAH.get(), 1));
                completeTrade(player, villager);
                return true;
            }
            // 1 Shekel -> Prayer Note
            if (held.getCount() >= JerusalemEconomy.PRAYER_NOTE_PRICE) {
                held.shrink(JerusalemEconomy.PRAYER_NOTE_PRICE);
                giveOrDrop(player, new ItemStack(ModItems.PRAYER_NOTE.get(), 1));
                completeTrade(player, villager);
                return true;
            }
        }

        // Sell Dead Sea Scroll Fragment to Jerusalem antiquities scholar -> 14 Shekels
        if (held.is(ModItems.DEAD_SEA_SCROLL_FRAGMENT.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 14));
            completeTrade(player, villager);
            return true;
        }

        // Sell handcrafted Mezuzah -> 4 Shekels
        if (held.is(ModItems.MEZUZAH.get())) {
            held.shrink(1);
            giveOrDrop(player, new ItemStack(ModItems.SHEKEL.get(), 4));
            completeTrade(player, villager);
            return true;
        }

        return false;
    }

    private static void completeTrade(Player player, AbstractVillager villager) {
        player.level().playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.1F);

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
