package com.israelsimulator.festival;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;

/**
 * Shabbat rest. Players who neither break or place blocks nor attack for {@link #REST_TICKS}
 * get a small rest bonus (Regeneration and a bite of Saturation) every {@link #BONUS_EVERY}
 * ticks; doing any of those "work" actions on Shabbat pauses it. Villagers rest too: they move
 * slowly. Shops close elsewhere (Shuk vendors, observant NPCs).
 */
public final class ShabbatRest {
    public static final int REST_TICKS = 1200;
    public static final int BONUS_EVERY = 200;
    public static final int REGEN_TICKS = 100;
    public static final int VILLAGER_SLOW_TICKS = 120;

    private static final Map<UUID, Long> LAST_WORK = new HashMap<>();

    private ShabbatRest() {}

    /** True when the player has rested long enough since the last work action. */
    public static boolean rested(long lastWork, long now) {
        return now - lastWork >= REST_TICKS;
    }

    public static boolean isShabbat(long gameTime) {
        return FestivalManager.getCurrentFestival(gameTime) == FestivalType.SHABBAT;
    }

    public static void reset() {
        LAST_WORK.clear();
    }

    /** A player broke/placed a block or attacked: no rest bonus for a while. */
    public static void onWork(Entity entity) {
        if (!(entity instanceof ServerPlayer player) || player.isCreative() || player.isSpectator()) return;
        long now = player.level().getGameTime();
        if (!isShabbat(now)) return;
        Long last = LAST_WORK.put(player.getUUID(), now);
        if (last == null || rested(last, now)) {
            player.sendSystemMessage(Component.translatable("message.israel_simulator.shabbat.work")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    /** Overworld tick: rest bonus for resting players. */
    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        if (now % BONUS_EVERY != 0) return;
        if (!isShabbat(now)) {
            if (!LAST_WORK.isEmpty()) LAST_WORK.clear();
            return;
        }
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            long last = LAST_WORK.getOrDefault(p.getUUID(), Long.MIN_VALUE / 2);
            if (!rested(last, now)) continue;
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0, true, false, false));
            com.israelsimulator.advancement.IsraelAdvancements.award(p, com.israelsimulator.advancement.AdvancementEvents.SHABBAT_REST);
            p.sendOverlayMessage(Component.translatable("message.israel_simulator.shabbat.rest")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    /** Villagers rest on Shabbat: they stroll slowly. */
    public static void tickVillager(AbstractVillager villager) {
        if (villager.tickCount % 100 != 0 || !isShabbat(villager.level().getGameTime())) return;
        villager.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, VILLAGER_SLOW_TICKS, 1, true, false, false));
    }

}
