package com.israelsimulator.festival;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FestivalEventsTest {
    @Test
    void everyFestivalHasBeginAndEndMessagesInBothLanguages() throws Exception {
        for (String lang : new String[] {"en_us", "it_it"}) {
            JsonObject j = JsonParser.parseString(Files.readString(
                    Path.of("src/main/resources/assets/israel_simulator/lang/" + lang + ".json"))).getAsJsonObject();
            for (FestivalType t : FestivalType.values()) {
                assertTrue(j.has(FestivalEvents.beginKey(t)), lang + " " + t);
                assertTrue(j.has(FestivalEvents.endKey(t)), lang + " " + t);
                assertNotEquals(FestivalEvents.beginKey(t), FestivalEvents.endKey(t));
            }
        }
    }
}
