package com.israelsimulator.audio;

import com.israelsimulator.entity.boss.BibiBossMusicRules;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BibiBossMusicTest {
    @Test
    void playsOnlyNearLivingBoss() {
        assertTrue(BibiBossMusicRules.shouldPlay(true, false, 63 * 63));
        assertTrue(BibiBossMusicRules.shouldPlay(true, false, 64 * 64));
        assertFalse(BibiBossMusicRules.shouldPlay(true, false, 65 * 65));
        assertFalse(BibiBossMusicRules.shouldPlay(false, false, 1));
        assertFalse(BibiBossMusicRules.shouldPlay(true, true, 1));
    }

    @Test
    void fadeOutReachesZero() {
        assertEquals(1.0F, BibiBossMusicRules.fadedVolume(0));
        assertEquals(0.0F, BibiBossMusicRules.fadedVolume(BibiBossMusicRules.FADE_TICKS));
    }

    @Test
    void soundEventStreamsBundledOgg() throws Exception {
        String json = Files.readString(Path.of("src/main/resources/assets/israel_simulator/sounds.json"));
        int i = json.indexOf("\"music.boss.bibi_theme\"");
        String block = json.substring(i, json.indexOf("}", i) + 1);
        assertTrue(block.contains("israel_simulator:music/bibi_boss_theme"));
        assertTrue(block.contains("\"stream\": true"));
        Path ogg = Path.of("src/main/resources/assets/israel_simulator/sounds/music/bibi_boss_theme.ogg");
        assertTrue(Files.exists(ogg));
        byte[] head = Files.readAllBytes(ogg);
        assertEquals("OggS", new String(head, 0, 4));
    }

    @Test
    void clientLoopsInMusicCategoryAndSilencesVanillaMusic() throws Exception {
        String inst = Files.readString(Path.of("src/main/java/com/israelsimulator/client/music/BibiBossMusicInstance.java"));
        assertTrue(inst.contains("extends AbstractTickableSoundInstance"));
        assertTrue(inst.contains("SoundSource.MUSIC"));
        assertTrue(inst.contains("this.looping = true"));
        String ev = Files.readString(Path.of("src/main/java/com/israelsimulator/client/music/BibiBossMusicClientEvents.java"));
        assertTrue(ev.contains("SelectMusicEvent") && ev.contains("setMusic(null)"));
        assertTrue(ev.contains("getMusicManager().stopPlaying()"));
    }
}
