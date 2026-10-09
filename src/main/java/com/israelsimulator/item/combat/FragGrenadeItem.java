package com.israelsimulator.item.combat;

import com.israelsimulator.entity.projectile.FragGrenadeEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Frag grenade: thrown like a snowball, bounces, and explodes after its fuse
 * ({@link com.israelsimulator.config.IsraelSimulatorConfig#grenadeFuseTicks()}).
 */
public class FragGrenadeItem extends Item {
    public static final int THROW_COOLDOWN_TICKS = 20;

    public FragGrenadeItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.PLAYERS, 0.8F, 1.6F);
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(FragGrenadeEntity::new, serverLevel, stack, player, 0.0F, 1.1F, 1.0F);
        }
        player.getCooldowns().addCooldown(stack, THROW_COOLDOWN_TICKS);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.israel_simulator.frag_grenade.desc").withStyle(ChatFormatting.GRAY));
    }
}
