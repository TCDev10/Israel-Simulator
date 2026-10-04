package com.israelsimulator.audit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Asset and Licensing Audit Test Suite (GAME_DESIGN.md §58, TODO §58).
 * Ensures CREDITS.md and ASSET_LICENSES.md are up-to-date, transparent, and accurate,
 * and that all texture and binary assets are valid and non-corrupt.
 */
public class AssetAndLicensingAuditTest {

    private static final byte[] PNG_HEADER = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final Path TEXTURES_PATH = Paths.get("src", "main", "resources", "assets", "israel_simulator", "textures");

    @Test
    @DisplayName("Verify CREDITS.md exists and contains required attribution sections")
    void testCreditsDocumentation() throws IOException {
        Path creditsPath = Paths.get("CREDITS.md");
        assertTrue(Files.exists(creditsPath), "CREDITS.md must exist in root");

        String content = Files.readString(creditsPath);
        assertTrue(content.contains("Credits & Attribution"), "Must contain main title");
        assertTrue(content.contains("Development Team"), "Must credit developers");
        assertTrue(content.contains("Minecraft Java Edition"), "Must acknowledge Minecraft");
        assertTrue(content.contains("NeoForge"), "Must acknowledge NeoForge");
        assertTrue(content.contains("Audio & Musical Heritage"), "Must credit audio/music heritage");
        assertTrue(content.contains("Hava Nagila"), "Must reference Hava Nagila");
        assertTrue(content.contains("Klezmer"), "Must reference Klezmer");
        assertTrue(content.contains("Shabbat Shalom"), "Must reference Shabbat Shalom");
    }

    @Test
    @DisplayName("Verify ASSET_LICENSES.md exists and details licenses for audio, textures, and models")
    void testAssetLicensesDocumentation() throws IOException {
        Path licensesPath = Paths.get("ASSET_LICENSES.md");
        assertTrue(Files.exists(licensesPath), "ASSET_LICENSES.md must exist in root");

        String content = Files.readString(licensesPath);
        assertTrue(content.contains("Asset Licenses & Provenance Audit"), "Must have audit title");
        assertTrue(content.contains("Sound & Audio Assets"), "Must cover sound assets");
        assertTrue(content.contains("Texture Assets"), "Must cover texture assets");
        assertTrue(content.contains("3D Models and Geometry"), "Must cover 3D models");
        assertTrue(content.contains("AGPL-3.0"), "Must state AGPL-3.0 license");
        assertTrue(content.contains("Mojang EULA"), "Must confirm Mojang EULA adherence");
        assertTrue(content.contains("bibi_boss.png"), "Must explicitly inventory bibi_boss.png");
        assertTrue(content.contains("rabbis_crown.json"), "Must explicitly inventory rabbis_crown.json");
    }

    @Test
    @DisplayName("Verify all PNG textures have valid PNG header magic bytes and are non-empty")
    void testTextureFilesValidity() throws IOException {
        assertTrue(Files.exists(TEXTURES_PATH), "Textures directory must exist");

        try (Stream<Path> stream = Files.walk(TEXTURES_PATH)) {
            List<Path> textures = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".png"))
                    .toList();

            assertFalse(textures.isEmpty(), "Textures must be present");

            for (Path pngPath : textures) {
                long size = Files.size(pngPath);
                assertTrue(size > 8, "PNG file must be non-empty and larger than header: " + pngPath);

                byte[] header = new byte[8];
                try (InputStream in = Files.newInputStream(pngPath)) {
                    int bytesRead = in.read(header);
                    assertTrue(bytesRead == 8, "Failed reading PNG header for: " + pngPath);
                }
                assertArrayEquals(PNG_HEADER, header, "Corrupted or non-PNG file at: " + pngPath);
            }
        }
    }
}
