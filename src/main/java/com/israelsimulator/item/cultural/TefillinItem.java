package com.israelsimulator.item.cultural;

import com.israelsimulator.effect.BlessedEffect;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Traditional Jewish phylacteries used for morning prayer (GAME_DESIGN.md §14, TODO §24).
 *
 * <p>Right-click ritual requires daylight, wearing a Kippah, and observes a server-authoritative
 * anti-spam cooldown. Wearing a Talit provides enhanced spiritual bonuses.</p>
 */
public class TefillinItem extends Item {

    public static final long COOLDOWN_TICKS = 12000L; // 10 real minutes (half Minecraft day)

    public enum TefillinStatus {
        SUCCESS,
        MISSING_KIPPAH,
        NOT_DAYTIME,
        COOLDOWN_ACTIVE
    }

    public TefillinItem(Properties properties) {
        super(properties);
    }

    public static TefillinManager.TefillinStatus validatePrayer(boolean hasKippah, boolean isDaytime, boolean onCooldown) {
        return TefillinManager.validatePrayer(hasKippah, isDaytime, onCooldown);
    }

    public static boolean isOnCooldown(TefillinCooldowns cooldowns, UUID playerId, long currentGameTime) {
        return TefillinManager.isOnCooldown(cooldowns, playerId, currentGameTime);
    }

    public static void setLastUseTime(TefillinCooldowns cooldowns, UUID playerId, long time) {
        TefillinManager.setLastUseTime(cooldowns, playerId, time);
    }

    public static void clearCooldown(TefillinCooldowns cooldowns, UUID playerId) {
        TefillinManager.clearCooldown(cooldowns, playerId);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        boolean hasKippah = KippahItem.isWearingKippah(player);
        boolean isDaytime = level.isBrightOutside();

        if (!(level instanceof ServerLevel serverLevel)) {
            // The client has no cooldown store. Reward and cooldown are decided on the server.
            TefillinManager.TefillinStatus preview = validatePrayer(hasKippah, isDaytime, false);
            return preview == TefillinManager.TefillinStatus.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        TefillinCooldowns cooldowns = TefillinCooldowns.get(serverLevel);
        boolean onCooldown = !player.getAbilities().instabuild
                && isOnCooldown(cooldowns, player.getUUID(), level.getGameTime());

        TefillinManager.TefillinStatus status = validatePrayer(hasKippah, isDaytime, onCooldown);

        switch (status) {
            case MISSING_KIPPAH -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.tefillin_need_kippah"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return InteractionResult.FAIL;
            }
            case NOT_DAYTIME -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.tefillin_not_daytime"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return InteractionResult.FAIL;
            }
            case COOLDOWN_ACTIVE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.tefillin_cooldown"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.2F);
                return InteractionResult.FAIL;
            }
            case SUCCESS -> {
                // Record cooldown
                TefillinManager.setLastUseTime(cooldowns, player.getUUID(), level.getGameTime());

                // Apply blessings: BlessedEffect + Resistance + Strength if also wearing Talit
                BlessedEffect.applyTo(player);
                boolean hasTalit = TalitItem.isWearingTalit(player);
                if (hasTalit) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 6000, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 6000, 1));
                    player.sendSystemMessage(Component.translatable("message.israel_simulator.tefillin_talit_complete"));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 3600, 0));
                    player.sendSystemMessage(Component.translatable("message.israel_simulator.tefillin_blessed"));
                }

                // Sounds
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.3F);

                // Particles
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.2, player.getZ(),
                        35, 0.5, 0.6, 0.5, 0.2);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        20, 0.4, 0.5, 0.4, 0.1);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.tefillin.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.tefillin.requirement")
                .withStyle(ChatFormatting.GOLD));
    }
}
