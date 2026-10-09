package com.israelsimulator.deadsea;

import com.israelsimulator.item.deadsea.DeadSeaMudItem;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dead Sea mineral mud: 30 s item cooldown on successful use and its own texture.
 */
class DeadSeaMudItemTest {
    private static final Path SRC = Path.of("src/main/java/com/israelsimulator/item/deadsea/DeadSeaMudItem.java");

    @Test
    @DisplayName("Cooldown is 600 ticks (30 s)")
    void cooldownIs600Ticks() {
        assertEquals(600, DeadSeaMudItem.COOLDOWN_TICKS);
    }

    @Test
    @DisplayName("use() applies the item cooldown, refuses while cooling down, and sets it before the stack shrinks")
    void cooldownAppliedOnUse() throws Exception {
        String src = Files.readString(SRC);
        int use = src.indexOf("public InteractionResult use(");
        assertTrue(use >= 0);
        String body = src.substring(use);
        int check = body.indexOf("getCooldowns().isOnCooldown(itemStack)");
        int add = body.indexOf("getCooldowns().addCooldown(itemStack, COOLDOWN_TICKS)");
        int shrink = body.indexOf("itemStack.shrink(1)");
        assertTrue(check >= 0, "must refuse use while on cooldown");
        assertTrue(add > check, "must add the cooldown after the on-cooldown check (only successful uses)");
        assertTrue(shrink > add, "cooldown must be set before the stack is consumed");
        String guard = body.substring(check, add);
        assertTrue(guard.contains("return InteractionResult.FAIL"), guard);
    }

    @Test
    @DisplayName("Item model references the mod's own 16x16 mud texture, not vanilla clay")
    void textureReferenced() throws Exception {
        String model = Files.readString(Path.of(
                "src/main/resources/assets/israel_simulator/models/item/dead_sea_mud.json"));
        assertTrue(model.contains("israel_simulator:item/dead_sea_mud"), model);
        assertFalse(model.contains("clay_ball"), model);
        Path png = Path.of("src/main/resources/assets/israel_simulator/textures/item/dead_sea_mud.png");
        BufferedImage img = ImageIO.read(png.toFile());
        assertEquals(16, img.getWidth());
        assertEquals(16, img.getHeight());
        int opaque = 0;
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) if ((img.getRGB(x, y) >>> 24) != 0) opaque++;
        assertTrue(opaque > 60, "texture looks empty");
    }
}
