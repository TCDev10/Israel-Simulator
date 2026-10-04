package com.israelsimulator.religion;

import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.registry.ModItems;
import com.israelsimulator.world.biome.ModBiomes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Synagogue and community spiritual interactions (GAME_DESIGN.md §22, §29).
 * Grants blessings (Blessed effect) for prayer notes, tzedakah donations, or wearing a Kippah.
 */
public final class RuralSynagogueTrades {
    private RuralSynagogueTrades() {}

    /**
     * Rural synagogues belong to the agricultural countryside
     * ({@link ModBiomes#ISRAELI_AGRICULTURE}), not to every villager.
     */
    public static boolean acceptsBiome(Identifier biomeId) {
        return biomeId != null && ModBiomes.ISRAELI_AGRICULTURE.identifier().equals(biomeId);
    }

    public static boolean tryInteract(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        Identifier biomeId = player.level().getBiome(villager.blockPosition())
                .unwrapKey()
                .map(ResourceKey::identifier)
                .orElse(null);
        if (!acceptsBiome(biomeId)) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);

        // Prayer Note Offering
        if (held.is(ModItems.PRAYER_NOTE.get())) {
            held.shrink(1);
            applyRabbiBlessing(player, villager);
            return true;
        }

        // Tzedakah (Charity donation of 1 Shekel)
        if (held.is(ModItems.SHEKEL.get()) && !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            held.shrink(1);
            applyRabbiBlessing(player, villager);
            return true;
        }

        return false;
    }

    private static void applyRabbiBlessing(Player player, AbstractVillager villager) {
        BlessedEffect.applyTo(player);

        player.level().playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.2F);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.2, player.getZ(),
                    15, 0.4, 0.4, 0.4, 0.1);
        }
    }
}
