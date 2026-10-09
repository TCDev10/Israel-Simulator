package com.israelsimulator.client.music;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.BibiBossEntity;
import com.israelsimulator.entity.boss.BibiBossMusicRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;

/**
 * Starts the Bibi boss song for the local player when a living Bibi is within
 * {@link BibiBossMusicRules#MUSIC_RADIUS} blocks, and keeps vanilla music silent while it plays.
 * Purely client-side: Bibi is already synced to every tracking client, so no payload is needed.
 */
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID, value = Dist.CLIENT)
public final class BibiBossMusicClientEvents {
    private static BibiBossMusicInstance current;
    private static int scanCooldown;

    private BibiBossMusicClientEvents() {}

    static boolean isBossMusicPlaying() {
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
            if (entity instanceof BibiBossEntity boss
                    && BibiBossMusicRules.shouldPlay(boss.isAlive(), boss.isRemoved(), boss.distanceToSqr(mc.player))) {
                if (current != null) {
                    mc.getSoundManager().stop(current);
                }
                mc.getMusicManager().stopPlaying();
                current = new BibiBossMusicInstance(boss);
                mc.getSoundManager().play(current);
                return;
            }
        }
    }

    /** No vanilla/situational music while the boss song plays. */
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
