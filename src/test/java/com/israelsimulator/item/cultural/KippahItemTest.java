package com.israelsimulator.item.cultural;

import com.israelsimulator.westernwall.WesternWallManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KippahItemTest {

    @Test
    @DisplayName("Verify KippahItem class exists and extends Item")
    void testKippahItemClassStructure() throws Exception {
        ClassLoader cl = KippahItemTest.class.getClassLoader();
        Class<?> kippahClass = Class.forName("com.israelsimulator.item.cultural.KippahItem", false, cl);
        assertNotNull(kippahClass);
        assertEquals("net.minecraft.world.item.Item", kippahClass.getSuperclass().getName());
    }

    @Test
    @DisplayName("Verify Kippah equipment asset constant and key")
    void testKippahEquipmentAssetKey() {
        assertNotNull(CulturalItems.KIPPAH_ASSET);
        assertEquals("israel_simulator:kippah", CulturalItems.KIPPAH_ASSET.identifier().toString());
    }

    @Test
    @DisplayName("Verify equipment JSON definition exists and points to kippah layer")
    void testEquipmentAssetJson() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/equipment/kippah.json");
        assertNotNull(stream, "equipment/kippah.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:kippah"), "Equipment asset must define kippah layer texture");
            assertTrue(json.contains("humanoid"), "Equipment asset must support humanoid layer");
        } catch (Exception e) {
            fail("Failed reading equipment asset JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify 3D model JSON exists and has display configurations")
    void testKippah3DModelJson() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/models/item/kippah.json");
        assertNotNull(stream, "models/item/kippah.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("elements"), "3D model must contain model elements");
            assertTrue(json.contains("thirdperson_righthand"), "3D model must contain third person display config");
            assertTrue(json.contains("firstperson_righthand"), "3D model must contain first person display config");
            assertTrue(json.contains("gui"), "3D model must contain GUI display config");
            assertTrue(json.contains("head"), "3D model must contain head display config");
        } catch (Exception e) {
            fail("Failed reading 3D model JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify humanoid equipment texture exists on classpath")
    void testKippahEquipmentTexturePresent() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/equipment/humanoid/kippah.png");
        assertNotNull(stream, "textures/entity/equipment/humanoid/kippah.png must exist");
    }

    @Test
    @DisplayName("Verify localization keys exist for Kippah, description, and requirement")
    void testKippahLocalization() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "lang/en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"item.israel_simulator.kippah\""), "en_us.json must have item.israel_simulator.kippah");
            assertTrue(json.contains("\"item.israel_simulator.kippah.desc\""), "en_us.json must have item.israel_simulator.kippah.desc");
            assertTrue(json.contains("\"item.israel_simulator.kippah.requirement\""), "en_us.json must have item.israel_simulator.kippah.requirement");
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Kippah cultural requirement in Western Wall prayer validation")
    void testKippahCulturalRequirement() {
        // Without kippah
        WesternWallManager.PrayerStatus statusWithoutKippah =
                WesternWallManager.validatePrayer(false, true, false);
        assertEquals(WesternWallManager.PrayerStatus.MISSING_KIPPAH, statusWithoutKippah);

        // With kippah
        WesternWallManager.PrayerStatus statusWithKippah =
                WesternWallManager.validatePrayer(true, true, false);
        assertEquals(WesternWallManager.PrayerStatus.SUCCESS, statusWithKippah);
    }
}
