package com.israelsimulator.item.combat;

import com.israelsimulator.entity.boss.BibiMissileEntity;
import com.israelsimulator.registry.ModEntities;
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
import net.minecraft.world.phys.Vec3;

/**
 * Tactical miniature rocket projectile (GAME_DESIGN.md §41–43).
 * Can be launched forward, producing homing or ballistic explosive impacts.
 */
public class MissileItem extends Item {

    public MissileItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(held, 30);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getViewVector(1.0F);

            BibiMissileEntity missile = new BibiMissileEntity(ModEntities.BIBI_MISSILE.get(), serverLevel);
            missile.setPos(eyePos.x + lookVec.x * 0.8, eyePos.y + lookVec.y * 0.8, eyePos.z + lookVec.z * 0.8);
            missile.setOwner(player);
            missile.setDeltaMovement(lookVec.scale(0.95));

            serverLevel.addFreshEntity(missile);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 2.0F, 0.8F);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    eyePos.x, eyePos.y, eyePos.z, 6, 0.1, 0.1, 0.1, 0.05);

            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipConsumer, TooltipFlag flag) {
        tooltipConsumer.accept(Component.translatable("item.israel_simulator.missile.desc")
                .withStyle(ChatFormatting.RED));
    }
}
