package com.israelsimulator.item.combat;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Standard-issue security sidearm carried by Coalition VIP guards (GAME_DESIGN.md §43).
 * Shoots high-velocity kinetic rounds with accurate ballistic raycasting.
 * Has 250 durability, consumes durability on fire, and can be enchanted with Unbreaking and Mending.
 */
public class SecurityPistolItem extends Item {

    public SecurityPistolItem(Properties properties) {
        super(properties.durability(250).enchantable(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(held, 14);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            held.hurtAndBreak(1, player, slot);

            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 muzzlePos = eyePos.add(lookVec.scale(0.5));
            Vec3 endPos = eyePos.add(lookVec.scale(32.0));

            // Gunshot acoustic feedback
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.8F, 1.8F);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.4F, 2.0F);

            // Muzzle flash FX
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, muzzlePos.x, muzzlePos.y, muzzlePos.z, 2, 0.02, 0.02, 0.02, 0.02);
            serverLevel.sendParticles(ParticleTypes.SMOKE, muzzlePos.x, muzzlePos.y, muzzlePos.z, 3, 0.05, 0.05, 0.05, 0.02);

            // Ballistic trajectory particles
            for (double d = 1.0; d < 32.0; d += 1.2) {
                Vec3 p = eyePos.add(lookVec.scale(d));
                serverLevel.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }

            // Entity hit detection
            AABB traceBox = new AABB(eyePos, endPos).inflate(1.2);
            List<LivingEntity> potentialTargets = serverLevel.getEntitiesOfClass(LivingEntity.class, traceBox,
                    e -> e != player && e.isAlive());

            LivingEntity closestTarget = null;
            double minDistanceSq = Double.MAX_VALUE;

            for (LivingEntity target : potentialTargets) {
                AABB targetBox = target.getBoundingBox().inflate(0.3);
                Optional<Vec3> hit = targetBox.clip(eyePos, endPos);
                if (hit.isPresent()) {
                    double distSq = eyePos.distanceToSqr(hit.get());
                    if (distSq < minDistanceSq) {
                        minDistanceSq = distSq;
                        closestTarget = target;
                    }
                }
            }

            if (closestTarget != null) {
                closestTarget.hurtServer(serverLevel, player.damageSources().playerAttack(player), 9.0F);
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        closestTarget.getX(), closestTarget.getY() + 1.0, closestTarget.getZ(),
                        5, 0.2, 0.2, 0.2, 0.1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipConsumer, TooltipFlag flag) {
        tooltipConsumer.accept(Component.translatable("item.israel_simulator.pistol.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
