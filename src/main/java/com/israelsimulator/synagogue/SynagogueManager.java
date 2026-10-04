package com.israelsimulator.synagogue;

import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.item.cultural.KippahItem;
import com.israelsimulator.registry.ModItems;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Spiritual mechanics, Aron Kodesh (Ark), and Bimah interaction manager for Synagogues (GAME_DESIGN.md §25).
 */
public final class SynagogueManager {

    public static final long COOLDOWN_TICKS = 12000L; // 10 minutes (anti-spam cooldown)

    public enum ArkInteractionStatus {
        SUCCESS,
        MISSING_KIPPAH,
        COOLDOWN_ACTIVE,
        INVALID_LOCATION
    }

    private SynagogueManager() {}

    public static ArkInteractionStatus validateInteraction(boolean hasKippah, boolean onCooldown) {
        if (!hasKippah) {
            return ArkInteractionStatus.MISSING_KIPPAH;
        }
        if (onCooldown) {
            return ArkInteractionStatus.COOLDOWN_ACTIVE;
        }
        return ArkInteractionStatus.SUCCESS;
    }

    public static boolean isOnCooldown(SynagogueCooldowns cooldowns, UUID playerId, long currentGameTime) {
        if (cooldowns == null || playerId == null) {
            return false;
        }
        Long lastTime = cooldowns.getLastUseTime(playerId);
        return lastTime != null && (currentGameTime - lastTime) < COOLDOWN_TICKS;
    }

    public static void clearCooldown(SynagogueCooldowns cooldowns, UUID playerId) {
        if (cooldowns != null) {
            cooldowns.clearCooldown(playerId);
        }
    }

    /**
     * Attempts an Aron Kodesh (Holy Ark) or Bimah prayer at a designated sacred block.
     * Suitable for chiseled bookshelves, lecterns, or decorated altars within synagogues.
     */
    public static boolean tryArkPray(Player player, InteractionHand hand, BlockPos pos) {
        Level level = player.level();
        if (hand != InteractionHand.MAIN_HAND || level.isClientSide()) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        boolean isSacredBlock = state.is(Blocks.CHISELED_BOOKSHELF)
                || state.is(Blocks.LECTERN)
                || state.is(Blocks.BOOKSHELF);

        if (!isSacredBlock) {
            return false;
        }

        boolean hasKippah = KippahItem.isWearingKippah(player);
        SynagogueCooldowns cooldowns = SynagogueCooldowns.get(serverLevel);
        boolean onCooldown = !player.getAbilities().instabuild
                && isOnCooldown(cooldowns, player.getUUID(), level.getGameTime());

        ArkInteractionStatus status = validateInteraction(hasKippah, onCooldown);

        switch (status) {
            case MISSING_KIPPAH -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.ark_need_kippah"));
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return true;
            }
            case COOLDOWN_ACTIVE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.ark_cooldown"));
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.2F);
                return true;
            }
            case SUCCESS -> {
                // If holding a prayer note or Shekel for charity, consume 1
                ItemStack held = player.getItemInHand(hand);
                boolean isOffering = false;
                if (held.is(ModItems.PRAYER_NOTE.get()) || held.is(ModItems.SHEKEL.get())) {
                    held.shrink(1);
                    isOffering = true;
                }

                cooldowns.setLastUseTime(player.getUUID(), level.getGameTime());
                BlessedEffect.applyTo(player);

                if (isOffering) {
                    player.sendSystemMessage(Component.translatable("message.israel_simulator.ark_offering_complete"));
                } else {
                    player.sendSystemMessage(Component.translatable("message.israel_simulator.ark_prayer_complete"));
                }

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                        25, 0.4, 0.4, 0.4, 0.15);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        15, 0.3, 0.3, 0.3, 0.08);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
