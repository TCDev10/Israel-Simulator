package com.israelsimulator.quest;

import com.israelsimulator.registry.ModItems;
import com.israelsimulator.reputation.ReputationManager;
import com.israelsimulator.reputation.ReputationScores;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Server-authoritative manager for optional non-linear discovery quests (GAME_DESIGN.md §52).
 *
 * <p>Completed quests are stored in {@link CompletedQuests} (world SavedData), not in a static
 * map, so a server restart keeps finished quests and blocks duplicate reward claims.</p>
 */
public final class QuestManager {

    private QuestManager() {}

    public static CompletedQuests get(ServerLevel level) {
        return CompletedQuests.get(level);
    }

    /**
     * Checks if a player has completed a given quest.
     */
    public static boolean isCompleted(CompletedQuests data, UUID playerUuid, DiscoveryQuest quest) {
        if (data == null) {
            return false;
        }
        return data.isCompleted(playerUuid, quest);
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

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        UUID uuid = player.getUUID();
        CompletedQuests completed = CompletedQuests.get(serverLevel);
        if (!completed.markCompleted(uuid, quest)) {
            return false; // Already completed
        }

        ReputationScores scores = ReputationScores.get(serverLevel);
        ReputationManager.adjustReputation(scores, uuid, quest.getRewardFaction(), quest.getReputationReward());

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
    public static Set<DiscoveryQuest> getCompletedQuests(CompletedQuests data, UUID playerUuid) {
        if (data == null) {
            return Collections.emptySet();
        }
        return data.getCompleted(playerUuid);
    }
}
