package com.israelsimulator.world;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every loot table JSON uses 26.2 number providers and only references known item ids.
 */
class LootTableValidationTest {

    private static final Gson GSON = new Gson();
    private static final Path LOOT_ROOT = Path.of(
            "src/main/resources/data/israel_simulator/loot_table");

    private static final Pattern REGISTER_ITEM = Pattern.compile(
            "register(?:Item|SimpleItem|SimpleBlockItem)\\(\\s*\"([a-z0-9_]+)\"");

    @Test
    @DisplayName("All loot tables use 26.2 uniform number providers and valid item ids")
    void lootTablesAreValidFor26_2() throws Exception {
        assertTrue(Files.isDirectory(LOOT_ROOT), "loot_table directory missing");
        Set<String> modItems = loadModItemIds();
        List<String> problems = new ArrayList<>();
        int files = 0;
        try (Stream<Path> walk = Files.walk(LOOT_ROOT)) {
            for (Path path : walk.filter(p -> p.toString().endsWith(".json")).toList()) {
                files++;
                JsonObject root = GSON.fromJson(Files.readString(path), JsonObject.class);
                String rel = LOOT_ROOT.relativize(path).toString().replace('\\', '/');
                if (!root.has("type") || !root.has("pools")) {
                    problems.add(rel + ": missing type/pools");
                    continue;
                }
                checkPools(root.getAsJsonArray("pools"), rel, modItems, problems);
            }
        }
        assertTrue(files >= 20, "Expected many loot tables, found " + files);
        assertTrue(problems.isEmpty(), "Loot table problems:\n" + String.join("\n", problems));
    }

    private void checkPools(JsonArray pools, String rel, Set<String> modItems, List<String> problems) {
        for (int i = 0; i < pools.size(); i++) {
            JsonObject pool = pools.get(i).getAsJsonObject();
            if (pool.has("rolls")) {
                checkNumberProvider(pool.get("rolls"), rel + " pool[" + i + "].rolls", problems);
            }
            if (pool.has("bonus_rolls")) {
                checkNumberProvider(pool.get("bonus_rolls"), rel + " pool[" + i + "].bonus_rolls", problems);
            }
            if (!pool.has("entries")) {
                problems.add(rel + " pool[" + i + "] missing entries");
                continue;
            }
            walkEntries(pool.getAsJsonArray("entries"), rel, modItems, problems);
        }
    }

    private void walkEntries(JsonArray entries, String rel, Set<String> modItems, List<String> problems) {
        for (JsonElement el : entries) {
            JsonObject e = el.getAsJsonObject();
            String type = e.has("type") ? e.get("type").getAsString() : "";
            if ("minecraft:item".equals(type) && e.has("name")) {
                String name = e.get("name").getAsString();
                if (name.startsWith("israel_simulator:")) {
                    String id = name.substring("israel_simulator:".length());
                    if (!modItems.contains(id)) {
                        problems.add(rel + ": unknown mod item " + name);
                    }
                } else if (!name.startsWith("minecraft:")) {
                    problems.add(rel + ": unexpected namespace in " + name);
                }
            }
            if (e.has("functions")) {
                for (JsonElement fEl : e.getAsJsonArray("functions")) {
                    JsonObject f = fEl.getAsJsonObject();
                    if ("minecraft:set_count".equals(f.get("function").getAsString()) && f.has("count")) {
                        checkNumberProvider(f.get("count"), rel + " set_count", problems);
                    }
                    if ("minecraft:enchanted_count_increase".equals(
                            f.has("function") ? f.get("function").getAsString() : "")
                            && f.has("count")) {
                        checkNumberProvider(f.get("count"), rel + " enchanted_count_increase", problems);
                    }
                }
            }
            if (e.has("children")) {
                walkEntries(e.getAsJsonArray("children"), rel, modItems, problems);
            }
            if (e.has("entries")) {
                walkEntries(e.getAsJsonArray("entries"), rel, modItems, problems);
            }
        }
    }

    private void checkNumberProvider(JsonElement el, String where, List<String> problems) {
        if (el.isJsonPrimitive() && (el.getAsJsonPrimitive().isNumber())) {
            return; // constant rolls like 1.0
        }
        if (!el.isJsonObject()) {
            problems.add(where + ": expected number or object, got " + el);
            return;
        }
        JsonObject obj = el.getAsJsonObject();
        if (!obj.has("type")) {
            problems.add(where + ": legacy {min,max} without type (need minecraft:uniform): " + obj);
            return;
        }
        String type = obj.get("type").getAsString();
        if ("minecraft:uniform".equals(type)) {
            if (!obj.has("min") || !obj.has("max")) {
                problems.add(where + ": uniform missing min/max");
            }
        }
    }

    private Set<String> loadModItemIds() throws Exception {
        Set<String> ids = new HashSet<>();
        String items = Files.readString(Path.of("src/main/java/com/israelsimulator/registry/ModItems.java"));
        String blocks = Files.readString(Path.of("src/main/java/com/israelsimulator/registry/ModBlocks.java"));
        Matcher m = REGISTER_ITEM.matcher(items + "\n" + blocks);
        while (m.find()) {
            ids.add(m.group(1));
        }
        // Block registrations without SimpleBlockItem still may appear as loot (grapevine drops grapes item)
        Pattern blockOnly = Pattern.compile("registerBlock\\(\\s*\"([a-z0-9_]+)\"");
        Matcher bm = blockOnly.matcher(blocks);
        while (bm.find()) {
            ids.add(bm.group(1));
        }
        assertFalse(ids.isEmpty(), "Expected mod item registrations");
        return ids;
    }
}
