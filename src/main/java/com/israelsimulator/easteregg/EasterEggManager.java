package com.israelsimulator.easteregg;

import com.israelsimulator.registry.ModItems;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;

/**
 * Manages satirical and humorous easter eggs, absurd events, and secret interactions (GAME_DESIGN.md §50).
 *
 * <p>Safeguards enforce:
 * <ul>
 *   <li>No progression breaks: Easter eggs never award overpowered endgame gear.</li>
 *   <li>Anti-spam: Minimum cooldown between easter egg interactions per player.</li>
 *   <li>Server authority: All triggers and particle/sound broadcasts validated on the server.</li>
 * </ul>
 *
 * <p>The anti-spam clock is not a static map. The server world stores it in
 * {@link EasterEggCooldowns}.</p>
 */
public final class EasterEggManager {
    private static final Random RNG = new Random();
    /** Five seconds between easter-egg lines or the kippah-cat interaction. */
    public static final long COOLDOWN_TICKS = 100L;

    private static final List<String> ABSURD_DIALOGUES = List.of(
            "Did you just put tahini on chocolate cake?! What kind of Silicon Alley accelerator taught you that?!",
            "My cousin in Herzliya is coding a smart drone to deliver piping-hot Shakshuka directly to your balcony.",
            "If you spin the dreidel and get Shin four times in a row, that's not bad luck—that's an unhandled exception.",
            "The street cat behind the falafel stand has more high-tech equity than most Tel Aviv startups.",
            "Take the Sherut, friend! Walking through the Negev at high noon is only recommended for wild camels.",
            "Is the Western Wall stone whispering ancient wisdom or just asking for a quick reboot?"
    );

    private EasterEggManager() {}

    public static boolean isOnCooldown(EasterEggCooldowns cooldowns, UUID playerId, long currentGameTime) {
        if (cooldowns == null || playerId == null) {
            return false;
        }
        Long last = cooldowns.getLastUseTime(playerId);
        return last != null && (currentGameTime - last) < COOLDOWN_TICKS;
    }

    public static void recordInteraction(EasterEggCooldowns cooldowns, UUID playerId, long time) {
        if (cooldowns != null) {
            cooldowns.setLastUseTime(playerId, time);
        }
    }

    public static void clearCooldown(EasterEggCooldowns cooldowns, UUID playerId) {
        if (cooldowns != null) {
            cooldowns.clearCooldown(playerId);
        }
    }

    /**
     * Attempts to trigger a random procedural easter-egg dialogue when conversing with a villager.
     */
    public static boolean triggerVillagerEasterEgg(ServerPlayer player, Villager villager) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        EasterEggCooldowns cooldowns = EasterEggCooldowns.get(serverLevel);
        long now = serverLevel.getGameTime();
        if (isOnCooldown(cooldowns, player.getUUID(), now)) {
            return false;
        }

        // 15% chance to trigger an easter egg line when interacting
        if (RNG.nextDouble() < 0.15) {
            recordInteraction(cooldowns, player.getUUID(), now);
            String line = ABSURD_DIALOGUES.get(RNG.nextInt(ABSURD_DIALOGUES.size()));
            player.sendSystemMessage(Component.literal("[Citizen] ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("\"" + line + "\"").withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE)));

            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    villager.getX(), villager.getY() + 1.8, villager.getZ(),
                    5, 0.3, 0.3, 0.3, 0.05);
            return true;
        }
        return false;
    }

    /**
     * Handles secret cat interaction: showing a Kippah to a cat grants purrs, hearts, and the secret advancement.
     */
    public static boolean interactWithCat(ServerPlayer player, Cat cat, ItemStack heldItem) {
        if (heldItem.is(ModItems.KIPPAH.get())) {
            if (!(player.level() instanceof ServerLevel serverLevel)) {
                return false;
            }
            EasterEggCooldowns cooldowns = EasterEggCooldowns.get(serverLevel);
            long now = serverLevel.getGameTime();
            if (isOnCooldown(cooldowns, player.getUUID(), now)) {
                return false;
            }
            recordInteraction(cooldowns, player.getUUID(), now);

            player.sendSystemMessage(Component.literal("The cat squints approvingly at your Kippah and purrs in holy serenity.")
                    .withStyle(ChatFormatting.AQUA));

            serverLevel.sendParticles(ParticleTypes.HEART,
                    cat.getX(), cat.getY() + 0.5, cat.getZ(),
                    8, 0.3, 0.3, 0.3, 0.05);
            serverLevel.playSound(null, cat.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.NEUTRAL, 1.0F, 1.2F);
            return true;
        }
        return false;
    }

    /**
     * Checks if a player holds the holy Levantine trio: Hummus, Falafel, and Sabich.
     */
    public static boolean isHummusConnoisseur(ServerPlayer player) {
        boolean hasHummus = player.getInventory().contains(new ItemStack(ModItems.HUMMUS.get()));
        boolean hasFalafel = player.getInventory().contains(new ItemStack(ModItems.FALAFEL.get()));
        boolean hasSabich = player.getInventory().contains(new ItemStack(ModItems.SABICH.get()));
        return hasHummus && hasFalafel && hasSabich;
    }

    public static List<String> getAbsurdDialogues() {
        return ABSURD_DIALOGUES;
    }
}
