package com.israelsimulator.item.deadsea;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.Level;

/**
 * Mineral-rich therapeutic mud harvested from the shores of the Dead Sea (GAME_DESIGN.md §20).
 * Cleanses negative physical debuffs and applies temporary absorption and regeneration.
 */
public class DeadSeaMudItem extends Item {
    public DeadSeaMudItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Therapeutic cleanse of negative ailments
            player.removeEffect(MobEffects.SLOWNESS);
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.POISON);

            // Mineral nourishment effects (Absorption I for 60s, Regeneration I for 10s)
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, true, true));

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 1.2F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        5, 0.3, 0.3, 0.3, 0.05);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
