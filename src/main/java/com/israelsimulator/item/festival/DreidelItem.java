package com.israelsimulator.item.festival;

import com.israelsimulator.festival.DreidelManager;
import com.israelsimulator.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Traditional four-sided spinning top used during Hanukkah (GAME_DESIGN.md §34, TODO §34).
 */
public class DreidelItem extends Item {

    public DreidelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        DreidelManager.SpinResult result = DreidelManager.spin(player.getUUID());
        DreidelManager.DreidelLetter letter = result.letter();
        int payout = result.payout();

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05);
        }

        player.sendSystemMessage(Component.translatable("message.israel_simulator.dreidel_spin",
                letter.getId().toUpperCase(), letter.getMeaning()));

        if (payout > 0) {
            player.addItem(new ItemStack(ModItems.SHEKEL.get(), payout));
            player.sendSystemMessage(Component.translatable("message.israel_simulator.dreidel_win", payout)
                    .withStyle(ChatFormatting.GOLD));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.dreidel.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
