package com.israelsimulator.easteregg;

import com.israelsimulator.registry.ModItems;
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

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages satirical and humorous easter eggs, absurd events, and secret interactions (GAME_DESIGN.md §50).
 *
 * <p>Safeguards enforce:
 * <ul>
 *   <li>No progression breaks: Easter eggs never award overpowered endgame gear.</li>
 *   <li>Anti-spam: Minimum cooldown between easter egg interactions per player.</li>
 *   <li>Server authority: All triggers and particle/sound broadcasts validated on the server.</li>
 * </ul>
 * </p>
 */
public final class EasterEggManager {
    private static final Random RNG = new Random();
    private static final Map<UUID, Long> LAST_EASTER_EGG_INTERACTION = new ConcurrentHashMap<>();
    private static final long COOLDOWN_TICKS = 100L; // 5 seconds

    private static final List<String> ABSURD_DIALOGUES = List.of(
            "Did you just put tahini on chocolate cake?! What kind of Silicon Alley accelerator taught you that?!",
            "My cousin in Herzliya is coding a smart drone to deliver piping-hot Shakshuka directly to your balcony.",
            "If you spin the dreidel and get Shin four times in a row, that's not bad luck—that's an unhandled exception.",
            "The street cat behind the falafel stand has more high-tech equity than most Tel Aviv startups.",
            "Take the Sherut, friend! Walking through the Negev at high noon is only recommended for wild camels.",
            "Is the Western Wall stone whispering ancient wisdom or just asking for a quick reboot?"
    );

    private EasterEggManager() {}

    /**
     * Attempts to trigger a random procedural easter-egg dialogue when conversing with a villager.
     */
    public static boolean triggerVillagerEasterEgg(ServerPlayer player, Villager villager) {
        long now = player.level().getGameTime();
        Long last = LAST_EASTER_EGG_INTERACTION.get(player.getUUID());
        if (last != null && now - last < COOLDOWN_TICKS) {
            return false;
        }

        // 15% chance to trigger an easter egg line when interacting
        if (RNG.nextDouble() < 0.15) {
            LAST_EASTER_EGG_INTERACTION.put(player.getUUID(), now);
            String line = ABSURD_DIALOGUES.get(RNG.nextInt(ABSURD_DIALOGUES.size()));
            player.sendSystemMessage(Component.literal("[Citizen] ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("\"" + line + "\"").withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE)));

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        villager.getX(), villager.getY() + 1.8, villager.getZ(),
                        5, 0.3, 0.3, 0.3, 0.05);
            }
            return true;
        }
        return false;
    }

    /**
     * Handles secret cat interaction: showing a Kippah to a cat grants purrs, hearts, and the secret advancement.
     */
    public static boolean interactWithCat(ServerPlayer player, Cat cat, ItemStack heldItem) {
        if (heldItem.is(ModItems.KIPPAH.get())) {
            long now = player.level().getGameTime();
            Long last = LAST_EASTER_EGG_INTERACTION.get(player.getUUID());
            if (last != null && now - last < COOLDOWN_TICKS) {
                return false;
            }
            LAST_EASTER_EGG_INTERACTION.put(player.getUUID(), now);

            player.sendSystemMessage(Component.literal("The cat squints approvingly at your Kippah and purrs in holy serenity.")
                    .withStyle(ChatFormatting.AQUA));

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        cat.getX(), cat.getY() + 0.5, cat.getZ(),
                        8, 0.3, 0.3, 0.3, 0.05);
                serverLevel.playSound(null, cat.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.NEUTRAL, 1.0F, 1.2F);
            }
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
