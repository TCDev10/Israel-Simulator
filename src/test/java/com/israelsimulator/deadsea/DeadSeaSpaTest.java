package com.israelsimulator.deadsea;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DeadSeaSpaTest {
    @Test
    void timingsAreSane() {
        assertTrue(DeadSeaSpa.SOAK_SECONDS > 0 && DeadSeaSpa.SOAK_SECONDS <= DeadSeaSpa.FLOAT_SECONDS);
        assertTrue(DeadSeaSpa.BATH_COOLDOWN_TICKS >= 20 * 60, "a mud bath is not spammable");
    }

    @Test
    void messagesExistInBothLanguages() throws Exception {
        for (String lang : new String[] {"en_us", "it_it"}) {
            JsonObject j = JsonParser.parseString(Files.readString(
                    Path.of("src/main/resources/assets/israel_simulator/lang/" + lang + ".json"))).getAsJsonObject();
            for (String k : new String[] {"mud_bath_start", "mud_bath_done", "salt_float"}) {
                assertTrue(j.has("message.israel_simulator.dead_sea." + k), lang + " " + k);
            }
        }
    }
}
