package com.israelsimulator.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Every shaped recipe must fit a 3x3 grid and only use keyed symbols (vanilla rejects the file otherwise). */
class ShapedRecipePatternTest {
    @Test
    @DisplayName("Shaped recipe patterns are at most 3x3 and fully keyed")
    void shapedPatternsFitTheGrid() throws Exception {
        Path root = Path.of(getClass().getClassLoader().getResource("data/israel_simulator/recipe").toURI());
        List<Path> files;
        try (var s = Files.walk(root)) {
            files = s.filter(p -> p.toString().endsWith(".json")).toList();
        }
        int shaped = 0;
        for (Path f : files) {
            JsonObject r = JsonParser.parseString(Files.readString(f)).getAsJsonObject();
            if (!"minecraft:crafting_shaped".equals(r.get("type").getAsString())) continue;
            shaped++;
            var pattern = r.getAsJsonArray("pattern");
            assertTrue(pattern.size() >= 1 && pattern.size() <= 3, f.getFileName() + ": too many rows");
            JsonObject key = r.getAsJsonObject("key");
            for (JsonElement row : pattern) {
                String line = row.getAsString();
                assertTrue(line.length() >= 1 && line.length() <= 3, f.getFileName() + ": row '" + line + "' too wide");
                for (char c : line.toCharArray()) {
                    assertTrue(c == ' ' || key.has(String.valueOf(c)), f.getFileName() + ": unkeyed symbol " + c);
                }
            }
        }
        assertTrue(shaped > 0, "expected shaped recipes");
    }
}
