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

/** Humanoid mob renderers must point at their own skin and must not use AvatarRenderState (26.2 sends those to the vanilla player renderer, which draws the default Steve skin). */
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
    void noRendererUsesAvatarRenderState() throws Exception {
        Class<?>[] renderers = {OratorRenderer.class, MossadAgentRenderer.class, MossadNpcRenderer.class, BibiBossRenderer.class,
                BibiGuardRenderer.class, JeffreyEpsteinRenderer.class, TrumpMinibossRenderer.class, MoneyChangerRenderer.class,
                ChildZombieMinionRenderer.class, IceAgentRenderer.class};
        for (Class<?> c : renderers) {
            Class<?> state = c.getMethod("createRenderState").getReturnType();
            assertFalse(net.minecraft.client.renderer.entity.state.AvatarRenderState.class.isAssignableFrom(state),
                    c.getSimpleName() + " uses AvatarRenderState: it would render with the default Steve skin");
        }
    }

    @Test
    void renderersUseOwnTexture() throws Exception {
        for (var e : textures().entrySet()) {
            Identifier id = e.getValue();
            assertEquals(IsraelSimulator.MOD_ID, id.getNamespace(), e.getKey());
            assertEquals("textures/entity/" + e.getKey() + ".png", id.getPath(), e.getKey());
            Path png = ASSETS.resolve(id.getPath());
            assertTrue(Files.exists(png), "missing " + png);
            BufferedImage img = ImageIO.read(png.toFile());
            assertEquals(64, img.getWidth(), e.getKey());
            assertEquals(64, img.getHeight(), e.getKey());
        }
    }
}
