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
import net.minecraft.world.entity.player.Inventory;
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
        ItemStack held = player.getItemInHand(hand);
        boolean creative = player.getAbilities().instabuild;
        int stake = DreidelManager.STAKE;
        if (!creative && countShekels(player) < stake) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.dreidel_need_shekels", stake)
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (!creative) {
            removeShekels(player, stake);
        }

        DreidelManager.SpinResult result = DreidelManager.spin(player.getUUID());
        DreidelManager.DreidelLetter letter = result.letter();
        int payout = result.payout();
        int gross = DreidelManager.grossReturn(letter);

        // Successful spin: 10 s cooldown (synced to the client, shows the hotbar overlay).
        player.getCooldowns().addCooldown(held, DreidelManager.COOLDOWN_TICKS);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05);
        }

        player.sendSystemMessage(Component.translatable("message.israel_simulator.dreidel_spin",
                letter.getId().toUpperCase(), letter.getMeaning()));

        if (!creative && gross > 0) {
            ItemStack back = new ItemStack(ModItems.SHEKEL.get(), gross);
            if (!player.addItem(back)) {
                player.drop(back, false);
            }
        }
        if (payout > 0) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.dreidel_win", payout)
                    .withStyle(ChatFormatting.GOLD));
        } else if (payout < 0) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.dreidel_lose", -payout)
                    .withStyle(ChatFormatting.GRAY));
        }

        return InteractionResult.SUCCESS;
    }

    private static int countShekels(Player player) {
        Inventory inv = player.getInventory();
        int count = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.SHEKEL.get())) {
                count += s.getCount();
            }
        }
        return count;
    }

    private static void removeShekels(Player player, int amount) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && amount > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.SHEKEL.get())) {
                int take = Math.min(amount, s.getCount());
                s.shrink(take);
                amount -= take;
            }
        }
        inv.setChanged();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.dreidel.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
