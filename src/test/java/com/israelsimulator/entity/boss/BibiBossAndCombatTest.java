package com.israelsimulator.entity.boss;

import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.item.cultural.CulturalItems;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BibiBossAndCombatTest {

    @Test
    @DisplayName("Verify BibiBossState state machine behavior")
    void testBibiBossStateMachine() {
        assertFalse(BibiBossState.IDLE.isAggressive());
        assertFalse(BibiBossState.ALERT.isAggressive());
        assertTrue(BibiBossState.COMBAT.isAggressive());
        assertTrue(BibiBossState.ENRAGED.isAggressive());
        assertTrue(BibiBossState.DESPERATE.isAggressive());
        assertFalse(BibiBossState.DEFEATED.isAggressive());

        assertFalse(BibiBossState.COMBAT.isEnraged());
        assertTrue(BibiBossState.ENRAGED.isEnraged());
        assertTrue(BibiBossState.DESPERATE.isEnraged());
        assertEquals(6, BibiBossState.values().length);
    }

    @Test
    @DisplayName("Verify Bibi Boss configuration and constants")
    void testBossConfigurationAndConstants() {
        assertEquals(10000.0, IsraelSimulatorConfig.bibiBossMaxHealth(), "Default boss health must be 10,000+ HP (GAME_DESIGN §41.1)");
        assertEquals(18.0, IsraelSimulatorConfig.bibiBossBaseDamage());
        assertEquals(4, IsraelSimulatorConfig.bibiBossMaxGuards(), "Max guards limit prevents mob runaway");
        assertEquals(48, IsraelSimulatorConfig.bibiBossArenaRadius());

        assertEquals(0.66F, BibiBossEntity.PHASE_2_HEALTH_FRACTION, 0.001F, "Phase 2 (Trump) triggers below 66% HP");
        assertEquals(0.33F, BibiBossEntity.PHASE_3_HEALTH_FRACTION, 0.001F, "Phase 3 (Epstein) triggers below 33% HP");
        assertEquals(0.66F, BibiBossEntity.ENRAGE_HEALTH_FRACTION, 0.001F, "Phase 2 (Trump/Enrage) triggers below 66% HP in 3-phase encounter");
        assertEquals(500.0F, BibiBossEntity.MAX_SINGLE_HIT_DAMAGE, 0.001F, "Single-hit damage cap must prevent one-shot cheese");
        assertTrue(BibiBossSpawner.BOSS_DUPLICATION_CHECK_RADIUS >= 64.0, "Boss duplication radius must adequately cover the arena");
    }

    @Test
    @DisplayName("Verify Bibi Boss, Miniboss Trump, ICE Agent, Epstein, Child Zombie, and Guard entity class structures")
    void testEntityClassStructures() throws Exception {
        ClassLoader cl = BibiBossAndCombatTest.class.getClassLoader();
        Class<?> bossClass = Class.forName("com.israelsimulator.entity.boss.BibiBossEntity", false, cl);
        assertNotNull(bossClass, "BibiBossEntity class must exist");
        assertEquals("net.minecraft.world.entity.monster.Monster", bossClass.getSuperclass().getName());

        Class<?> guardClass = Class.forName("com.israelsimulator.entity.boss.BibiGuardEntity", false, cl);
        assertNotNull(guardClass, "BibiGuardEntity class must exist");
        assertEquals("net.minecraft.world.entity.monster.Monster", guardClass.getSuperclass().getName());

        Class<?> trumpClass = Class.forName("com.israelsimulator.entity.boss.TrumpMinibossEntity", false, cl);
        assertNotNull(trumpClass, "TrumpMinibossEntity class must exist");
        assertEquals("net.minecraft.world.entity.monster.Monster", trumpClass.getSuperclass().getName());

        Class<?> iceClass = Class.forName("com.israelsimulator.entity.boss.IceAgentEntity", false, cl);
        assertNotNull(iceClass, "IceAgentEntity class must exist");
        assertEquals("net.minecraft.world.entity.monster.Monster", iceClass.getSuperclass().getName());

        Class<?> epsteinClass = Class.forName("com.israelsimulator.entity.boss.JeffreyEpsteinEntity", false, cl);
        assertNotNull(epsteinClass, "JeffreyEpsteinEntity class must exist");
        assertEquals("net.minecraft.world.entity.monster.Monster", epsteinClass.getSuperclass().getName());

        Class<?> childZombieClass = Class.forName("com.israelsimulator.entity.boss.ChildZombieMinionEntity", false, cl);
        assertNotNull(childZombieClass, "ChildZombieMinionEntity class must exist");
        assertEquals("net.minecraft.world.entity.monster.Monster", childZombieClass.getSuperclass().getName());

        Class<?> missileClass = Class.forName("com.israelsimulator.entity.boss.BibiMissileEntity", false, cl);
        assertNotNull(missileClass, "BibiMissileEntity class must exist");
        assertEquals("net.minecraft.world.entity.projectile.Projectile", missileClass.getSuperclass().getName());
    }

    @Test
    @DisplayName("Verify sounds.json defines all required sound events")
    void testSoundDefinitionsJson() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/sounds.json");
        assertNotNull(stream, "sounds.json must exist in assets");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("music_disc.hava_nagila"), "Missing music_disc.hava_nagila sound event");
            assertTrue(json.contains("entity.bibi_boss.ambient"), "Missing entity.bibi_boss.ambient sound event");
            assertTrue(json.contains("entity.bibi_boss.hurt"), "Missing entity.bibi_boss.hurt sound event");
            assertTrue(json.contains("entity.bibi_boss.death"), "Missing entity.bibi_boss.death sound event");
            assertTrue(json.contains("entity.bibi_boss.speech"), "Missing entity.bibi_boss.speech sound event");
            assertTrue(json.contains("entity.bibi_boss.enrage"), "Missing entity.bibi_boss.enrage sound event");
        } catch (Exception e) {
            fail("Failed reading sounds.json: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Hava Nagila disc item class and jukebox song key")
    void testHavaNagilaDiscConfiguration() throws Exception {
        ClassLoader cl = BibiBossAndCombatTest.class.getClassLoader();
        Class<?> discClass = Class.forName("com.israelsimulator.item.cultural.HavaNagilaDiscItem", false, cl);
        assertNotNull(discClass, "HavaNagilaDiscItem must exist");
        assertEquals("net.minecraft.world.item.Item", discClass.getSuperclass().getName());

        assertNotNull(CulturalItems.HAVA_NAGILA_SONG);
        assertEquals("israel_simulator:hava_nagila", CulturalItems.HAVA_NAGILA_SONG.identifier().toString());
    }

    @Test
    @DisplayName("Verify Hava Nagila jukebox song JSON exists and is valid")
    void testJukeboxSongJson() {
        InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/jukebox_song/hava_nagila.json");
        assertNotNull(stream, "data/israel_simulator/jukebox_song/hava_nagila.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:music_disc.hava_nagila"));
            assertTrue(json.contains("comparator_output"));
        } catch (Exception e) {
            fail("Failed reading jukebox_song JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Bibi Boss entity loot table JSON exists and drops Hava Nagila disc")
    void testBossLootTableJson() {
        InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/loot_table/entities/bibi_boss.json");
        assertNotNull(stream, "data/israel_simulator/loot_table/entities/bibi_boss.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:hava_nagila_disc"), "Loot table must drop Hava Nagila disc");
            assertTrue(json.contains("israel_simulator:shekel"), "Loot table must include Shekels");
            assertTrue(json.contains("israel_simulator:ancient_coin"), "Loot table must include Ancient Coins");
        } catch (Exception e) {
            fail("Failed reading loot table JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify Hava Nagila advancement JSON exists and targets bibi_boss")
    void testAdvancementJson() {
        InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/advancement/combat/hava_nagila.json");
        assertNotNull(stream, "data/israel_simulator/advancement/combat/hava_nagila.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("israel_simulator:bibi_boss"));
            assertTrue(json.contains("minecraft:entity_type"));
            assertTrue(json.contains("israel_simulator:hava_nagila_disc"));
            assertTrue(json.contains("advancements.israel_simulator.hava_nagila.title"));
        } catch (Exception e) {
            fail("Failed reading advancement JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify boss entity textures exist")
    void testBossEntityTextures() {
        InputStream bossStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/bibi_boss.png");
        assertNotNull(bossStream, "bibi_boss.png must exist");

        InputStream guardStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/bibi_guard.png");
        assertNotNull(guardStream, "bibi_guard.png must exist");

        InputStream trumpStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/trump_miniboss.png");
        assertNotNull(trumpStream, "trump_miniboss.png must exist");

        InputStream iceStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/ice_agent.png");
        assertNotNull(iceStream, "ice_agent.png must exist");

        InputStream epsteinStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/jeffrey_epstein.png");
        assertNotNull(epsteinStream, "jeffrey_epstein.png must exist");

        InputStream childStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/child_zombie.png");
        assertNotNull(childStream, "child_zombie.png must exist");
    }

    @Test
    @DisplayName("Verify dollar bill particle assets exist")
    void testDollarBillParticleAsset() {
        InputStream texStream = getClass().getResourceAsStream("/assets/israel_simulator/textures/particle/dollar_bill.png");
        assertNotNull(texStream, "dollar_bill.png texture must exist");

        InputStream defStream = getClass().getResourceAsStream("/assets/israel_simulator/particles/dollar_bill.json");
        assertNotNull(defStream, "dollar_bill.json particle definition must exist");
    }

    @Test
    @DisplayName("Verify Star of David endgame recipe requires nether star, netherite, diamonds, and rare relics")
    void testStarOfDavidRecipe() {
        InputStream stream = getClass().getResourceAsStream("/data/israel_simulator/recipe/star_of_david.json");
        assertNotNull(stream, "data/israel_simulator/recipe/star_of_david.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("minecraft:nether_star"), "Recipe must require Nether Star");
            assertTrue(json.contains("minecraft:netherite_ingot"), "Recipe must require Netherite Ingot");
            assertTrue(json.contains("minecraft:diamond_block"), "Recipe must require Diamond Block");
            assertTrue(json.contains("israel_simulator:dead_sea_scroll_fragment"), "Recipe must require Dead Sea Scroll Fragments");
            assertTrue(json.contains("israel_simulator:ancient_coin"), "Recipe must require Ancient Coins");
            assertTrue(json.contains("israel_simulator:star_of_david"), "Recipe must output Star of David");
        } catch (Exception e) {
            fail("Failed reading star_of_david recipe JSON: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verify boss, guard, miniboss, epstein, and disc localizations in en_us.json")
    void testBossLocalizations() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"entity.israel_simulator.bibi_boss\""));
            assertTrue(json.contains("\"entity.israel_simulator.bibi_boss.enraged\""));
            assertTrue(json.contains("\"entity.israel_simulator.bibi_boss.desperate\""));
            assertTrue(json.contains("\"entity.israel_simulator.bibi_guard\""));
            assertTrue(json.contains("\"entity.israel_simulator.trump_miniboss\""));
            assertTrue(json.contains("\"entity.israel_simulator.ice_agent\""));
            assertTrue(json.contains("\"entity.israel_simulator.jeffrey_epstein\""));
            assertTrue(json.contains("\"entity.israel_simulator.child_zombie_minion\""));
            assertTrue(json.contains("\"item.israel_simulator.star_of_david.desc\""));
            assertTrue(json.contains("\"item.israel_simulator.star_of_david.summon_hint\""));
            assertTrue(json.contains("\"item.israel_simulator.hava_nagila_disc\""));
            assertTrue(json.contains("\"item.israel_simulator.hava_nagila_disc.desc\""));
            assertTrue(json.contains("\"item.israel_simulator.hava_nagila_disc.lore\""));
            assertTrue(json.contains("\"jukebox_song.israel_simulator.hava_nagila\""));
            assertTrue(json.contains("\"advancements.israel_simulator.hava_nagila.title\""));
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }
}
