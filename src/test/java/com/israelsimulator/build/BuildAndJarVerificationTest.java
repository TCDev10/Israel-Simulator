package com.israelsimulator.build;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates build artifacts, JAR structure, and metadata per GAME_DESIGN.md §61.
 */
public class BuildAndJarVerificationTest {

    @Test
    public void testJarArtifactIntegrityIfPresent() throws IOException {
        Path libsDir = Path.of("build/libs");
        if (!Files.exists(libsDir)) {
            return; // Skip if clean build has not run yet
        }

        try (var stream = Files.list(libsDir)) {
            var jarOpt = stream
                    .filter(p -> p.toString().endsWith(".jar"))
                    .filter(p -> !p.toString().endsWith("-sources.jar"))
                    .findFirst();

            if (jarOpt.isPresent()) {
                Path jarPath = jarOpt.get();
                assertTrue(Files.size(jarPath) > 50_000, "Mod JAR must be non-empty and reasonably sized (>50KB)");

                Set<String> entries = new HashSet<>();
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(jarPath))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        entries.add(entry.getName());
                    }
                }

                // Verify core entries exist
                assertTrue(entries.contains("META-INF/MANIFEST.MF"), "JAR must have manifest");
                assertTrue(entries.contains("META-INF/neoforge.mods.toml"), "JAR must contain neoforge.mods.toml");
                assertTrue(entries.stream().anyMatch(e -> e.startsWith("com/israelsimulator/IsraelSimulator.class")),
                        "JAR must contain root mod class");
                assertTrue(entries.stream().anyMatch(e -> e.startsWith("assets/israel_simulator/")),
                        "JAR must bundle asset resources");
                assertTrue(entries.stream().anyMatch(e -> e.startsWith("data/israel_simulator/")),
                        "JAR must bundle data resources");
            }
        }
    }

    @Test
    public void testModMetadataFileValid() throws IOException {
        Path metaPath = Path.of("src/main/templates/META-INF/neoforge.mods.toml");
        assertTrue(Files.exists(metaPath), "neoforge.mods.toml template must exist");

        String content = Files.readString(metaPath);
        assertTrue(content.contains("modId=\"${mod_id}\""), "Template must define modId placeholder");
        assertTrue(content.contains("license=\"${mod_license}\""), "Template must define license placeholder");
    }
}
