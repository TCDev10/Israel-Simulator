package com.israelsimulator.westernwall;

import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.registry.ModItems;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Server-authoritative logic for the Western Wall prayer interaction (GAME_DESIGN.md §10, §11).
 * Validates Kippah requirement, consumes Prayer Note, enforces anti-exploit cooldowns,
 * and awards 5 Diamonds and the Blessed Effect.
 *
 * <p>Cooldown state is not kept in a static map. The server world stores it in
 * {@link WesternWallCooldowns}, so a restart cannot clear it and the client cannot
 * keep a separate copy.</p>
 */
public final class WesternWallManager {
    public static final long COOLDOWN_TICKS = 24000L; // 1 in-game day (20 minutes)
    public static final int REWARD_DIAMONDS = 5;

    public enum PrayerStatus {
        SUCCESS,
        MISSING_KIPPAH,
        MISSING_PRAYER_NOTE,
        COOLDOWN_ACTIVE
    }

    private WesternWallManager() {}

    /**
     * Creative mode still receives the diamond reward, so it must not skip the cooldown.
     */
    public static boolean enforcesCooldown(boolean instabuild) {
        return true;
    }

    /**
     * Pure validation logic suitable for fast unit testing.
     */
    public static PrayerStatus validatePrayer(boolean hasKippah, boolean hasPrayerNote, boolean onCooldown) {
        if (!hasKippah) {
            return PrayerStatus.MISSING_KIPPAH;
        }
        if (!hasPrayerNote) {
            return PrayerStatus.MISSING_PRAYER_NOTE;
        }
        if (onCooldown) {
            return PrayerStatus.COOLDOWN_ACTIVE;
        }
        return PrayerStatus.SUCCESS;
    }

    /**
     * Arm swing when the player clicked the wall with a prayer note in the main hand.
     * Covers success and the feedback paths that already ate the click (missing kippah / cooldown).
     */
    public static boolean shouldAnimateSwing(PrayerStatus status) {
        return status == PrayerStatus.SUCCESS
                || status == PrayerStatus.MISSING_KIPPAH
                || status == PrayerStatus.COOLDOWN_ACTIVE;
    }

    public static boolean isOnCooldown(WesternWallCooldowns cooldowns, UUID playerId, long currentGameTime) {
        if (cooldowns == null || playerId == null) {
            return false;
        }
        Long lastTime = cooldowns.getLastPrayerTime(playerId);
        if (lastTime == null) {
            return false;
        }
        return (currentGameTime - lastTime) < COOLDOWN_TICKS;
    }

    /**
     * Executes the prayer interaction sequence when a player right-clicks the Western Wall.
     */
    public static boolean tryPray(Player player, InteractionHand hand, BlockPos pos) {
        Level level = player.level();
        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean hasKippah = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.KIPPAH.get())
                || com.israelsimulator.item.cultural.KippahItem.isWearingKippah(player);
        boolean hasPrayerNote = held.is(ModItems.PRAYER_NOTE.get());

        if (level.isClientSide()) {
            // No cooldown/reward on the client. Returning true lets ModGameEvents cancel with
            // SUCCESS so the local arm swings; the server still runs the real prayer once.
            if (hasPrayerNote) {
                player.swing(InteractionHand.MAIN_HAND);
            }
            return hasPrayerNote;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        WesternWallCooldowns cooldowns = WesternWallCooldowns.get(serverLevel);
        boolean onCooldown = enforcesCooldown(player.getAbilities().instabuild)
                && isOnCooldown(cooldowns, player.getUUID(), level.getGameTime());

        PrayerStatus status = validatePrayer(hasKippah, hasPrayerNote, onCooldown);

        switch (status) {
            case MISSING_KIPPAH -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_need_kippah"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                // true = notify other clients of the swing
                player.swing(InteractionHand.MAIN_HAND, true);
                return true;
            }
            case MISSING_PRAYER_NOTE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_need_prayer_note"));
                return false;
            }
            case COOLDOWN_ACTIVE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_cooldown"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.2F);
                player.swing(InteractionHand.MAIN_HAND, true);
                return true;
            }
            case SUCCESS -> {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                // Record before the reward so a later save still remembers the prayer.
                cooldowns.setLastPrayerTime(player.getUUID(), level.getGameTime());

                giveOrDrop(player, new ItemStack(Items.DIAMOND, REWARD_DIAMONDS));

                BlessedEffect.applyTo(player);

                level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.3F);

                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                        30, 0.5, 0.5, 0.5, 0.15);
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        40, 0.6, 0.6, 0.6, 0.2);

                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_blessed"));
                player.swing(InteractionHand.MAIN_HAND, true);
                return true;
            }
        }

        return false;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
