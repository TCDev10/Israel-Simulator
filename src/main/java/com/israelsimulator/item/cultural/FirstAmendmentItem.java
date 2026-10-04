package com.israelsimulator.item.cultural;

import com.israelsimulator.effect.ModEffects;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Historic collectible charter representing freedom of expression and assembly (GAME_DESIGN.md §35, §38, TODO §39).
 * LEGENDARY rarity item earned from active civic participation in the Public Speech event.
 * Grants the Freedom effect, removing negative movement debuffs.
 */
public class FirstAmendmentItem extends Item {

    public FirstAmendmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Apply Freedom effect for 60 seconds (1200 ticks)
        if (ModEffects.FREEDOM != null) {
            player.addEffect(new MobEffectInstance(ModEffects.FREEDOM, 1200, 0));
        }

        if (player.hasEffect(MobEffects.SLOWNESS)) {
            player.removeEffect(MobEffects.SLOWNESS);
        }
        if (player.hasEffect(MobEffects.MINING_FATIGUE)) {
            player.removeEffect(MobEffects.MINING_FATIGUE);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    25, 0.4, 0.4, 0.4, 0.1);
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    20, 0.3, 0.3, 0.3, 0.15);
        }

        player.sendSystemMessage(Component.translatable("message.israel_simulator.first_amendment_used")
                .withStyle(ChatFormatting.GOLD));

        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        // Passive cleansing of slowness every 3 seconds while carried
        if (entity instanceof Player player && level.getGameTime() % 60 == 0) {
            if (player.hasEffect(MobEffects.SLOWNESS)) {
                player.removeEffect(MobEffects.SLOWNESS);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.first_amendment.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.first_amendment.effect")
                .withStyle(ChatFormatting.AQUA));
    }
}
