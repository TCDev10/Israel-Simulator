package com.israelsimulator.item.combat;

import com.israelsimulator.entity.projectile.PalantrioDroneEntity;
import com.israelsimulator.registry.ModEntities;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Launches a {@link PalantrioDroneEntity} in front of the player. Consumed on use, 5 s cooldown. */
public class PalantrioDroneItem extends Item {
    public static final int COOLDOWN_TICKS = 100;

    public PalantrioDroneItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            PalantrioDroneEntity drone = ModEntities.PALANTRIO_DRONE.get().create(server, EntitySpawnReason.SPAWN_ITEM_USE);
            if (drone == null) {
                return InteractionResult.FAIL;
            }
            Vec3 pos = player.getEyePosition().add(player.getViewVector(1.0F).scale(1.2));
            drone.snapTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
            drone.setOwner(player);
            server.addFreshEntity(drone);
            server.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BEEHIVE_EXIT, SoundSource.PLAYERS, 1.0F, 1.5F);
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.israel_simulator.palantrio_drone.desc").withStyle(ChatFormatting.GRAY));
    }
}
