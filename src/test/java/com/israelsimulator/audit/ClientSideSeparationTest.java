package com.israelsimulator.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NeoForge deprecated {@code @OnlyIn} (runtime member stripping) and logs a warning on every
 * launch while it is present. Client-only code must instead live in the {@code client}
 * packages, be registered from the client entry point / {@code @EventBusSubscriber(value = Dist.CLIENT)},
 * and never be referenced from common or server code.
 */
public class ClientSideSeparationTest {

    private static final Path MAIN_JAVA = Paths.get("src", "main", "java");
    private static final Pattern ONLY_IN = Pattern.compile("@OnlyIn\\b|import\\s+net\\.neoforged\\.api\\.distmarker\\.OnlyIn\\s*;");
    private static final Pattern CLIENT_REFERENCE = Pattern.compile(
            "\\bcom\\.israelsimulator\\.client\\.|\\bnet\\.minecraft\\.client\\.|\\bnet\\.neoforged\\.neoforge\\.client\\.");

    private static List<Path> javaSources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN_JAVA)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static boolean isClientOnly(Path file) {
        Path rel = MAIN_JAVA.relativize(file);
        for (Path part : rel) {
            if (part.toString().equals("client")) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("No source file uses the deprecated @OnlyIn annotation")
    void noOnlyInAnnotations() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : javaSources()) {
            if (ONLY_IN.matcher(Files.readString(file)).find()) {
                offenders.add(file.toString());
            }
        }
        assertTrue(offenders.isEmpty(), "@OnlyIn is deprecated by NeoForge, remove it from: " + offenders);
    }

    @Test
    @DisplayName("Common/server code never references client-only classes")
    void commonCodeDoesNotReferenceClientClasses() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : javaSources()) {
            if (isClientOnly(file)) {
                continue;
            }
            if (CLIENT_REFERENCE.matcher(Files.readString(file)).find()) {
                offenders.add(file.toString());
            }
        }
        assertTrue(offenders.isEmpty(), "client-only classes referenced from common code: " + offenders);
    }
}
