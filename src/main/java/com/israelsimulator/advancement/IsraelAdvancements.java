package com.israelsimulator.advancement;

import com.israelsimulator.festival.FestivalManager;
import com.israelsimulator.festival.FestivalType;
import com.israelsimulator.registry.ModTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Fires {@code israel_simulator:mod_event} criteria from gameplay code. Server side only; safe anywhere. */
public final class IsraelAdvancements {
    private IsraelAdvancements() {}

    public static void award(Entity entity, String event) {
        if (!(entity instanceof ServerPlayer player) || !ModTriggers.EVENT.isBound()) {
            return;
        }
        ModTriggers.EVENT.get().trigger(player, event);
    }

    /** Once a second per player: festivals (and Shabbat) the player is around for. */
    public static void tickPlayer(ServerPlayer player) {
        FestivalType festival = FestivalManager.getCurrentFestival(player.level().getGameTime());
        if (festival != null) {
            award(player, AdvancementEvents.festival(festival.getId()));
        }
    }
}
