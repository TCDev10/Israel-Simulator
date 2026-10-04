package com.israelsimulator.block;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenorahBlockTest {

    @Test
    @DisplayName("Verify MenorahBlock class exists and extends Block")
    void testMenorahBlockClassStructure() throws Exception {
        ClassLoader cl = MenorahBlockTest.class.getClassLoader();
        Class<?> menorahClass = Class.forName("com.israelsimulator.block.MenorahBlock", false, cl);
        assertNotNull(menorahClass, "MenorahBlock class must exist");
        assertEquals("net.minecraft.world.level.block.Block", menorahClass.getSuperclass().getName(),
                "MenorahBlock must inherit from net.minecraft.world.level.block.Block");
    }

    @Test
    @DisplayName("Verify Menorah blockstate definition exists and points to block model")
    void testMenorahBlockstateJson() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/blockstates/menorah.json");
        assertNotNull(stream, "blockstates/menorah.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:block/menorah"), "Blockstate must reference menorah model");
        } catch (Exception e) {
            fail("Failed reading menorah blockstate JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Menorah 3D block model JSON exists with elements and texture mapping")
    void testMenorah3DBlockModelJson() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/models/block/menorah.json");
        assertNotNull(stream, "models/block/menorah.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("elements"), "3D model must contain elements");
            assertTrue(json.contains("israel_simulator:block/menorah"), "3D model must reference block texture");
            assertTrue(json.contains("base"), "Model must contain base element");
            assertTrue(json.contains("stem"), "Model must contain stem element");
            assertTrue(json.contains("crossbar"), "Model must contain crossbar element");
            assertTrue(json.contains("branch_tips"), "Model must contain branch tips element");
        } catch (Exception e) {
            fail("Failed reading menorah 3D model JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Menorah item model JSON exists and references block model")
    void testMenorahItemModelJson() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/models/item/menorah.json");
        assertNotNull(stream, "models/item/menorah.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:block/menorah"), "Item model must inherit from menorah block model");
        } catch (Exception e) {
            fail("Failed reading menorah item model JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Menorah block texture file exists and is non-empty")
    void testMenorahTextureFile() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/textures/block/menorah.png");
        assertNotNull(stream, "textures/block/menorah.png must exist");
        try {
            byte[] bytes = stream.readAllBytes();
            assertTrue(bytes.length > 0, "Texture file must not be empty");
        } catch (Exception e) {
            fail("Failed reading menorah texture: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Menorah block loot table exists and drops menorah item")
    void testMenorahLootTable() {
        InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/loot_table/blocks/menorah.json");
        assertNotNull(stream, "loot_table/blocks/menorah.json must exist in data");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:menorah"), "Loot table must drop menorah item");
            assertTrue(json.contains("minecraft:survives_explosion"), "Loot table must include survives_explosion condition");
        } catch (Exception e) {
            fail("Failed reading menorah loot table JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Menorah localization keys are present in en_us.json")
    void testMenorahLocalization() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"block.israel_simulator.menorah\""), "Missing Menorah block translation key");
            assertTrue(json.contains("\"message.israel_simulator.menorah_lit\""), "Missing menorah lit message key");
            assertTrue(json.contains("\"message.israel_simulator.menorah_fully_lit\""), "Missing menorah fully lit message key");
            assertTrue(json.contains("\"message.israel_simulator.menorah_extinguished\""), "Missing menorah extinguished message key");
            assertTrue(json.contains("\"message.israel_simulator.menorah_status\""), "Missing menorah status message key");
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }
}

