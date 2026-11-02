package com.israelsimulator.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The whole Israel-Simulator advancement tab: one root, every parent exists, every criterion is
 * reachable, and every id an advancement names (items, entities, structures, biomes, effects,
 * triggers, custom events) really exists in the mod or in vanilla.
 */
public class AchievementsAndProgressionTest {

    private static final String NS = "israel_simulator:";
    private static final Path ADV_DIR = Path.of("src/main/resources/data/israel_simulator/advancement");
    private static final Path DATA = Path.of("src/main/resources/data/israel_simulator");
    private static final Path JAVA = Path.of("src/main/java/com/israelsimulator");
    private static final Path LANG = Path.of("src/main/resources/assets/israel_simulator/lang");

    /** Vanilla triggers the mod's advancements may use. */
    private static final Set<String> VANILLA_TRIGGERS = Set.of(
            "minecraft:location", "minecraft:inventory_changed", "minecraft:consume_item",
            "minecraft:player_killed_entity", "minecraft:villager_trade", "minecraft:effects_changed");
    private static final String MOD_EVENT = "israel_simulator:mod_event";

    /** Original advancements that must keep their ids (docs, wiki and other tests point at them). */
    private static final List<String> MILESTONES = List.of(
            "exploration/welcome_to_israel", "cultural/shalom", "exploration/visit_jerusalem",
            "exploration/tel_aviv_nights", "exploration/jaffa", "exploration/dead_sea_tourist",
            "economy/five_diamonds", "economy/blessed_trader", "combat/hava_nagila",
            "cultural/freedom_of_speech", "technology/startup_founder", "exploration/master_explorer",
            "secret/secret_kippah_cat", "food/hummus_connoisseur");

    private static final Map<String, JsonObject> ADVANCEMENTS = new HashMap<>();
    private static JsonObject en;
    private static JsonObject it;

    @BeforeAll
    static void load() throws Exception {
        try (Stream<Path> s = Files.walk(ADV_DIR)) {
            for (Path f : s.filter(p -> p.toString().endsWith(".json")).toList()) {
                String id = ADV_DIR.relativize(f).toString().replace('\\', '/').replace(".json", "");
                ADVANCEMENTS.put(NS + id, JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject());
            }
        }
        en = JsonParser.parseString(Files.readString(LANG.resolve("en_us.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        it = JsonParser.parseString(Files.readString(LANG.resolve("it_it.json"), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test
    @DisplayName("Milestone advancements exist and there are plenty of advancements")
    void milestonesExist() {
        for (String m : MILESTONES) {
            assertTrue(ADVANCEMENTS.containsKey(NS + m), "missing milestone advancement " + m);
        }
        assertTrue(ADVANCEMENTS.size() >= 50, "expected at least 50 advancements, got " + ADVANCEMENTS.size());
    }

    @Test
    @DisplayName("One root; every parent exists; no cycles")
    void treeIsConnected() {
        List<String> roots = new ArrayList<>();
        for (Map.Entry<String, JsonObject> e : ADVANCEMENTS.entrySet()) {
            JsonObject json = e.getValue();
            if (!json.has("parent")) {
                roots.add(e.getKey());
                assertTrue(json.getAsJsonObject("display").has("background"), "root needs a tab background");
                continue;
            }
            String parent = json.get("parent").getAsString();
            assertTrue(ADVANCEMENTS.containsKey(parent), e.getKey() + " has unknown parent " + parent);
        }
        assertEquals(List.of(NS + "exploration/welcome_to_israel"), roots, "exactly one root expected");
        for (String id : ADVANCEMENTS.keySet()) {
            Set<String> seen = new HashSet<>();
            String cur = id;
            while (ADVANCEMENTS.get(cur).has("parent")) {
                assertTrue(seen.add(cur), "cycle at " + id);
                cur = ADVANCEMENTS.get(cur).get("parent").getAsString();
            }
        }
    }

    @Test
    @DisplayName("Display: icon, frame, and translated title/description in en_us and it_it")
    void displaysAreComplete() {
        Set<String> frames = Set.of("task", "goal", "challenge");
        for (Map.Entry<String, JsonObject> e : ADVANCEMENTS.entrySet()) {
            JsonObject display = e.getValue().getAsJsonObject("display");
            assertNotNull(display, e.getKey() + " has no display");
            assertTrue(display.getAsJsonObject("icon").has("id"), e.getKey() + " icon id");
            assertTrue(frames.contains(display.get("frame").getAsString()), e.getKey() + " frame");
            for (String part : List.of("title", "description")) {
                String key = display.getAsJsonObject(part).get("translate").getAsString();
                assertTrue(key.startsWith("advancements.israel_simulator."), key);
                assertTrue(en.has(key) && !en.get(key).getAsString().isBlank(), "en_us missing " + key);
                assertTrue(it.has(key) && !it.get(key).getAsString().isBlank(), "it_it missing " + key);
            }
        }
    }

    @Test
    @DisplayName("Advancement lang keys are the same set in en_us and it_it, with no leftovers")
    void langKeysMatch() {
        Set<String> used = new TreeSet<>();
        for (JsonObject json : ADVANCEMENTS.values()) {
            JsonObject display = json.getAsJsonObject("display");
            used.add(display.getAsJsonObject("title").get("translate").getAsString());
            used.add(display.getAsJsonObject("description").get("translate").getAsString());
        }
        Set<String> enKeys = new TreeSet<>();
        Set<String> itKeys = new TreeSet<>();
        en.keySet().stream().filter(k -> k.startsWith("advancements.israel_simulator.")).forEach(enKeys::add);
        it.keySet().stream().filter(k -> k.startsWith("advancements.israel_simulator.")).forEach(itKeys::add);
        assertEquals(enKeys, itKeys, "en_us and it_it advancement keys differ");
        assertEquals(used, enKeys, "unused or missing advancement lang keys");
    }

    @Test
    @DisplayName("Every criterion is required and every requirement names a criterion")
    void requirementsMatchCriteria() {
        for (Map.Entry<String, JsonObject> e : ADVANCEMENTS.entrySet()) {
            JsonObject criteria = e.getValue().getAsJsonObject("criteria");
            assertFalse(criteria.isEmpty(), e.getKey() + " has no criteria");
            Set<String> required = new HashSet<>();
            for (JsonElement group : e.getValue().getAsJsonArray("requirements")) {
                for (JsonElement name : group.getAsJsonArray()) {
                    assertTrue(criteria.has(name.getAsString()), e.getKey() + " requires unknown criterion " + name);
                    required.add(name.getAsString());
                }
            }
            assertEquals(criteria.keySet(), required, e.getKey() + " has criteria outside the requirements");
        }
    }

    @Test
    @DisplayName("Triggers are vanilla or israel_simulator:mod_event with a known, fired event")
    void triggersAndEventsExist() throws Exception {
        String trigger = Files.readString(JAVA.resolve("registry/ModTriggers.java"));
        assertTrue(trigger.contains("register(\"mod_event\""), "mod_event trigger must be registered");

        Set<String> events = constants(JAVA.resolve("advancement/AdvancementEvents.java"));
        String prefix = "festival_";
        events.remove(prefix);
        Set<String> festivals = new HashSet<>();
        Matcher f = Pattern.compile("^\\s+[A-Z_]+\\(\\s*\"([a-z_]+)\"", Pattern.MULTILINE)
                .matcher(Files.readString(JAVA.resolve("festival/FestivalType.java")));
        while (f.find()) festivals.add(prefix + f.group(1));
        assertEquals(7, festivals.size(), "festival ids: " + festivals);

        for (Map.Entry<String, JsonObject> e : ADVANCEMENTS.entrySet()) {
            for (Map.Entry<String, JsonElement> c : e.getValue().getAsJsonObject("criteria").entrySet()) {
                JsonObject crit = c.getValue().getAsJsonObject();
                String t = crit.get("trigger").getAsString();
                if (t.equals(MOD_EVENT)) {
                    String ev = crit.getAsJsonObject("conditions").get("event").getAsString();
                    assertTrue(events.contains(ev) || festivals.contains(ev), e.getKey() + " uses unknown event " + ev);
                } else {
                    assertTrue(VANILLA_TRIGGERS.contains(t), e.getKey() + " uses unexpected trigger " + t);
                }
            }
        }

        // every event constant is really fired somewhere in the gameplay code
        String allCode = allJava();
        Matcher m = Pattern.compile("public static final String ([A-Z_]+) = \"([a-z_]+)\"")
                .matcher(Files.readString(JAVA.resolve("advancement/AdvancementEvents.java")));
        while (m.find()) {
            if (m.group(2).equals(prefix)) continue;
            assertTrue(allCode.contains("AdvancementEvents." + m.group(1) + ")"),
                    "event " + m.group(2) + " is never fired");
        }
    }

    @Test
    @DisplayName("Every item, entity, structure, biome and effect id named by an advancement exists")
    void referencedIdsExist() throws Exception {
        Set<String> items = registered(JAVA.resolve("registry/ModItems.java"));
        items.addAll(registered(JAVA.resolve("registry/ModBlocks.java")));
        Set<String> entities = registered(JAVA.resolve("registry/ModEntities.java"));
        Set<String> effects = registered(JAVA.resolve("registry/ModMobEffects.java"));
        assertTrue(entities.contains("money_changer"), "constant-named entity ids must resolve");

        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, JsonObject> e : ADVANCEMENTS.entrySet()) {
            Refs refs = new Refs();
            collect(e.getValue(), null, refs);
            for (String id : refs.items) check(missing, e.getKey(), "item", id, items.contains(local(id)));
            for (String id : refs.entities) check(missing, e.getKey(), "entity", id, entities.contains(local(id)));
            for (String id : refs.effects) check(missing, e.getKey(), "effect", id, effects.contains(local(id)));
            for (String id : refs.structures) {
                check(missing, e.getKey(), "structure", id,
                        Files.exists(DATA.resolve("worldgen/structure/" + local(id) + ".json")));
            }
            for (String id : refs.biomes) {
                boolean ok = id.startsWith("#")
                        ? Files.exists(DATA.resolve("tags/worldgen/biome/" + local(id.substring(1)) + ".json"))
                        : Files.exists(DATA.resolve("worldgen/biome/" + local(id) + ".json"));
                check(missing, e.getKey(), "biome", id, ok);
            }
        }
        assertTrue(missing.isEmpty(), "Advancements reference ids that do not exist: " + missing);
    }

    @Test
    @DisplayName("Hava Nagila uses 26.2 entity_properties / minecraft:entity_type predicate form")
    public void testHavaNagilaUsesModernEntityTypePredicate() {
        JsonObject json = ADVANCEMENTS.get(NS + "combat/hava_nagila");
        JsonElement entity = json.getAsJsonObject("criteria")
                .getAsJsonObject("kill_bibi_boss")
                .getAsJsonObject("conditions")
                .get("entity");
        assertTrue(entity.isJsonArray(), "entity conditions must be a predicate array on 26.2");
        String raw = entity.toString();
        assertTrue(raw.contains("minecraft:entity_properties"));
        assertTrue(raw.contains("minecraft:entity_type"));
        assertTrue(raw.contains("israel_simulator:bibi_boss"));
        assertEquals(2, json.getAsJsonArray("requirements").size(), "kill AND disc, not either");
    }

    @Test
    @DisplayName("Location criteria use the 26.2 player predicate, not a bare 'location' field")
    void locationCriteriaUsePlayerPredicate() {
        for (Map.Entry<String, JsonObject> e : ADVANCEMENTS.entrySet()) {
            for (Map.Entry<String, JsonElement> c : e.getValue().getAsJsonObject("criteria").entrySet()) {
                JsonObject crit = c.getValue().getAsJsonObject();
                if (!crit.get("trigger").getAsString().equals("minecraft:location")) continue;
                JsonObject cond = crit.getAsJsonObject("conditions");
                assertFalse(cond.has("location"), e.getKey() + ": 'location' is not a field of minecraft:location");
                assertTrue(cond.get("player").isJsonArray(), e.getKey() + ": player predicate array expected");
            }
        }
    }

    // ---- helpers ----

    private static final class Refs {
        final Set<String> items = new HashSet<>();
        final Set<String> entities = new HashSet<>();
        final Set<String> structures = new HashSet<>();
        final Set<String> biomes = new HashSet<>();
        final Set<String> effects = new HashSet<>();
    }

    private static void collect(JsonElement el, String key, Refs refs) {
        if (el.isJsonArray()) {
            for (JsonElement child : el.getAsJsonArray()) collect(child, key, refs);
            return;
        }
        if (el.isJsonPrimitive()) {
            if (!el.getAsJsonPrimitive().isString() || key == null) return;
            String v = el.getAsString();
            switch (key) {
                case "id", "items" -> refs.items.add(v);
                case "minecraft:entity_type" -> refs.entities.add(v);
                case "structures" -> refs.structures.add(v);
                case "biomes" -> refs.biomes.add(v);
                default -> { }
            }
            return;
        }
        if (!el.isJsonObject()) return;
        for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
            if ("effects".equals(e.getKey()) && e.getValue().isJsonObject()) {
                refs.effects.addAll(e.getValue().getAsJsonObject().keySet());
                continue;
            }
            if ("display".equals(key) && !"icon".equals(e.getKey())) continue;
            collect(e.getValue(), e.getKey(), refs);
        }
    }

    private static void check(List<String> missing, String adv, String kind, String id, boolean exists) {
        if (id.startsWith("minecraft:") || id.startsWith("#minecraft:")) return;
        if (!exists) missing.add(kind + " " + id + " (" + adv + ")");
    }

    private static String local(String id) {
        return id.startsWith(NS) ? id.substring(NS.length()) : "\u0000" + id;
    }

    /** Names registered in a registry class; constant names (Foo.BAR) are resolved from the code. */
    private static Set<String> registered(Path registry) throws Exception {
        String src = Files.readString(registry, StandardCharsets.UTF_8);
        Set<String> names = new HashSet<>();
        Matcher m = Pattern.compile("\\.register(?:Item|SimpleItem|SimpleBlockItem|EntityType|Block|SimpleBlock)?\\(\\s*\"([a-z0-9_]+)\"").matcher(src);
        while (m.find()) names.add(m.group(1));
        Matcher c = Pattern.compile("\\.register(?:Item|EntityType|Block)?\\(\\s*([A-Za-z.]+)\\.([A-Z_]+)\\s*,").matcher(src);
        String code = allJava();
        while (c.find()) {
            Matcher v = Pattern.compile(c.group(2) + "\\s*=\\s*\"([a-z0-9_]+)\"").matcher(code);
            if (v.find()) names.add(v.group(1));
        }
        return names;
    }

    private static Set<String> constants(Path javaFile) throws Exception {
        Set<String> out = new HashSet<>();
        Matcher m = Pattern.compile("= \"([a-z_]+)\"").matcher(Files.readString(javaFile));
        while (m.find()) out.add(m.group(1));
        return out;
    }

    private static String cachedJava;

    private static String allJava() throws Exception {
        if (cachedJava == null) {
            StringBuilder sb = new StringBuilder();
            try (Stream<Path> s = Files.walk(JAVA)) {
                for (Path p : s.filter(x -> x.toString().endsWith(".java")).toList()) {
                    sb.append(Files.readString(p, StandardCharsets.UTF_8)).append('\n');
                }
            }
            cachedJava = sb.toString();
        }
        return cachedJava;
    }
}
