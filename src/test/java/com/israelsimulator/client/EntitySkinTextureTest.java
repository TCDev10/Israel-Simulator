package com.israelsimulator.client;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.client.renderer.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Humanoid mob renderers must point at their own skin, and that skin must not be a copy of vanilla Steve. */
public class EntitySkinTextureTest {

    private static final Path ASSETS = Path.of("src", "main", "resources", "assets", IsraelSimulator.MOD_ID);

    private static Map<String, Identifier> textures() {
        Map<String, Identifier> m = new LinkedHashMap<>();
        m.put("orator", OratorRenderer.TEXTURE);
        m.put("mossad_agent", MossadAgentRenderer.TEXTURE);
        m.put("mossad_handler", MossadNpcRenderer.textureFor("mossad_handler"));
        m.put("mossad_informant", MossadNpcRenderer.textureFor("mossad_informant"));
        m.put("bibi_boss", BibiBossRenderer.TEXTURE);
        m.put("bibi_guard", BibiGuardRenderer.TEXTURE);
        m.put("jeffrey_epstein", JeffreyEpsteinRenderer.TEXTURE);
        m.put("trump_miniboss", TrumpMinibossRenderer.TEXTURE);
        m.put("money_changer", MoneyChangerRenderer.TEXTURE);
        m.put("child_zombie", ChildZombieMinionRenderer.TEXTURE);
        m.put("ice_agent", IceAgentRenderer.TEXTURE);
        return m;
    }

    @Test
    void renderersUseOwnTextureAndSkinIsNotSteve() throws Exception {
        BufferedImage steve;
        try (InputStream in = Identifier.class.getClassLoader()
                .getResourceAsStream("assets/minecraft/textures/entity/player/wide/steve.png")) {
            assertNotNull(in, "vanilla steve.png must be on the test classpath");
            steve = ImageIO.read(in);
        }
        for (var e : textures().entrySet()) {
            Identifier id = e.getValue();
            assertEquals(IsraelSimulator.MOD_ID, id.getNamespace(), e.getKey());
            assertEquals("textures/entity/" + e.getKey() + ".png", id.getPath(), e.getKey());
            Path png = ASSETS.resolve(id.getPath());
            assertTrue(Files.exists(png), "missing " + png);
            BufferedImage img = ImageIO.read(png.toFile());
            assertEquals(64, img.getWidth(), e.getKey());
            assertEquals(64, img.getHeight(), e.getKey());
            // Face (front of head, 8..16 x 8..16) must differ from Steve's.
            boolean differs = false;
            for (int x = 8; x < 16 && !differs; x++)
                for (int y = 8; y < 16 && !differs; y++)
                    differs = img.getRGB(x, y) != steve.getRGB(x, y);
            assertTrue(differs, e.getKey() + " skin face is a copy of vanilla Steve");
        }
    }
}
