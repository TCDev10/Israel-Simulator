package com.israelsimulator.audio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MusicAndAudioTest {

    @Test
    @DisplayName("Verify ModSoundEvents class and fields exist")
    void testSoundEventClassAndFields() throws Exception {
        ClassLoader cl = MusicAndAudioTest.class.getClassLoader();
        Class<?> soundClass = Class.forName("com.israelsimulator.registry.ModSoundEvents", false, cl);
        assertNotNull(soundClass, "ModSoundEvents class must exist");

        assertNotNull(soundClass.getField("HAVA_NAGILA"));
        assertNotNull(soundClass.getField("KLEZMER"));
        assertNotNull(soundClass.getField("SHABBAT_SHALOM"));
        assertNotNull(soundClass.getField("TEL_AVIV_AMBIENT"));
        assertNotNull(soundClass.getField("JERUSALEM_AMBIENT"));
        assertNotNull(soundClass.getField("JAFFA_AMBIENT"));
        assertNotNull(soundClass.getField("BIBI_THEME"));
        assertNotNull(soundClass.getField("BICYCLE_BELL"));
        assertNotNull(soundClass.getField("BUS_HORN"));
        assertNotNull(soundClass.getField("TRAIN_WHISTLE"));
        assertNotNull(soundClass.getField("TRANSIT_TRAVEL"));
        assertNotNull(soundClass.getField("LANDMARK_DISCOVERED"));
    }

    @Test
    @DisplayName("Verify sounds.json contains all registered audio mappings")
    void testSoundsJsonIntegrity() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/sounds.json");
        assertNotNull(stream, "assets/israel_simulator/sounds.json must exist in classpath");

        String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(content.contains("music_disc.hava_nagila"), "sounds.json must define hava_nagila");
        assertTrue(content.contains("music.cultural.klezmer"), "sounds.json must define klezmer");
        assertTrue(content.contains("music.cultural.shabbat_shalom"), "sounds.json must define shabbat_shalom");
        assertTrue(content.contains("ambient.city.tel_aviv"), "sounds.json must define tel_aviv ambience");
        assertTrue(content.contains("ambient.city.jerusalem"), "sounds.json must define jerusalem ambience");
        assertTrue(content.contains("ambient.city.jaffa"), "sounds.json must define jaffa ambience");
        assertTrue(content.contains("music.boss.bibi_theme"), "sounds.json must define bibi_theme");
        assertTrue(content.contains("entity.bicycle.bell"), "sounds.json must define bicycle bell");
        assertTrue(content.contains("entity.bus.horn"), "sounds.json must define bus horn");
        assertTrue(content.contains("entity.train.whistle"), "sounds.json must define train whistle");
        assertTrue(content.contains("entity.transport.travel"), "sounds.json must define transit travel");
        assertTrue(content.contains("ui.landmark_discovered"), "sounds.json must define landmark discovered");
    }
}
