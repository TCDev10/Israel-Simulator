package com.israelsimulator.item.combat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Firearms (assault rifle, SMG, sniper rifle), frag grenades and ammo: balance, magazine logic,
 * 3D models, recipes and translations.
 */
class FirearmsTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/israel_simulator");
    private static final Path RECIPES = Path.of("src/main/resources/data/israel_simulator/recipe");
    private static final List<String> MODELLED = List.of("assault_rifle", "smg", "sniper_rifle", "frag_grenade");
    private static final List<String> AMMO = List.of("rifle_ammo", "smg_ammo", "sniper_ammo");
    private static final double NETHERITE_SWORD_DPS = 8.0 * 1.6;

    private static JsonObject read(Path p) throws Exception {
        assertTrue(Files.exists(p), "missing " + p);
        return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
    }

    @Test
    @DisplayName("Weapon stats are balanced against vanilla melee and ranged weapons")
    void balance() {
        for (FirearmStats s : FirearmStats.ALL) {
            assertTrue(s.sustainedDps() <= NETHERITE_SWORD_DPS * 1.4,
                    s.id() + " sustained DPS " + s.sustainedDps() + " far above a netherite sword");
            assertTrue(s.damage() > 0 && s.damage() <= 20.0F, s.id());
            assertTrue(s.fireIntervalTicks() >= 4, s.id() + ": faster than the vanilla 4-tick use repeat is unreachable");
            assertTrue(s.magazineSize() > 0 && s.reloadTicks() >= 20, s.id());
            assertTrue(AMMO.contains(s.ammoId()), s.id());
        }
        // Role checks: SMG is the shortest-ranged and least accurate, the sniper the strongest per shot.
        assertTrue(FirearmStats.SMG.range() < FirearmStats.ASSAULT_RIFLE.range());
        assertTrue(FirearmStats.SMG.spreadDegrees() > FirearmStats.ASSAULT_RIFLE.spreadDegrees());
        assertTrue(FirearmStats.SNIPER_RIFLE.damage() > FirearmStats.ASSAULT_RIFLE.damage() * 4);
        assertEquals(0.0F, FirearmStats.SNIPER_RIFLE.scopedSpreadDegrees(), "scoped sniper is precise");
        assertTrue(FirearmStats.SNIPER_RIFLE.spreadDegrees() >= 4.0F, "hip-firing the sniper is inaccurate");
        // A sniper headshot-free body shot must not one-shot a full-health player with no armour twice over.
        assertTrue(FirearmStats.SNIPER_RIFLE.damage() <= 20.0F);
    }

    @Test
    @DisplayName("Reload loads what is missing, limited by the ammo in the inventory")
    void reloadMath() {
        assertEquals(30, FirearmStats.roundsToLoad(0, 30, 64));
        assertEquals(12, FirearmStats.roundsToLoad(18, 30, 64));
        assertEquals(7, FirearmStats.roundsToLoad(0, 30, 7));
        assertEquals(0, FirearmStats.roundsToLoad(30, 30, 64));
        assertEquals(0, FirearmStats.roundsToLoad(3, 5, 0));
    }

    @Test
    @DisplayName("Each weapon and the grenade have a 3D cuboid model, a flat GUI icon and a 32x32 atlas")
    void models() throws Exception {
        for (String id : MODELLED) {
            JsonObject def = read(ASSETS.resolve("items/" + id + ".json")).getAsJsonObject("model");
            assertEquals("minecraft:display_context", def.get("property").getAsString(), id);
            assertEquals("israel_simulator:item/" + id + "_icon",
                    def.getAsJsonArray("cases").get(0).getAsJsonObject().getAsJsonObject("model").get("model").getAsString());
            assertEquals("israel_simulator:item/" + id, def.getAsJsonObject("fallback").get("model").getAsString());

            JsonObject model = read(ASSETS.resolve("models/item/" + id + ".json"));
            assertFalse(model.has("parent"), id);
            assertTrue(model.getAsJsonArray("elements").size() >= 10, id + " should be detailed");
            for (JsonElement e : model.getAsJsonArray("elements")) {
                JsonObject el = e.getAsJsonObject();
                for (String k : List.of("from", "to")) {
                    for (JsonElement v : el.getAsJsonArray(k)) {
                        assertTrue(v.getAsFloat() >= -16 && v.getAsFloat() <= 32, id + " out of bounds: " + el.get("name"));
                    }
                }
                if (el.has("rotation")) {
                    float a = el.getAsJsonObject("rotation").get("angle").getAsFloat();
                    assertTrue(Math.abs(a) <= 45 && a % 22.5F == 0, id + " " + el.get("name"));
                }
                assertEquals(6, el.getAsJsonObject("faces").size(), id + " " + el.get("name"));
            }
            JsonObject display = model.getAsJsonObject("display");
            for (String ctx : List.of("thirdperson_righthand", "thirdperson_lefthand", "firstperson_righthand",
                    "firstperson_lefthand", "gui", "ground", "fixed", "head")) {
                assertTrue(display.has(ctx), id + " missing display." + ctx);
            }
            BufferedImage atlas = ImageIO.read(ASSETS.resolve("textures/item/" + id + "_model.png").toFile());
            assertEquals(32, atlas.getWidth());
            assertEquals(32, atlas.getHeight());
            BufferedImage icon = ImageIO.read(ASSETS.resolve("textures/item/" + id + ".png").toFile());
            assertEquals(32, icon.getWidth());
        }
        // Firearms keep the pistol's hand rotation so the grip lands in the fist.
        for (String gun : List.of("assault_rifle", "smg", "sniper_rifle")) {
            JsonObject tp = read(ASSETS.resolve("models/item/" + gun + ".json")).getAsJsonObject("display")
                    .getAsJsonObject("thirdperson_righthand");
            assertEquals("[0,-90,-72]", tp.get("rotation").toString().replace(" ", ""), gun);
        }
        for (String ammo : AMMO) {
            assertNotNull(ImageIO.read(ASSETS.resolve("textures/item/" + ammo + ".png").toFile()));
            assertTrue(Files.exists(ASSETS.resolve("items/" + ammo + ".json")));
        }
    }

    @Test
    @DisplayName("Weapons, grenades and ammo are craftable with 3x3 shaped recipes")
    void recipes() throws Exception {
        for (String id : List.of("assault_rifle", "smg", "sniper_rifle", "frag_grenade", "rifle_ammo", "smg_ammo", "sniper_ammo")) {
            JsonObject r = read(RECIPES.resolve(id + ".json"));
            assertEquals("minecraft:crafting_shaped", r.get("type").getAsString());
            assertEquals("israel_simulator:" + id, r.getAsJsonObject("result").get("id").getAsString());
            var pattern = r.getAsJsonArray("pattern");
            assertTrue(pattern.size() <= 3);
            Set<Character> used = new HashSet<>();
            for (JsonElement row : pattern) {
                assertTrue(row.getAsString().length() <= 3, id);
                for (char c : row.getAsString().toCharArray()) {
                    if (c != ' ') used.add(c);
                }
            }
            for (char c : used) {
                assertTrue(r.getAsJsonObject("key").has(String.valueOf(c)), id + " key " + c);
            }
        }
        assertTrue(Files.readString(RECIPES.resolve("assault_rifle.json")).contains("netherite_ingot"));
        assertTrue(Files.readString(RECIPES.resolve("sniper_rifle.json")).contains("spyglass"));
    }

    @Test
    @DisplayName("Weapon names are translated in English and Italian")
    void translations() throws Exception {
        JsonObject en = read(ASSETS.resolve("lang/en_us.json"));
        JsonObject it = read(ASSETS.resolve("lang/it_it.json"));
        for (String id : List.of("assault_rifle", "smg", "sniper_rifle", "frag_grenade", "rifle_ammo", "smg_ammo", "sniper_ammo")) {
            assertTrue(en.has("item.israel_simulator." + id), id);
            assertTrue(it.has("item.israel_simulator." + id), id);
        }
        assertTrue(en.has("key.category.israel_simulator.weapons") && it.has("key.israel_simulator.reload"));
    }

    @Test
    @DisplayName("Grenades do not break blocks unless the config allows it")
    void grenadeConfig() throws Exception {
        String entity = Files.readString(Path.of("src/main/java/com/israelsimulator/entity/projectile/FragGrenadeEntity.java"));
        assertTrue(entity.contains("ExplosionInteraction.NONE"));
        String config = Files.readString(Path.of("src/main/java/com/israelsimulator/config/IsraelSimulatorConfig.java"));
        assertTrue(config.contains("define(\"grenadeBreaksBlocks\", false)"));
    }
}
