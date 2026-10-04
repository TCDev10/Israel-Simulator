package com.israelsimulator.item.festival;

import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.festival.ShofarCooldowns;
import com.israelsimulator.festival.ShofarManager;
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
 * Traditional musical horn blown during Rosh Hashanah and Yom Kippur (GAME_DESIGN.md §35, TODO §35).
 */
public class ShofarItem extends Item {

    public ShofarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            // The client has no cooldown store. The blast is decided on the server.
            return InteractionResult.SUCCESS;
        }

        ShofarCooldowns cooldowns = ShofarCooldowns.get(serverLevel);
        if (ShofarManager.isOnCooldown(cooldowns, player.getUUID(), level.getGameTime())) {
            return InteractionResult.FAIL;
        }

        ShofarManager.recordBlow(cooldowns, player.getUUID(), level.getGameTime());

        // Play authentic horn sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 1.2F, 0.9F);

        // Apply blessings: BlessedEffect + Luck II + Absorption
        BlessedEffect.applyTo(player);
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, 3600, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));

        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1.2, player.getZ(),
                25, 0.5, 0.5, 0.5, 0.1);
        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.0, player.getZ(),
                20, 0.4, 0.4, 0.4, 0.2);

        player.sendSystemMessage(Component.translatable("message.israel_simulator.shofar_blast")
                .withStyle(ChatFormatting.GOLD));

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.shofar.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
