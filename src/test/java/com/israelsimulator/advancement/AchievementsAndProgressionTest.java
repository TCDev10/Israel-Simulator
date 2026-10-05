package com.israelsimulator.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AchievementsAndProgressionTest {

    private static final List<String> ADVANCEMENT_FILES = List.of(
            "exploration/welcome_to_israel.json",
            "cultural/shalom.json",
            "exploration/visit_jerusalem.json",
            "exploration/tel_aviv_nights.json",
            "exploration/jaffa.json",
            "exploration/dead_sea_tourist.json",
            "economy/five_diamonds.json",
            "economy/blessed_trader.json",
            "combat/hava_nagila.json",
            "cultural/freedom_of_speech.json",
            "technology/startup_founder.json",
            "exploration/master_explorer.json",
            "secret/secret_kippah_cat.json",
            "secret/hummus_connoisseur.json"
    );

    @Test
    public void testAllMilestoneAdvancementsExistAndAreValidJson() {
        Path baseDir = Path.of("src/main/resources/data/israel_simulator/advancement");
        for (String subPath : ADVANCEMENT_FILES) {
            Path file = baseDir.resolve(subPath);
            assertTrue(Files.exists(file), "Advancement JSON file must exist: " + file);

            try {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                assertTrue(json.has("display"), "Advancement must have display block: " + subPath);
                JsonObject display = json.getAsJsonObject("display");
                assertTrue(display.has("icon"), "Display must have icon: " + subPath);
                assertTrue(display.has("title"), "Display must have title: " + subPath);
                assertTrue(display.has("description"), "Display must have description: " + subPath);
                assertTrue(json.has("criteria"), "Advancement must have criteria: " + subPath);
            } catch (Exception e) {
                fail("Failed to parse advancement JSON " + subPath + ": " + e.getMessage());
            }
        }
    }

    @Test
    public void testAdvancementLocalizationStringsExistInEnUs() throws Exception {
        Path langPath = Path.of("src/main/resources/assets/israel_simulator/lang/en_us.json");
        assertTrue(Files.exists(langPath), "en_us.json must exist");
        String content = Files.readString(langPath, StandardCharsets.UTF_8);
        JsonObject json = JsonParser.parseString(content).getAsJsonObject();

        String[] advancementKeys = {
                "welcome_to_israel",
                "shalom",
                "visit_jerusalem",
                "tel_aviv_nights",
                "jaffa",
                "dead_sea_tourist",
                "five_diamonds",
                "blessed_trader",
                "hava_nagila",
                "freedom_of_speech",
                "startup_founder",
                "master_explorer",
                "secret_kippah_cat",
                "hummus_connoisseur"
        };

        for (String key : advancementKeys) {
            String titleKey = "advancements.israel_simulator." + key + ".title";
            String descKey = "advancements.israel_simulator." + key + ".description";

            assertTrue(json.has(titleKey), "Missing title translation for: " + titleKey);
            assertTrue(json.has(descKey), "Missing description translation for: " + descKey);
            assertFalse(json.get(titleKey).getAsString().isBlank(), "Title cannot be blank: " + titleKey);
            assertFalse(json.get(descKey).getAsString().isBlank(), "Desc cannot be blank: " + descKey);
        }
    }

    private static final Pattern REGISTERED_NAME = Pattern.compile(
            "register(?:Item|SimpleItem|SimpleBlockItem|EntityType)?\\(\\s*\"([a-z0-9_]+)\""
    );

    private static Set<String> loadRegisteredNames(Path javaFile) throws Exception {
        String src = Files.readString(javaFile, StandardCharsets.UTF_8);
        Set<String> names = new HashSet<>();
        Matcher m = REGISTERED_NAME.matcher(src);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static void collectIds(JsonElement el, Set<String> itemIds, Set<String> entityTypeIds, String keyHint) {
        if (el == null || el.isJsonNull()) {
            return;
        }
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            String value = el.getAsString();
            if (!value.contains(":")) {
                return;
            }
            if ("minecraft:entity_type".equals(keyHint) || "type".equals(keyHint) && value.contains(":")) {
                // entity type values under predicate minecraft:entity_type
                if ("minecraft:entity_type".equals(keyHint)) {
                    entityTypeIds.add(value);
                }
            }
            return;
        }
        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            for (var entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement child = entry.getValue();
                if ("id".equals(key) || "items".equals(key) || "item".equals(key)) {
                    if (child.isJsonPrimitive()) {
                        itemIds.add(child.getAsString());
                    } else if (child.isJsonArray()) {
                        for (JsonElement e : child.getAsJsonArray()) {
                            if (e.isJsonPrimitive()) {
                                itemIds.add(e.getAsString());
                            }
                        }
                    }
                } else if ("minecraft:entity_type".equals(key) && child.isJsonPrimitive()) {
                    entityTypeIds.add(child.getAsString());
                } else if ("type".equals(key) && child.isJsonPrimitive()) {
                    // Legacy / invalid bare entity type field — still flag for registry check
                    String v = child.getAsString();
                    if (v.contains(":") && !v.startsWith("minecraft:") || v.startsWith("israel_simulator:")) {
                        // Only treat as entity if parent context looks like entity predicate;
                        // collected separately via minecraft:entity_type. Skip bare "type" for loot/recipe.
                    }
                } else {
                    collectIds(child, itemIds, entityTypeIds, key);
                }
            }
            return;
        }
        if (el.isJsonArray()) {
            for (JsonElement child : el.getAsJsonArray()) {
                collectIds(child, itemIds, entityTypeIds, keyHint);
            }
        }
    }

    @Test
    @DisplayName("Every item/entity id referenced by advancement JSONs is registered (mod) or vanilla")
    public void testAdvancementReferencedIdsAreRegistered() throws Exception {
        Set<String> modItems = loadRegisteredNames(Path.of("src/main/java/com/israelsimulator/registry/ModItems.java"));
        modItems.addAll(loadRegisteredNames(Path.of("src/main/java/com/israelsimulator/registry/ModBlocks.java")));
        Set<String> modEntities = loadRegisteredNames(Path.of("src/main/java/com/israelsimulator/registry/ModEntities.java"));

        Path advDir = Path.of("src/main/resources/data/israel_simulator/advancement");
        assertTrue(Files.isDirectory(advDir), "advancement dir missing");

        Set<String> missingItems = new HashSet<>();
        Set<String> missingEntities = new HashSet<>();
        try (Stream<Path> stream = Files.walk(advDir)) {
            List<Path> files = stream.filter(p -> p.toString().endsWith(".json")).toList();
            assertFalse(files.isEmpty(), "expected advancement JSON files");
            for (Path file : files) {
                JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                Set<String> itemIds = new HashSet<>();
                Set<String> entityTypeIds = new HashSet<>();
                collectIds(json, itemIds, entityTypeIds, "");
                for (String id : itemIds) {
                    if (id.startsWith("minecraft:")) {
                        continue;
                    }
                    assertTrue(id.startsWith("israel_simulator:"),
                            file + " references non-mod non-vanilla item id: " + id);
                    String path = id.substring("israel_simulator:".length());
                    if (!modItems.contains(path)) {
                        missingItems.add(id + " (" + advDir.relativize(file) + ")");
                    }
                }
                for (String id : entityTypeIds) {
                    if (id.startsWith("minecraft:")) {
                        continue;
                    }
                    assertTrue(id.startsWith("israel_simulator:"),
                            file + " references non-mod non-vanilla entity id: " + id);
                    String path = id.substring("israel_simulator:".length());
                    if (!modEntities.contains(path)) {
                        missingEntities.add(id + " (" + advDir.relativize(file) + ")");
                    }
                }
            }
        }
        assertTrue(missingItems.isEmpty(), "Advancement item ids not in ModItems/ModBlocks: " + missingItems);
        assertTrue(missingEntities.isEmpty(), "Advancement entity ids not in ModEntities: " + missingEntities);
    }

    @Test
    @DisplayName("Hava Nagila uses 26.2 entity_properties / minecraft:entity_type predicate form")
    public void testHavaNagilaUsesModernEntityTypePredicate() throws Exception {
        Path file = Path.of("src/main/resources/data/israel_simulator/advancement/combat/hava_nagila.json");
        JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonElement entity = json.getAsJsonObject("criteria")
                .getAsJsonObject("kill_bibi_boss")
                .getAsJsonObject("conditions")
                .get("entity");
        assertTrue(entity.isJsonArray(), "entity conditions must be a predicate array on 26.2");
        String raw = entity.toString();
        assertTrue(raw.contains("minecraft:entity_properties"));
        assertTrue(raw.contains("minecraft:entity_type"));
        assertTrue(raw.contains("israel_simulator:bibi_boss"));
        assertFalse(raw.contains("\"type\":\"israel_simulator:bibi_boss\""),
                "legacy bare type field must not be used");
    }
}

