package com.israelsimulator.transport;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainLineTest {

    @Test
    @DisplayName("The train links Tel Aviv (Urban Area) and Jerusalem, both ways")
    void terminalsAreTheTwoCities() {
        assertEquals(TrainLine.Terminal.JERUSALEM, TrainLine.Terminal.TEL_AVIV.other());
        assertEquals(TrainLine.Terminal.TEL_AVIV, TrainLine.Terminal.JERUSALEM.other());
        assertEquals("israel_simulator:urban_area", TrainLine.Terminal.TEL_AVIV.biome().identifier().toString());
        assertEquals("israel_simulator:jerusalem", TrainLine.Terminal.JERUSALEM.biome().identifier().toString());
        assertEquals(TransportType.TRAIN.getFareInShekels(), TrainLine.FARE);
    }

    @Test
    @DisplayName("Each end has a structure tag with its generated city, used when no station is built there")
    void cityTagsPointAtTheCities() throws Exception {
        for (TrainLine.Terminal t : TrainLine.Terminal.values()) {
            String path = "data/israel_simulator/tags/worldgen/structure/train/" + t.id() + ".json";
            InputStream in = getClass().getClassLoader().getResourceAsStream(path);
            assertNotNull(in, path);
            JsonObject json = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            assertEquals("israel_simulator:" + t.id() + "_city", json.getAsJsonArray("values").get(0).getAsString());
            assertEquals("israel_simulator:train/" + t.id(), t.cities().location().toString());
        }
    }

    @Test
    @DisplayName("Stations are saved with the world and keep no duplicates")
    void stationsPersist() {
        TransitStations s = new TransitStations(List.of(new BlockPos(1, 64, 2), new BlockPos(1, 64, 2), new BlockPos(5, 70, -3)));
        assertEquals(2, s.list().size());
        s.add(new BlockPos(9, 9, 9));
        s.remove(new BlockPos(1, 64, 2));
        assertEquals(List.of(new BlockPos(5, 70, -3), new BlockPos(9, 9, 9)), s.list());
        var json = TransitStations.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, s).getOrThrow();
        TransitStations back = TransitStations.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(s.list(), back.list());
    }
}
