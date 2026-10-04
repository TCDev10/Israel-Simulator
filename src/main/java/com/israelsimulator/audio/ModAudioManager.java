package com.israelsimulator.audio;

import com.israelsimulator.registry.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Audio manager handling ambient city sounds, festival chimes, boss music, and audio events (GAME_DESIGN.md §45, TODO §46).
 */
public final class ModAudioManager {

    private ModAudioManager() {}

    /**
     * Plays a sound event at a specific position for all nearby players.
     */
    public static void playSoundAt(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (level == null || sound == null) return;
        level.playSound(null, pos, sound, source, volume, pitch);
    }

    /**
     * Plays a private notification sound for a single player (e.g. discovery toast, transit cue).
     */
    public static void playSoundForPlayer(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        if (player == null || sound == null) return;
        player.playSound(sound, volume, pitch);
    }

    /**
     * Plays regional city ambience based on city name.
     */
    public static void playCityAmbience(ServerLevel level, BlockPos pos, String cityName) {
        if (level == null || cityName == null) return;
        SoundEvent sound = switch (cityName.toLowerCase()) {
            case "tel_aviv" -> ModSoundEvents.TEL_AVIV_AMBIENT.get();
            case "jerusalem" -> ModSoundEvents.JERUSALEM_AMBIENT.get();
            case "jaffa" -> ModSoundEvents.JAFFA_AMBIENT.get();
            default -> null;
        };
        if (sound != null) {
            playSoundAt(level, pos, sound, SoundSource.AMBIENT, 0.8F, 1.0F);
        }
    }

    /**
     * Plays festival chime (e.g. Hanukkah or Shabbat lighting).
     */
    public static void playFestivalChime(Level level, BlockPos pos, boolean isHanukkah) {
        SoundEvent sound = isHanukkah
                ? ModSoundEvents.HANUKKAH_CHIME.get()
                : ModSoundEvents.SHABBAT_CANDLE.get();
        playSoundAt(level, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Plays public transit sound (horn, whistle, or departure).
     */
    public static void playTransitSound(Level level, BlockPos pos, String transportType) {
        if (level == null || transportType == null) return;
        SoundEvent sound = switch (transportType.toUpperCase()) {
            case "BUS" -> ModSoundEvents.BUS_HORN.get();
            case "TRAIN" -> ModSoundEvents.TRAIN_WHISTLE.get();
            case "BICYCLE" -> ModSoundEvents.BICYCLE_BELL.get();
            default -> ModSoundEvents.TRANSIT_TRAVEL.get();
        };
        playSoundAt(level, pos, sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    /**
     * Plays landmark discovery fanfare.
     */
    public static void playDiscoveryFanfare(ServerPlayer player) {
        playSoundForPlayer(player, ModSoundEvents.LANDMARK_DISCOVERED.get(), 1.0F, 1.0F);
    }
}
