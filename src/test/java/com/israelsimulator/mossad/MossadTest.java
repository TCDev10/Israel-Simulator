package com.israelsimulator.mossad;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Mossad faction (fictional): agents, handler missions, reputation, safehouse and assets. */
class MossadTest {
    private static final Path RES = Path.of("src/main/resources");
    private static final Path ASSETS = RES.resolve("assets/israel_simulator");
    private static final Path DATA = RES.resolve("data/israel_simulator");

    private static JsonObject json(Path p) throws Exception {
        return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
    }

    @Test
    @DisplayName("Reputation makes hostile agents rarer and trusted players are left alone")
    void reputationRules() {
        assertEquals(1.0, MossadRules.spawnMultiplier(0));
        assertEquals(1.0, MossadRules.spawnMultiplier(-50));
        assertEquals(0.5, MossadRules.spawnMultiplier(50), 1e-9);
        assertEquals(0.1, MossadRules.spawnMultiplier(100), 1e-9);
        assertTrue(MossadRules.spawnChance(80) < MossadRules.spawnChance(20));
        assertFalse(MossadRules.isTrusted(59));
        assertTrue(MossadRules.isTrusted(60));
        assertEquals(100, MossadRules.clampReputation(250));
        assertEquals(-100, MossadRules.clampReputation(-250));
    }

    @Test
    @DisplayName("Three missions with growing rewards")
    void missions() {
        assertEquals(3, MossadMission.values().length);
        assertSame(MossadMission.ELIMINATE, MossadMission.byId("eliminate"));
        assertSame(MossadMission.RETRIEVE, MossadMission.byId("retrieve"));
        assertSame(MossadMission.ESCORT, MossadMission.byId("escort"));
        assertNull(MossadMission.byId("nope"));
        assertEquals(3, MossadMission.ELIMINATE.goal());
        assertTrue(MossadMission.ESCORT.shekelReward() > MossadMission.ELIMINATE.shekelReward());
        assertTrue(MossadMission.ESCORT.reputationReward() > MossadMission.ELIMINATE.reputationReward());
    }

    @Test
    @DisplayName("Per-player reputation is stored and clamped")
    void dataStore() {
        MossadData data = new MossadData();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        assertEquals(0, data.reputation(a));
        assertEquals(30, data.adjustReputation(a, 30));
        assertEquals(100, data.adjustReputation(a, 500));
        assertEquals(0, data.reputation(b));
        MossadData.Entry e = data.entry(b);
        e.mission = "escort";
        assertSame(MossadMission.ESCORT, e.activeMission());
        e.clearMission();
        assertNull(e.activeMission());
    }

    @Test
    @DisplayName("Mission texts, entity names and items are translated in English and Italian")
    void translations() throws Exception {
        JsonObject en = json(ASSETS.resolve("lang/en_us.json"));
        JsonObject it = json(ASSETS.resolve("lang/it_it.json"));
        for (MossadMission m : MossadMission.values()) {
            for (String k : new String[] {m.translationKey(), m.translationKey() + ".desc"}) {
                assertTrue(en.has(k) && it.has(k), k);
            }
        }
        for (String k : new String[] {"entity.israel_simulator.mossad_agent", "entity.israel_simulator.mossad_handler",
                "entity.israel_simulator.mossad_informant", "item.israel_simulator.sealed_dossier"}) {
            assertTrue(en.has(k) && it.has(k), k);
        }
        assertEquals("Mossad Handler", en.get("entity.israel_simulator.mossad_handler").getAsString());
    }

    @Test
    @DisplayName("Skins, loot, dossier loot and safehouse pool entries exist")
    void assetsAndWorldgen() throws Exception {
        for (String skin : new String[] {"mossad_agent", "mossad_handler", "mossad_informant"}) {
            var img = javax.imageio.ImageIO.read(ASSETS.resolve("textures/entity/" + skin + ".png").toFile());
            assertEquals(64, img.getWidth());
            assertEquals(64, img.getHeight());
        }
        String loot = Files.readString(DATA.resolve("loot_table/entities/mossad_agent.json"));
        assertTrue(loot.contains("israel_simulator:smg_ammo") && loot.contains("israel_simulator:shekel"));
        assertTrue(Files.readString(DATA.resolve("loot_table/chests/desert_ruins.json")).contains("sealed_dossier"));
        for (String city : new String[] {"tel_aviv", "jerusalem"}) {
            assertTrue(Files.readString(DATA.resolve("worldgen/template_pool/" + city + "/buildings.json"))
                    .contains("israel_simulator:" + city + "/mossad_safehouse"), city);
            assertTrue(Files.exists(DATA.resolve("structure/" + city + "/mossad_safehouse.nbt")), city);
        }
    }

    @Test
    @DisplayName("Agents are armed, stealthy and use the guards' aiming pose")
    void agentBehaviour() throws Exception {
        String agent = Files.readString(Path.of("src/main/java/com/israelsimulator/entity/mossad/MossadAgentEntity.java"));
        assertTrue(agent.contains("setInvisible(stalking)") && agent.contains("ModItems.SMG") && agent.contains("ModItems.PISTOL"));
        assertTrue(agent.contains("MossadRules.isTrusted"));
        String renderer = Files.readString(Path.of("src/main/java/com/israelsimulator/client/renderer/MossadAgentRenderer.java"));
        assertTrue(renderer.contains("new BibiGuardModel(") && renderer.contains("ModelLayers.PLAYER"));
        String spawner = Files.readString(Path.of("src/main/java/com/israelsimulator/mossad/MossadAgentSpawner.java"));
        assertTrue(spawner.contains("isDarkOutside") && spawner.contains("is_israel_region"));
    }
}
