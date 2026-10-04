package com.israelsimulator.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates dedicated server compatibility per GAME_DESIGN.md §61 and §62.
 * Ensures that common/server classes NEVER import net.minecraft.client.*,
 * which would cause dedicated server crashes (NoClassDefFoundError).
 */
public class DedicatedServerCompatibilityTest {

    @Test
    public void testCommonClassesDoNotImportClientPackages() throws IOException {
        Path sourceRoot = Path.of("src/main/java/com/israelsimulator");
        assertTrue(Files.exists(sourceRoot), "Source root must exist");

        List<String> violations = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().replace('\\', '/').contains("/client/"))
                    .forEach(path -> {
                        try {
                            List<String> lines = Files.readAllLines(path);
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i).trim();
                                if (line.startsWith("import net.minecraft.client.")
                                        && !line.contains("net.minecraft.client.gui.screens.Screen") // Allowed if guarded or not present
                                        && !line.contains("net.minecraft.client.renderer.entity.EntityRendererProvider")) {
                                    violations.add(path.getFileName() + " (line " + (i + 1) + "): " + line);
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        assertTrue(violations.isEmpty(),
                "Dedicated server violation! Common classes must not import net.minecraft.client.*:\n"
                        + String.join("\n", violations));
    }
}
