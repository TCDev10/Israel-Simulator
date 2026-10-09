package com.israelsimulator.item.cultural;

import com.israelsimulator.reputation.ReputationScores;
import com.israelsimulator.trading.IsraelVillagerTrades;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RabbisCrownAndBlessedTraderTest {

    @Test
    @DisplayName("Verify RabbisCrownItem class exists and extends Item")
    void testRabbisCrownClassStructure() throws Exception {
        ClassLoader cl = RabbisCrownAndBlessedTraderTest.class.getClassLoader();
        Class<?> crownClass = Class.forName("com.israelsimulator.item.cultural.RabbisCrownItem", false, cl);
        assertNotNull(crownClass, "RabbisCrownItem must exist");
        assertEquals("net.minecraft.world.item.Item", crownClass.getSuperclass().getName());
    }

    @Test
    @DisplayName("Verify BlessedTraderEffect class exists and extends MobEffect")
    void testBlessedTraderEffectClassStructure() throws Exception {
        ClassLoader cl = RabbisCrownAndBlessedTraderTest.class.getClassLoader();
        Class<?> effectClass = Class.forName("com.israelsimulator.effect.BlessedTraderEffect", false, cl);
        assertNotNull(effectClass, "BlessedTraderEffect must exist");
        assertEquals("net.minecraft.world.effect.MobEffect", effectClass.getSuperclass().getName());
    }

    @Test
    @DisplayName("Verify Rabbi's Crown is worn as its 3D item model (no flat equipment asset)")
    void testRabbisCrownWornAs3DModel() {
        assertNull(getClass().getResourceAsStream("/assets/israel_simulator/equipment/rabbis_crown.json"),
                "an equipment asset would make HumanoidArmorLayer draw a flat texture instead of the 3D crown");
        assertNull(getClass().getResourceAsStream("/assets/israel_simulator/textures/entity/equipment/humanoid/rabbis_crown.png"),
                "the flat humanoid equipment texture must be gone");
        assertNotNull(getClass().getResourceAsStream("/assets/israel_simulator/textures/item/rabbis_crown.png"),
                "textures/item/rabbis_crown.png must exist");
    }

    @Test
    @DisplayName("Verify Rabbi's Crown item definition and 3D cuboid item model")
    void testRabbisCrownItemModelJson() throws Exception {
        try (InputStream def = getClass().getResourceAsStream("/assets/israel_simulator/items/rabbis_crown.json")) {
            assertNotNull(def, "items/rabbis_crown.json must exist in assets");
            assertTrue(new String(def.readAllBytes(), StandardCharsets.UTF_8).contains("israel_simulator:item/rabbis_crown"),
                    "item definition must point at the crown model");
        }
        try (InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/models/item/rabbis_crown.json")) {
            assertNotNull(stream, "models/item/rabbis_crown.json must exist in assets");
            com.google.gson.JsonObject model = com.google.gson.JsonParser.parseString(
                    new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            assertFalse(model.has("parent") && model.get("parent").getAsString().contains("generated"),
                    "the crown must no longer be a flat generated item");
            assertTrue(model.getAsJsonArray("elements").size() >= 30, "crown must be a detailed cuboid model");
            assertTrue(model.getAsJsonObject("display").has("head"), "crown must define the head display transform");
        }
    }

    @Test
    @DisplayName("Verify Blessed Trader eligibility logic with Crown and effects")
    void testBlessedTraderEligibility() {
        // Any of: Crown equipped, Blessed Trader effect, or Blessed effect qualifies
        assertTrue(IsraelVillagerTrades.isBlessedTraderEligible(true, false, false), "Equipped Crown must grant eligibility");
        assertTrue(IsraelVillagerTrades.isBlessedTraderEligible(false, true, false), "Blessed Trader effect must grant eligibility");
        assertTrue(IsraelVillagerTrades.isBlessedTraderEligible(false, false, true), "Blessed effect must grant eligibility");
        assertFalse(IsraelVillagerTrades.isBlessedTraderEligible(false, false, false), "Unequipped without effects must not be eligible");
    }

    @Test
    @DisplayName("Verify Blessed Trader price discount and non-negative lower bound")
    void testBlessedTraderPricingRules() {
        UUID playerId = UUID.randomUUID();
        int basePrice = 100;
        ReputationScores scores = new ReputationScores();

        int normalPrice = IsraelVillagerTrades.calculateAdjustedPrice(basePrice, scores, playerId, null, false);
        int blessedPrice = IsraelVillagerTrades.calculateAdjustedPrice(basePrice, scores, playerId, null, true);

        assertTrue(blessedPrice < normalPrice, "Blessed price must be discounted compared to normal price");
        assertEquals(85, blessedPrice, "15% discount on base 100 should yield 85");

        // Hard minimum validation: low base price cannot drop below 1
        int cheapPrice = IsraelVillagerTrades.calculateAdjustedPrice(1, scores, playerId, null, true);
        assertEquals(1, cheapPrice, "Minimum price must be capped at 1 Shekel/Emerald");
    }

    @Test
    @DisplayName("Verify trade anti-exploit safeguard against infinite money loops")
    void testTradeAntiExploitProtection() {
        IsraelVillagerTrades.SpecialTrade buy = new IsraelVillagerTrades.SpecialTrade("shekel", 10, "olives", 5, 10, false, false);
        IsraelVillagerTrades.SpecialTrade safeSell = new IsraelVillagerTrades.SpecialTrade("olives", 5, "shekel", 8, 10, false, false);
        IsraelVillagerTrades.SpecialTrade exploitSell = new IsraelVillagerTrades.SpecialTrade("olives", 5, "shekel", 15, 10, false, false);

        assertTrue(IsraelVillagerTrades.isTradeExploitSafe(buy, safeSell), "Safe trade margin must pass validation");
        assertFalse(IsraelVillagerTrades.isTradeExploitSafe(buy, exploitSell), "Exploit trade loop (selling for more than buy cost) must be blocked");
    }

    @Test
    @DisplayName("Verify localization keys for Rabbi's Crown and Blessed Trader")
    void testLocalization() {
        InputStream stream = getClass().getResourceAsStream("/assets/israel_simulator/lang/en_us.json");
        assertNotNull(stream, "en_us.json must exist");
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("\"item.israel_simulator.rabbis_crown\""), "Missing rabbis_crown key");
            assertTrue(json.contains("\"item.israel_simulator.rabbis_crown.desc\""), "Missing rabbis_crown.desc key");
            assertTrue(json.contains("\"item.israel_simulator.rabbis_crown.armor\""), "Missing rabbis_crown.armor key");
            assertTrue(json.contains("\"item.israel_simulator.rabbis_crown.effect\""), "Missing rabbis_crown.effect key");
            assertTrue(json.contains("\"effect.israel_simulator.blessed_trader\""), "Missing blessed_trader effect key");
        } catch (Exception e) {
            fail("Failed reading en_us.json: " + e.getMessage());
        }
    }
}
