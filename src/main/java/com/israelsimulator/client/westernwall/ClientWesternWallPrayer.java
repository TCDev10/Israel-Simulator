package com.israelsimulator.client.westernwall;

import com.israelsimulator.network.WesternWallPrayingPayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Client mirror of who is praying (entity id → wall pos). */
@OnlyIn(Dist.CLIENT)
public final class ClientWesternWallPrayer {
    public static final ContextKey<Boolean> PRAYING_KEY =
            new ContextKey<>(Identifier.fromNamespaceAndPath("israel_simulator", "western_wall_praying"));

    private static final Map<Integer, BlockPos> PRAYING = new ConcurrentHashMap<>();

    private ClientWesternWallPrayer() {}

    public static void handle(WesternWallPrayingPayload payload) {
        if (payload.praying()) {
            PRAYING.put(payload.entityId(), payload.wallPos());
        } else {
            PRAYING.remove(payload.entityId());
        }
    }

    public static boolean isPraying(int entityId) {
        return PRAYING.containsKey(entityId);
    }

    public static boolean isLocalPlayerPraying() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && isPraying(mc.player.getId());
    }

    public static void clear() {
        PRAYING.clear();
    }
}
