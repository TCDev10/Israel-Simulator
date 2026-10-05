package com.israelsimulator.westernwall;

import com.israelsimulator.effect.BlessedEffect;
import com.israelsimulator.network.WesternWallPrayingPayload;
import com.israelsimulator.registry.ModItems;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Western Wall prayer: failures are immediate; success starts a {@link WesternWallPrayerSession}
 * that completes after {@link WesternWallPrayerSession#DURATION_TICKS} if the player stays put.
 */
public final class WesternWallManager {
    public static final long COOLDOWN_TICKS = 24000L;
    public static final int REWARD_DIAMONDS = 5;
    /** Cancel if the player moves farther than this from the start position (horizontal+vertical). */
    public static final double CANCEL_MOVE_DISTANCE = 0.5;

    public enum PrayerStatus {
        SUCCESS,
        MISSING_KIPPAH,
        MISSING_PRAYER_NOTE,
        COOLDOWN_ACTIVE,
        ALREADY_PRAYING
    }

    private static final Map<UUID, WesternWallPrayerSession> ACTIVE = new ConcurrentHashMap<>();

    private WesternWallManager() {}

    public static boolean enforcesCooldown(boolean instabuild) {
        return true;
    }

    public static PrayerStatus validatePrayer(boolean hasKippah, boolean hasPrayerNote, boolean onCooldown) {
        if (!hasKippah) {
            return PrayerStatus.MISSING_KIPPAH;
        }
        if (!hasPrayerNote) {
            return PrayerStatus.MISSING_PRAYER_NOTE;
        }
        if (onCooldown) {
            return PrayerStatus.COOLDOWN_ACTIVE;
        }
        return PrayerStatus.SUCCESS;
    }

    public static boolean isOnCooldown(WesternWallCooldowns cooldowns, UUID playerId, long currentGameTime) {
        if (cooldowns == null || playerId == null) {
            return false;
        }
        Long lastTime = cooldowns.getLastPrayerTime(playerId);
        if (lastTime == null) {
            return false;
        }
        return (currentGameTime - lastTime) < COOLDOWN_TICKS;
    }

    public static boolean isPraying(UUID playerId) {
        return playerId != null && ACTIVE.containsKey(playerId);
    }

    public static WesternWallPrayerSession getSession(UUID playerId) {
        return playerId == null ? null : ACTIVE.get(playerId);
    }

    /** Pure helper for tests: would the session complete at {@code now}? */
    public static boolean isComplete(WesternWallPrayerSession session, long now) {
        return session != null && (now - session.startGameTime()) >= WesternWallPrayerSession.DURATION_TICKS;
    }

    /** Pure helper for tests: cancel when displacement exceeds threshold. */
    public static boolean shouldCancelForMovement(WesternWallPrayerSession session, double x, double y, double z) {
        if (session == null) {
            return false;
        }
        double dx = x - session.startX();
        double dy = y - session.startY();
        double dz = z - session.startZ();
        return dx * dx + dy * dy + dz * dz > CANCEL_MOVE_DISTANCE * CANCEL_MOVE_DISTANCE;
    }

    /**
     * Starts a prayer session or sends an immediate failure message.
     * Does not complete the prayer; {@link #tickPlayer} does.
     */
    public static boolean tryPray(Player player, InteractionHand hand, BlockPos pos) {
        Level level = player.level();
        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean hasKippah = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.KIPPAH.get())
                || com.israelsimulator.item.cultural.KippahItem.isWearingKippah(player);
        boolean hasPrayerNote = held.is(ModItems.PRAYER_NOTE.get());

        if (level.isClientSide()) {
            // Client: accept the click so the interaction cancels; posing comes from the synced payload.
            return hasPrayerNote || hasKippah;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (ACTIVE.containsKey(player.getUUID())) {
            return true; // ignore extra clicks while praying
        }

        WesternWallCooldowns cooldowns = WesternWallCooldowns.get(serverLevel);
        boolean onCooldown = enforcesCooldown(player.getAbilities().instabuild)
                && isOnCooldown(cooldowns, player.getUUID(), level.getGameTime());

        PrayerStatus status = validatePrayer(hasKippah, hasPrayerNote, onCooldown);
        switch (status) {
            case MISSING_KIPPAH -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_need_kippah"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.9F, 1.0F);
                return true;
            }
            case MISSING_PRAYER_NOTE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_need_prayer_note"));
                return false;
            }
            case COOLDOWN_ACTIVE -> {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_cooldown"));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8F, 1.2F);
                return true;
            }
            case ALREADY_PRAYING -> {
                return true;
            }
            case SUCCESS -> {
                Vec3 p = player.position();
                WesternWallPrayerSession session = new WesternWallPrayerSession(
                        pos.immutable(), level.getGameTime(), p.x, p.y, p.z);
                ACTIVE.put(player.getUUID(), session);
                syncPraying(serverPlayer, true, pos);
                player.sendOverlayMessage(Component.translatable("message.israel_simulator.western_wall_praying"));
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.6F, 1.0F);
                return true;
            }
        }
        return false;
    }

    /** Per-player tick: cancel or complete active sessions. */
    public static void tickPlayer(ServerPlayer player) {
        WesternWallPrayerSession session = ACTIVE.get(player.getUUID());
        if (session == null) {
            return;
        }
        ServerLevel level = player.level();
        long now = level.getGameTime();

        ItemStack held = player.getMainHandItem();
        boolean stillHasNote = held.is(ModItems.PRAYER_NOTE.get()) || player.getAbilities().instabuild;
        boolean stillHasKippah = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.KIPPAH.get())
                || com.israelsimulator.item.cultural.KippahItem.isWearingKippah(player);

        if (!stillHasNote || !stillHasKippah || shouldCancelForMovement(session, player.getX(), player.getY(), player.getZ())) {
            cancel(player, false);
            return;
        }

        // Soft particles while praying
        if (now % 5 == 0) {
            BlockPos pos = session.wallPos();
            level.sendParticles(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    4, 0.3, 0.4, 0.3, 0.02);
        }
        player.sendOverlayMessage(Component.translatable("message.israel_simulator.western_wall_praying"));

        if (isComplete(session, now)) {
            complete(player, session);
        }
    }

    public static void cancel(ServerPlayer player, boolean silent) {
        WesternWallPrayerSession removed = ACTIVE.remove(player.getUUID());
        if (removed == null) {
            return;
        }
        syncPraying(player, false, removed.wallPos());
        if (!silent) {
            player.sendOverlayMessage(Component.translatable("message.israel_simulator.western_wall_prayer_interrupted"));
        }
    }

    private static void complete(ServerPlayer player, WesternWallPrayerSession session) {
        ACTIVE.remove(player.getUUID());
        syncPraying(player, false, session.wallPos());

        ServerLevel level = player.level();
        BlockPos pos = session.wallPos();
        ItemStack held = player.getMainHandItem();
        if (!player.getAbilities().instabuild && held.is(ModItems.PRAYER_NOTE.get())) {
            held.shrink(1);
        }

        WesternWallCooldowns cooldowns = WesternWallCooldowns.get(level);
        cooldowns.setLastPrayerTime(player.getUUID(), level.getGameTime());

        giveOrDrop(player, new ItemStack(Items.DIAMOND, REWARD_DIAMONDS));
        BlessedEffect.applyTo(player);

        level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.3F);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                30, 0.5, 0.5, 0.5, 0.15);
        level.sendParticles(ParticleTypes.ENCHANT,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                40, 0.6, 0.6, 0.6, 0.2);

        player.sendSystemMessage(Component.translatable("message.israel_simulator.western_wall_blessed"));
        player.sendOverlayMessage(Component.empty());
    }

    private static void syncPraying(ServerPlayer player, boolean praying, BlockPos wallPos) {
        WesternWallPrayingPayload payload = new WesternWallPrayingPayload(player.getId(), praying, wallPos);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    /** Test-only: clear sessions between unit tests. */
    public static void clearActiveForTests() {
        ACTIVE.clear();
    }

    /** Test-only: inject a session without networking. */
    public static void putSessionForTests(UUID id, WesternWallPrayerSession session) {
        ACTIVE.put(id, session);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
