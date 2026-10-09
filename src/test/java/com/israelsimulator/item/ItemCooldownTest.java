package com.israelsimulator.item;

import com.israelsimulator.festival.DreidelManager;
import com.israelsimulator.item.cultural.FirstAmendmentItem;
import com.israelsimulator.item.cultural.HavaNagilaDiscItem;
import com.israelsimulator.transport.RavKavItem;
import com.israelsimulator.world.map.IsraelMapItem;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Right-click items that used to be spammable now set a vanilla item cooldown on a successful use.
 */
class ItemCooldownTest {
    private static final Path MAIN = Path.of("src/main/java/com/israelsimulator");

    @Test
    @DisplayName("Cooldown values: Dreidel 10 s, First Amendment 120 s, Hava Nagila 5 s, Map and Rav-Kav 2 s")
    void cooldownValues() {
        assertEquals(200, DreidelManager.COOLDOWN_TICKS);
        assertEquals(2400, FirstAmendmentItem.COOLDOWN_TICKS);
        assertEquals(100, HavaNagilaDiscItem.COOLDOWN_TICKS);
        assertEquals(40, IsraelMapItem.COOLDOWN_TICKS);
        assertEquals(40, RavKavItem.COOLDOWN_TICKS);
    }

    @Test
    @DisplayName("First Amendment cooldown outlasts its 60 s Freedom effect")
    void firstAmendmentOutlastsEffect() throws Exception {
        String src = read("item/cultural/FirstAmendmentItem.java");
        assertTrue(src.contains("ModEffects.FREEDOM, 1200"), "effect is 60 s");
        assertTrue(FirstAmendmentItem.COOLDOWN_TICKS > 1200);
    }

    @Test
    @DisplayName("Each item calls getCooldowns().addCooldown(..., COOLDOWN_TICKS) inside use()")
    void cooldownAppliedInUse() throws Exception {
        for (String f : new String[] {"item/festival/DreidelItem.java", "item/cultural/FirstAmendmentItem.java",
                "item/cultural/HavaNagilaDiscItem.java", "transport/RavKavItem.java", "world/map/IsraelMapItem.java"}) {
            String use = useBody(read(f));
            assertTrue(use.contains("getCooldowns().addCooldown("), f + " must add an item cooldown");
            assertTrue(use.contains("COOLDOWN_TICKS)"), f);
        }
    }

    @Test
    @DisplayName("Dreidel: no cooldown and no spin without the stake; cooldown only after a real spin")
    void dreidelCooldownOnlyOnSuccess() throws Exception {
        String use = useBody(read("item/festival/DreidelItem.java"));
        int fail = use.indexOf("return InteractionResult.FAIL");
        int spin = use.indexOf("DreidelManager.spin(");
        int cd = use.indexOf("addCooldown(");
        assertTrue(fail >= 0 && fail < spin, "missing-stake check must come before the spin");
        assertTrue(cd > spin, "cooldown only after a successful spin");
        assertTrue(use.contains("removeShekels(player, stake)"));
    }

    @Test
    @DisplayName("Dreidel odds are fair: expected net payout 0, Shin loses exactly the stake")
    void dreidelOddsAreFair() {
        assertEquals(0.0, DreidelManager.expectedNetPayout(), 1e-9);
        assertEquals(3, DreidelManager.STAKE);
        assertEquals(0, DreidelManager.grossReturn(DreidelManager.DreidelLetter.SHIN));
        assertEquals(3, DreidelManager.grossReturn(DreidelManager.DreidelLetter.NUN));
        assertEquals(4, DreidelManager.grossReturn(DreidelManager.DreidelLetter.HEI));
        assertEquals(5, DreidelManager.grossReturn(DreidelManager.DreidelLetter.GIMEL));
        for (DreidelManager.DreidelLetter l : DreidelManager.DreidelLetter.values()) {
            assertTrue(DreidelManager.grossReturn(l) >= 0);
        }
    }

    private static String read(String rel) throws Exception {
        return Files.readString(MAIN.resolve(rel));
    }

    private static String useBody(String src) {
        int start = src.indexOf("public InteractionResult use(");
        assertTrue(start >= 0);
        int next = src.indexOf("\n    @Override", start);
        int priv = src.indexOf("\n    private ", start);
        int end = src.length();
        if (next > 0) end = Math.min(end, next);
        if (priv > 0) end = Math.min(end, priv);
        return src.substring(start, end);
    }
}
