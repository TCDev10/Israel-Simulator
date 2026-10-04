package com.israelsimulator.westernwall;

import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.registry.ModItems;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
 */
public final class WesternWallManager {
    public static final long COOLDOWN_TICKS = 24000L; // 1 in-game day (20 minutes)
    public static final int REWARD_DIAMONDS = 5;

    private static final Map<UUID, Long> LAST_PRAYER_TIMES = new ConcurrentHashMap<>();

    public enum PrayerStatus {
        SUCCESS,
        MISSING_KIPPAH,
        MISSING_PRAYER_NOTE,
        COOLDOWN_ACTIVE
    }

    private WesternWallManager() {}

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

    public static boolean isOnCooldown(UUID playerId, long currentGameTime) {
        Long lastTime = LAST_PRAYER_TIMES.get(playerId);
        if (lastTime == null) {
            return false;
        }
        return (currentGameTime - lastTime) < COOLDOWN_TICKS;
    }

    public static void setLastPrayerTime(UUID playerId, long time) {
        LAST_PRAYER_TIMES.put(playerId, time);
    }

    public static void clearCooldown(UUID playerId) {
        LAST_PRAYER_TIMES.remove(playerId);
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
        boolean hasKippah = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.KIPPAH.get());
        boolean hasPrayerNote = held.is(ModItems.PRAYER_NOTE.get());
        boolean onCooldown = !player.getAbilities().instabuild && isOnCooldown(player.getUUID(), level.getGameTime());

        PrayerStatus status = validatePrayer(hasKippah, hasPrayerNote, onCooldown);

        if (level.isClientSide()) {
            return status == PrayerStatus.SUCCESS || hasPrayerNote;
        }

        switch (status) {
            case MISSING_KIPPAH -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_need_kippah"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return true;
            }
            case MISSING_PRAYER_NOTE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_need_prayer_note"));
                return false;
            }
            case COOLDOWN_ACTIVE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_cooldown"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.2F);
                return true;
            }
            case SUCCESS -> {
                // Consume prayer note
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                // Record cooldown server-side
                LAST_PRAYER_TIMES.put(player.getUUID(), level.getGameTime());

                // Award 5 Diamonds
                giveOrDrop(player, new ItemStack(Items.DIAMOND, REWARD_DIAMONDS));

                // Apply Blessed effect
                BlessedEffect.applyTo(player);

                // Sound effects
                level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.3F);

                // Visual particles
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                            30, 0.5, 0.5, 0.5, 0.15);
                    serverLevel.sendParticles(ParticleTypes.ENCHANT,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            40, 0.6, 0.6, 0.6, 0.2);
                }

                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_blessed"));
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
