package com.israelsimulator.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every item registered by name in the mod's registries needs an item definition
 * (assets/israel_simulator/items/&lt;id&gt;.json) pointing at an existing model, otherwise the
 * client logs "Missing item model" and shows the purple/black icon (chickpeas, tomato,
 * eggplant and sesame_seeds had none).
 */
class ItemDefinitionCoverageTest {

    private static final Pattern REG = Pattern.compile(
            "register(?:Simple)?(?:Block)?Item\\(\\s*\"([a-z0-9_]+)\"");
    private static final Pattern MODEL = Pattern.compile("\"model\"\\s*:\\s*\"israel_simulator:([a-z0-9_/]+)\"");

    @Test
    @DisplayName("Every registered item has an item definition with an existing model")
    void everyItemHasDefinition() throws Exception {
        TreeSet<String> ids = new TreeSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path p : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".java"))::iterator) {
                Matcher m = REG.matcher(Files.readString(p));
                while (m.find()) ids.add(m.group(1));
            }
        }
        assertTrue(ids.size() > 50, "registry scan found too few items: " + ids.size());
        assertTrue(ids.containsAll(List.of("chickpeas", "tomato", "eggplant", "sesame_seeds")));
        Path assets = Path.of("src/main/resources/assets/israel_simulator");
        List<String> problems = new ArrayList<>();
        for (String id : ids) {
            Path def = assets.resolve("items/" + id + ".json");
            if (!Files.exists(def)) {
                problems.add(id + ": no items/" + id + ".json");
                continue;
            }
            Matcher m = MODEL.matcher(Files.readString(def));
            while (m.find()) {
                if (!Files.exists(assets.resolve("models/" + m.group(1) + ".json"))) {
                    problems.add(id + ": model " + m.group(1) + " missing");
                }
            }
        }
        assertTrue(problems.isEmpty(), "item definitions: " + problems);
    }
}
