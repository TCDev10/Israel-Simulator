package com.israelsimulator.world.map;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Discovery-oriented map item displaying discovered landmarks, regions, and travel hubs (GAME_DESIGN.md §8–10, §32, TODO §48).
 */
public class IsraelMapItem extends Item {
    /** Right-click cooldown: 2 s. */
    public static final int COOLDOWN_TICKS = 40;


    public IsraelMapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Set<String> discovered = PlayerLandmarkTracker.getDiscoveredLandmarks(serverPlayer.getUUID());
            Set<IsraelRegion> visited = PlayerLandmarkTracker.getVisitedRegions(serverPlayer.getUUID());

            serverPlayer.sendSystemMessage(
                    Component.literal("====== [ Map of Israel — Discovery Log ] ======")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
            );

            int totalLandmarks = Landmark.values().length;
            int foundLandmarks = discovered.size();
            int totalRegions = IsraelRegion.values().length;
            int foundRegions = visited.size();

            serverPlayer.sendSystemMessage(
                    Component.literal("Regions Visited: ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(foundRegions + " / " + totalRegions).withStyle(ChatFormatting.GREEN))
            );
            serverPlayer.sendSystemMessage(
                    Component.literal("Landmarks Discovered: ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(foundLandmarks + " / " + totalLandmarks).withStyle(ChatFormatting.GREEN))
            );

            for (Landmark lm : Landmark.values()) {
                boolean isFound = discovered.contains(lm.getId());
                if (isFound) {
                    serverPlayer.sendSystemMessage(
                            Component.literal("  ✔ " + lm.getDisplayName() + " [" + lm.getRegion().getDisplayName() + "]")
                                    .withStyle(ChatFormatting.GREEN)
                    );
                } else {
                    serverPlayer.sendSystemMessage(
                            Component.literal("  ? Undiscovered Landmark (" + lm.getRegion().getDisplayName() + ")")
                                    .withStyle(ChatFormatting.DARK_GRAY)
                    );
                }
            }

            if (foundLandmarks == totalLandmarks) {
                serverPlayer.sendSystemMessage(
                        Component.literal("★ Master Explorer: All landmarks across Israel discovered!")
                                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                );
            }
            serverPlayer.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipOutput, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipOutput, flag);
        tooltipOutput.accept(Component.translatable("item.israel_simulator.israel_map.desc").withStyle(ChatFormatting.GRAY));
        tooltipOutput.accept(Component.translatable("item.israel_simulator.israel_map.usage").withStyle(ChatFormatting.YELLOW));
    }
}
