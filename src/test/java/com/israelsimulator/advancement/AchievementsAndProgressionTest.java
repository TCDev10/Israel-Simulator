package com.israelsimulator.advancement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
}
