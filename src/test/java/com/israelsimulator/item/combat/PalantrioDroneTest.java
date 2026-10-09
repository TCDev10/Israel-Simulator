package com.israelsimulator.item.combat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.israelsimulator.entity.projectile.PalantrioDroneEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Palantrio Drone: recipe, name, behaviour constants, target rules, model and config defaults. */
class PalantrioDroneTest {
    private static JsonObject json(String p) throws Exception {
        return JsonParser.parseString(Files.readString(Path.of(p))).getAsJsonObject();
    }

    @Test
    @DisplayName("Name is exactly 'Palantrio Drone' in English and Italian")
    void names() throws Exception {
        for (String lang : new String[] {"en_us", "it_it"}) {
            assertEquals("Palantrio Drone", json("src/main/resources/assets/israel_simulator/lang/" + lang + ".json")
                    .get("item.israel_simulator.palantrio_drone").getAsString());
        }
    }

    @Test
    @DisplayName("Crafted from drone parts, redstone and iron")
    void recipe() throws Exception {
        assertTrue(Files.exists(Path.of("src/main/resources/assets/israel_simulator/items/drone_part.json")), "drone_part item exists");
        String r = Files.readString(Path.of("src/main/resources/data/israel_simulator/recipe/palantrio_drone.json"));
        assertTrue(r.contains("israel_simulator:drone_part") && r.contains("minecraft:redstone") && r.contains("minecraft:iron_ingot"));
        assertTrue(r.contains("israel_simulator:palantrio_drone"));
    }

    @Test
    @DisplayName("Seeks within ~32 blocks, expires after ~20 s, 5 s item cooldown")
    void constants() {
        assertEquals(32.0, PalantrioDroneEntity.SEEK_RANGE);
        assertEquals(400, PalantrioDroneEntity.LIFETIME_TICKS);
        assertEquals(100, PalantrioDroneItem.COOLDOWN_TICKS);
    }

    @Test
    @DisplayName("Targets only hostile mobs, never its owner, tamed animals or other drones; no block damage by default")
    void targetRulesAndConfig() throws Exception {
        String src = Files.readString(Path.of("src/main/java/com/israelsimulator/entity/projectile/PalantrioDroneEntity.java"));
        assertTrue(src.contains("instanceof Enemy"));
        assertTrue(src.contains("OwnableEntity") && src.contains("ownerId.equals"));
        assertTrue(src.contains("ExplosionInteraction.NONE"));
        String cfg = Files.readString(Path.of("src/main/java/com/israelsimulator/config/IsraelSimulatorConfig.java"));
        assertTrue(cfg.contains("define(\"droneBreaksBlocks\", false)"));
        String item = Files.readString(Path.of("src/main/java/com/israelsimulator/item/combat/PalantrioDroneItem.java"));
        assertTrue(item.contains("stack.consume(1, player)") && item.contains("addCooldown(stack, COOLDOWN_TICKS)"));
    }

    @Test
    @DisplayName("Quadcopter model has four rotors and a texture")
    void model() throws Exception {
        String model = Files.readString(Path.of("src/main/java/com/israelsimulator/client/renderer/PalantrioDroneModel.java"));
        for (String r : new String[] {"rotor_fl", "rotor_fr", "rotor_bl", "rotor_br"}) {
            assertTrue(model.contains("\"" + r + "\""), r);
        }
        assertTrue(Files.exists(Path.of("src/main/resources/assets/israel_simulator/textures/entity/palantrio_drone.png")));
        assertTrue(Files.exists(Path.of("src/main/resources/assets/israel_simulator/textures/item/palantrio_drone.png")));
    }
}
