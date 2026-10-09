package com.israelsimulator.entity.boss;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Bibi is immune to fire and lava. */
class BibiFireImmunityTest {
    @Test
    @DisplayName("EntityType is fireImmune and hurtServer ignores fire/lava damage")
    void fireImmune() throws Exception {
        String reg = Files.readString(Path.of("src/main/java/com/israelsimulator/registry/ModEntities.java"));
        int i = reg.indexOf("\"bibi_boss\"");
        assertTrue(i > 0);
        assertTrue(reg.substring(i, reg.indexOf(';', i)).contains(".fireImmune()"));
        String src = Files.readString(Path.of("src/main/java/com/israelsimulator/entity/boss/BibiBossEntity.java"));
        assertTrue(src.contains("DamageTypeTags.IS_FIRE"));
        assertTrue(src.contains("DamageTypes.LAVA"));
        int h = src.indexOf("public boolean hurtServer(");
        int g = src.indexOf("isFireOrLavaDamage(damageSource)", h);
        assertTrue(g > h && g - h < 300, "fire check must be the first thing in hurtServer");
    }
}
