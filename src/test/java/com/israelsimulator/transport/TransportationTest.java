package com.israelsimulator.transport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransportationTest {

    @Test
    @DisplayName("Verify TransportType enum parameters (fares and speed factors)")
    void testTransportTypes() {
        assertEquals(0, TransportType.WALKING.getFareInShekels());
        assertTrue(TransportType.WALKING.getSpeedFactor() > 1.0F);

        assertEquals(0, TransportType.BICYCLE.getFareInShekels());
        assertTrue(TransportType.BICYCLE.getSpeedFactor() > TransportType.WALKING.getSpeedFactor());

        assertEquals(5, TransportType.BUS.getFareInShekels());
        assertEquals(10, TransportType.TRAIN.getFareInShekels());
        assertEquals(8, TransportType.TAXI.getFareInShekels());
        assertEquals(6, TransportType.BOAT.getFareInShekels());
    }

    @Test
    @DisplayName("Verify TransportNetwork connectivity and routing across Israeli regions")
    void testTransportNetwork() {
        List<TransportNetwork.TransportStop> stops = TransportNetwork.getAllStops();
        assertFalse(stops.isEmpty(), "Transit network must contain registered stops");

        TransportNetwork.TransportStop telAviv = TransportNetwork.getStop("tel_aviv_central");
        assertNotNull(telAviv, "Tel Aviv Central must be registered");
        assertEquals("Tel Aviv", telAviv.cityRegion());

        TransportNetwork.TransportStop jerusalem = TransportNetwork.getStop("jerusalem_navon");
        assertNotNull(jerusalem, "Jerusalem Navon must be registered");
        assertEquals("Jerusalem", jerusalem.cityRegion());

        TransportNetwork.TransportStop jaffa = TransportNetwork.getStop("jaffa_clock_tower");
        assertNotNull(jaffa, "Jaffa Clock Tower stop must be registered");

        TransportNetwork.TransportStop deadSea = TransportNetwork.getStop("dead_sea_resort");
        assertNotNull(deadSea, "Dead Sea resort stop must be registered");

        // Test route succession
        TransportNetwork.TransportStop next = TransportNetwork.getNextStop("tel_aviv_central");
        assertNotNull(next, "Next stop from Tel Aviv must exist");
        assertNotEquals("tel_aviv_central", next.id());
    }

    @Test
    @DisplayName("Verify transportation items, blocks, and entity registrations")
    void testRegistrations() throws Exception {
        ClassLoader cl = TransportationTest.class.getClassLoader();
        Class<?> blockClass = Class.forName("com.israelsimulator.registry.ModBlocks", false, cl);
        Class<?> itemClass = Class.forName("com.israelsimulator.registry.ModItems", false, cl);
        Class<?> entityClass = Class.forName("com.israelsimulator.registry.ModEntities", false, cl);

        assertNotNull(blockClass.getField("PAVED_ROAD"), "PAVED_ROAD block must be declared in ModBlocks");
        assertNotNull(blockClass.getField("TRANSPORT_STOP"), "TRANSPORT_STOP block must be declared in ModBlocks");
        assertNotNull(itemClass.getField("BICYCLE"), "BICYCLE item must be declared in ModItems");
        assertNotNull(itemClass.getField("RAV_KAV"), "RAV_KAV item must be declared in ModItems");
        assertNotNull(itemClass.getField("WALKING_SHOES"), "WALKING_SHOES item must be declared in ModItems");
        assertNotNull(entityClass.getField("BICYCLE"), "BICYCLE entity type must be declared in ModEntities");
    }
}
