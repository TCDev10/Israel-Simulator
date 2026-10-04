package com.israelsimulator.quest;

import com.israelsimulator.registry.ModItems;
import com.israelsimulator.reputation.ReputationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative manager for optional non-linear discovery quests (GAME_DESIGN.md §52).
 *
 * <p>Tracks completed quests per player UUID, awards faction reputation and currency rewards,
 * and ensures quests can only be claimed once without duplicate farming.</p>
 */
public final class QuestManager {
    private static final Map<UUID, Set<DiscoveryQuest>> COMPLETED_QUESTS = new ConcurrentHashMap<>();

    private QuestManager() {}

    /**
     * Checks if a player has completed a given quest.
     */
    public static boolean isCompleted(UUID playerUuid, DiscoveryQuest quest) {
        Set<DiscoveryQuest> completed = COMPLETED_QUESTS.get(playerUuid);
        return completed != null && completed.contains(quest);
    }

    /**
     * Attempts to complete and claim a discovery quest for a player.
     * Thread-safe and server-authoritative to prevent double-claiming.
     *
     * @return true if newly completed and rewards awarded; false if already completed.
     */
    public static boolean completeQuest(ServerPlayer player, DiscoveryQuest quest) {
        Objects.requireNonNull(player, "Player must not be null");
        Objects.requireNonNull(quest, "Quest must not be null");

        UUID uuid = player.getUUID();
        Set<DiscoveryQuest> set = COMPLETED_QUESTS.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());

        // Atomically check and add
        if (!set.add(quest)) {
            return false; // Already completed
        }

        // Award reputation
        ReputationManager.adjustReputation(uuid, quest.getRewardFaction(), quest.getReputationReward());

        // Award currency rewards (e.g. Shekels)
        if (quest.getRewardAmount() > 0) {
            ItemStack rewardStack = new ItemStack(ModItems.SHEKEL.get(), quest.getRewardAmount());
            if (!player.getInventory().add(rewardStack)) {
                player.drop(rewardStack, false);
            }
        }

        // Notify player with sound and announcement
        player.sendSystemMessage(Component.literal("★ Discovery Quest Completed: ")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.literal(quest.getTitle()).withStyle(ChatFormatting.YELLOW))
        );
        player.sendSystemMessage(Component.literal("  " + quest.getDescription())
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal("  Rewards: +" + quest.getReputationReward() + " "
                + quest.getRewardFaction().name() + " Rep, " + quest.getRewardAmount() + " Shekels.")
                .withStyle(ChatFormatting.GREEN));

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        return true;
    }

    /**
     * Gets all quests completed by a player.
     */
    public static Set<DiscoveryQuest> getCompletedQuests(UUID playerUuid) {
        Set<DiscoveryQuest> set = COMPLETED_QUESTS.get(playerUuid);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    /**
     * Resets quest data for testing.
     */
    public static void resetForTesting() {
        COMPLETED_QUESTS.clear();
    }
}
