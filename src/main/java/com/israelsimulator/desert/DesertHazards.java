package com.israelsimulator.desert;

import com.israelsimulator.registry.ModItems;
import com.israelsimulator.world.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Server-authoritative environmental hazards for the Judean Desert & Negev (GAME_DESIGN.md §21).
 * Features daytime sun exposure and heat exhaustion mechanics mitigable by headwear (e.g. Kippah).
 */
public final class DesertHazards {
    private DesertHazards() {}

    /**
     * Determines whether heat exposure condition is active based on environmental parameters.
     */
    public static boolean isVulnerableToHeat(boolean isDay, boolean canSeeSky, boolean hasHeadgear) {
        return isDay && canSeeSky && !hasHeadgear;
    }

    /**
     * Checks if player has sun protection, such as wearing a Kippah or helmet, or being sheltered.
     */
    public static boolean isProtectedFromSun(Player player) {
        ItemStack headgear = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!headgear.isEmpty()) {
            return true;
        }

        Level level = player.level();
        BlockPos pos = player.blockPosition();
        return !level.canSeeSky(pos);
    }

    /**
     * Applies arid heat exhaustion to players exploring the desert without sun protection.
     */
    public static boolean handleDesertTick(Player player) {
        Level level = player.level();
        if (level.isClientSide()) {
            return false;
        }

        BlockPos pos = player.blockPosition();
        if (!level.getBiome(pos).is(ModBiomes.JUDEAN_DESERT)) {
            return false;
        }

        boolean isDay = level.getSkyDarken() < 4;
        if (isVulnerableToHeat(isDay, level.canSeeSky(pos), !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty())) {
            // Apply gradual sun exhaustion every 5 seconds (100 ticks)
            if (player.tickCount % 100 == 0) {
                player.causeFoodExhaustion(0.6F);

                if (player.getFoodData().getFoodLevel() <= 6) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 0, false, false, true));
                }
                return true;
            }
        }

        return false;
    }
}
