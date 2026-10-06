package com.israelsimulator.item.cultural;

import com.israelsimulator.entity.boss.BibiBossEntity;
import com.israelsimulator.entity.boss.BibiBossSpawner;
import java.util.List;
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
import net.minecraft.world.phys.AABB;

/**
 * Sacred talisman used exclusively to summon the Coalition Premier Bibi Boss (GAME_DESIGN.md §41–44).
 * Crafted through a super late-game ritual recipe requiring a Nether Star, Netherite, Diamond blocks,
 * Dead Sea Scroll fragments, and Ancient Coins.
 */
public class StarOfDavidItem extends Item {

    public StarOfDavidItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Anti-duplication check: prevent multiple bosses in the same arena
            if (BibiBossSpawner.isBossAlreadyActive(serverLevel, player.blockPosition())) {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.bibi_already_active")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                return InteractionResult.FAIL;
            }

            // Spawn the Bibi Boss at the player's location
            BibiBossEntity boss = BibiBossSpawner.spawnBoss(serverLevel, player.blockPosition().above(1));
            if (boss != null) {
                // Dramatic acoustic and particle feedback
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.9F);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.8F, 1.0F);

                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        60, 1.0, 1.0, 1.0, 0.5);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        40, 0.8, 1.2, 0.8, 0.2);

                // Broadcast summoning message to all nearby players
                Component summonMsg = Component.translatable("message.israel_simulator.bibi_summoned_by_star")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
                AABB broadcastArea = player.getBoundingBox().inflate(96.0);
                for (Player p : serverLevel.getEntitiesOfClass(Player.class, broadcastArea)) {
                    p.sendSystemMessage(summonMsg);
                }

                // Consume Star of David in Survival mode
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                player.getCooldowns().addCooldown(held, 60);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipConsumer, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipConsumer, flag);
        tooltipConsumer.accept(Component.translatable("item.israel_simulator.star_of_david.desc")
                .withStyle(ChatFormatting.GOLD));
        tooltipConsumer.accept(Component.translatable("item.israel_simulator.star_of_david.summon_hint")
                .withStyle(ChatFormatting.AQUA));
    }
}
