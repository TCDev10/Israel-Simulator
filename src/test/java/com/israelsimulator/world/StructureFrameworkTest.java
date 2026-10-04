package com.israelsimulator.world;

import com.israelsimulator.world.structure.ModStructures;
import java.io.InputStream;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructureFrameworkTest {

    @Test
    @DisplayName("Verify all structure keys are registered with israel_simulator namespace")
    void testStructureKeys() {
        List<ResourceKey<Structure>> structures = ModStructures.allStructures();
        assertNotNull(structures);
        assertEquals(7, structures.size(), "Should register 7 structures in framework");

        for (ResourceKey<Structure> structure : structures) {
            assertEquals("israel_simulator", structure.identifier().getNamespace());
            assertFalse(structure.identifier().getPath().isBlank());
        }
    }

    @Test
    @DisplayName("Verify chest loot tables are accessible on the classpath")
    void testChestLootTablesExist() {
        List<String> lootTables = List.of(
                "mediterranean_village",
                "agricultural_farm",
                "dead_sea_resort",
                "desert_ruins",
                "synagogue",
                "tel_aviv_tech_office",
                "tel_aviv_apartment",
                "ein_gedi_oasis"
        );

        for (String name : lootTables) {
            String path = "data/israel_simulator/loot_table/chests/" + name + ".json";
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            assertNotNull(stream, "Loot table resource must exist at: " + path);
        }
    }
}
