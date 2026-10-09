package com.israelsimulator.localization;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 * Every translation key written in the Java sources (e.g. Component.translatable("message.israel_simulator.x"))
 * must exist in both en_us.json and it_it.json, so chat never shows a raw key.
 */
class TranslationKeysInCodeTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path LANG = Path.of("src/main/resources/assets/israel_simulator/lang");
    /** Any "<category>.israel_simulator.<path>" string literal in the code. */
    private static final Pattern KEY = Pattern.compile("\"([a-z]+\\.israel_simulator\\.[a-z0-9_.]+)\"");
    /** Keys built by concatenation: Component.translatable("prefix" + index). */
    private static final Pattern DYNAMIC = Pattern.compile("translatable\\(\\s*\"([a-z]+\\.israel_simulator\\.[a-z0-9_.]+)\"\\s*\\+");

    @Test
    @DisplayName("Every translation key used in the code exists in en_us.json and it_it.json")
    void allCodeKeysTranslated() throws Exception {
        JsonObject en = lang("en_us");
        JsonObject it = lang("it_it");
        TreeSet<String> keys = new TreeSet<>();
        TreeSet<String> prefixes = new TreeSet<>();
        try (Stream<Path> files = Files.walk(JAVA)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String src = Files.readString(f);
                Matcher m = KEY.matcher(src);
                while (m.find()) {
                    keys.add(m.group(1));
                }
                Matcher d = DYNAMIC.matcher(src);
                while (d.find()) {
                    prefixes.add(d.group(1));
                }
            }
        }
        assertTrue(keys.contains("message.israel_simulator.bibi_shielded_by_epstein"), "scanner sanity check");
        keys.removeAll(prefixes);

        List<String> missing = new ArrayList<>();
        for (String k : keys) {
            if (!en.has(k)) missing.add("en_us: " + k);
            if (!it.has(k)) missing.add("it_it: " + k);
        }
        for (String p : prefixes) {
            // quote_ prefixes are used with random.nextInt(3): 0..2 must all exist
            for (int i = 0; i < 3; i++) {
                if (!en.has(p + i)) missing.add("en_us: " + p + i);
                if (!it.has(p + i)) missing.add("it_it: " + p + i);
            }
        }
        assertTrue(missing.isEmpty(), "Missing translations:\n" + String.join("\n", missing));
    }

    @Test
    @DisplayName("Bibi's Epstein shield message is translated")
    void bibiShieldMessage() throws Exception {
        assertEquals("Bibi is shielded by Epstein! Defeat him first.",
                lang("en_us").get("message.israel_simulator.bibi_shielded_by_epstein").getAsString());
        assertEquals("Bibi è protetto da Epstein! Sconfiggilo prima.",
                lang("it_it").get("message.israel_simulator.bibi_shielded_by_epstein").getAsString());
    }

    private static JsonObject lang(String name) throws Exception {
        return JsonParser.parseString(Files.readString(LANG.resolve(name + ".json"))).getAsJsonObject();
    }
}
