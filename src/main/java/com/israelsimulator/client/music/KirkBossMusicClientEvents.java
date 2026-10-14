package com.israelsimulator.client.music;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.npc.KirkBossMusicRules;
import com.israelsimulator.entity.npc.OratorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;

/**
 * Starts the Kirk boss song when Charlie Kirk spawns/is nearby, and silences vanilla music until he dies.
 */
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID, value = Dist.CLIENT)
public final class KirkBossMusicClientEvents {
    private static KirkBossMusicInstance current;
    private static int scanCooldown;

    private KirkBossMusicClientEvents() {}

    public static boolean isBossMusicPlaying() {
        return current != null && !current.isStopped();
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stop(mc);
            return;
        }
        if (current != null && (current.isStopped() || !mc.getSoundManager().isActive(current)) && current.isFading()) {
            current = null;
        }
        if (current != null && !current.isFading()) {
            return;
        }
        if (--scanCooldown > 0) {
            return;
        }
        scanCooldown = 10;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof OratorEntity orator
                    && KirkBossMusicRules.shouldPlay(orator.isAlive(), orator.isRemoved(), orator.distanceToSqr(mc.player))) {
                if (current != null) {
                    mc.getSoundManager().stop(current);
                }
                mc.getMusicManager().stopPlaying();
                current = new KirkBossMusicInstance(orator);
                mc.getSoundManager().play(current);
                return;
            }
        }
    }

    @SubscribeEvent
    static void onSelectMusic(SelectMusicEvent event) {
        if (isBossMusicPlaying()) {
            event.setMusic(null);
        }
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        stop(Minecraft.getInstance());
    }

    private static void stop(Minecraft mc) {
        if (current != null) {
            mc.getSoundManager().stop(current);
            current = null;
        }
    }
}

