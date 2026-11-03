package com.israelsimulator.audio;

import com.israelsimulator.entity.npc.KirkBossMusicRules;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class KirkBossMusicTest {
    @Test
    void playsOnlyNearLivingKirk() {
        assertTrue(KirkBossMusicRules.shouldPlay(true, false, 63 * 63));
        assertTrue(KirkBossMusicRules.shouldPlay(true, false, 64 * 64));
        assertFalse(KirkBossMusicRules.shouldPlay(true, false, 65 * 65));
        assertFalse(KirkBossMusicRules.shouldPlay(false, false, 1));
        assertFalse(KirkBossMusicRules.shouldPlay(true, true, 1));
    }

    @Test
    void fadeOutReachesZero() {
        assertEquals(1.0F, KirkBossMusicRules.fadedVolume(0));
        assertEquals(0.0F, KirkBossMusicRules.fadedVolume(KirkBossMusicRules.FADE_TICKS));
    }

    @Test
    void oratorHasOwnStreamedTheme() throws Exception {
        String json = Files.readString(Path.of("src/main/resources/assets/israel_simulator/sounds.json"));
        int i = json.indexOf("\"music.boss.orator_theme\"");
        assertTrue(i != -1, "music.boss.orator_theme must be in sounds.json");
        String block = json.substring(i, json.indexOf("}", i) + 1);
        assertTrue(block.contains("israel_simulator:music/orator_boss_theme"));
        assertTrue(block.contains("\"stream\": true"));
        assertFalse(json.contains("music/kirk_boss_theme"), "the old copy of Bibi's theme must be gone");
        String inst = Files.readString(Path.of("src/main/java/com/israelsimulator/client/music/KirkBossMusicInstance.java"));
        assertTrue(inst.contains("ModSoundEvents.ORATOR_THEME"), "the Orator must not reuse Bibi's theme");
        assertFalse(inst.contains("BIBI_THEME"));
        // The Ogg is optional (owner adds it); if present it must be a real Ogg.
        Path ogg = Path.of("src/main/resources/assets/israel_simulator/sounds/music/orator_boss_theme.ogg");
        if (Files.exists(ogg)) {
            byte[] head = Files.readAllBytes(ogg);
            assertEquals("OggS", new String(head, 0, 4));
        }
    }

    @Test
    void missingOggDoesNotSilenceVanillaMusicForever() throws Exception {
        String ev = Files.readString(Path.of("src/main/java/com/israelsimulator/client/music/KirkBossMusicClientEvents.java"));
        assertTrue(ev.contains("getSoundManager().isActive(current)"));
        assertTrue(ev.contains("current = null;"));
    }

    @Test
    void clientPlaysInMusicCategoryAndSilencesVanillaMusic() throws Exception {
        String inst = Files.readString(Path.of("src/main/java/com/israelsimulator/client/music/KirkBossMusicInstance.java"));
        assertTrue(inst.contains("extends AbstractTickableSoundInstance"));
        assertTrue(inst.contains("SoundSource.MUSIC"));
        String ev = Files.readString(Path.of("src/main/java/com/israelsimulator/client/music/KirkBossMusicClientEvents.java"));
        assertTrue(ev.contains("SelectMusicEvent") && ev.contains("setMusic(null)"));
        assertTrue(ev.contains("getMusicManager().stopPlaying()"));
    }
}

