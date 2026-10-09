package com.israelsimulator.event;

import com.israelsimulator.agriculture.AgriculturalTrades;
import com.israelsimulator.npc.IsraelNpcData;
import com.israelsimulator.npc.IsraelNpcManager;
import com.israelsimulator.npc.NpcProfession;
import com.israelsimulator.religion.RuralSynagogueTrades;
import com.israelsimulator.world.biome.ModBiomes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Villagers must behave like normal vanilla villagers in every case: the mod may never
 * cancel, replace or spam a villager right-click. Currency exchange lives on the
 * separate Money Changer entity, whose trades are data-driven.
 */
class VillagerTradeHijackTest {

    private static final Gson GSON = new Gson();
    private static final Path DATA_PATH = Path.of("src", "main", "resources", "data", "israel_simulator");

    @Test
    @DisplayName("Agricultural trades apply only in israeli_agriculture, and tryTrade consults that gate first")
    void testAgriculturalTradeRequiresAgricultureBiome() throws Exception {
        Identifier plains = Identifier.fromNamespaceAndPath("minecraft", "plains");
        assertFalse(AgriculturalTrades.acceptsBiome(null));
        assertFalse(AgriculturalTrades.acceptsBiome(plains));
        assertFalse(AgriculturalTrades.acceptsBiome(ModBiomes.URBAN_AREA.identifier()));
        assertFalse(AgriculturalTrades.acceptsBiome(ModBiomes.JERUSALEM.identifier()));
        assertFalse(AgriculturalTrades.acceptsBiome(ModBiomes.DEAD_SEA.identifier()));
        assertFalse(AgriculturalTrades.acceptsBiome(ModBiomes.JUDEAN_DESERT.identifier()));
        assertFalse(AgriculturalTrades.acceptsBiome(ModBiomes.MEDITERRANEAN_COAST.identifier()));
        assertTrue(AgriculturalTrades.acceptsBiome(ModBiomes.ISRAELI_AGRICULTURE.identifier()));

        String method = methodSource(
                "src/main/java/com/israelsimulator/agriculture/AgriculturalTrades.java",
                "public static boolean tryTrade");
        int gate = method.indexOf("acceptsBiome(");
        int trade = method.indexOf("calculateTrade(");
        assertTrue(gate >= 0 && trade > gate, "a crop in hand must not trade before the biome gate");
    }

    @Test
    @DisplayName("Rural synagogue blessings apply only in the agricultural biome")
    void testRuralSynagogueRequiresAgricultureBiome() throws Exception {
        assertFalse(RuralSynagogueTrades.acceptsBiome(null));
        assertFalse(RuralSynagogueTrades.acceptsBiome(Identifier.fromNamespaceAndPath("minecraft", "plains")));
        assertFalse(RuralSynagogueTrades.acceptsBiome(ModBiomes.JERUSALEM.identifier()));
        assertTrue(RuralSynagogueTrades.acceptsBiome(ModBiomes.ISRAELI_AGRICULTURE.identifier()));

        String method = methodSource(
                "src/main/java/com/israelsimulator/religion/RuralSynagogueTrades.java",
                "public static boolean tryInteract");
        int gate = method.indexOf("acceptsBiome(");
        int offer = method.indexOf("PRAYER_NOTE");
        assertTrue(gate >= 0 && offer > gate, "a prayer note must not bless before the biome gate");
    }

    @Test
    @DisplayName("Villager right-clicks are never intercepted: no trades, no NPC claims, no cancel")
    void testVillagersNeverIntercepted() throws Exception {
        assertFalse(IsraelNpcManager.shouldHandleRegisteredNpc(null));
        IsraelNpcData registered = new IsraelNpcData(
                UUID.randomUUID(), NpcProfession.FARMER, BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO);
        assertTrue(IsraelNpcManager.shouldHandleRegisteredNpc(registered));

        String interact = methodSource(
                "src/main/java/com/israelsimulator/event/ModGameEvents.java",
                "public static void onEntityInteract");
        int next = interact.indexOf("public static void onRightClickBlock");
        assertTrue(next > 0);
        interact = interact.substring(0, next);

        assertFalse(interact.contains("AbstractVillager"),
                "villager interactions must never be claimed by the mod; the vanilla GUI must always open");
        assertFalse(interact.contains("npc.villager"), "no villager branch may remain");
        assertFalse(interact.contains("tryTrade"), "regional trades must not hijack villager right-clicks");
        assertFalse(interact.contains("tryInteract"), "rural synagogue blessings must not hijack villager right-clicks");
        assertFalse(interact.contains("handleNpcInteraction"), "NPC dialogue must not hijack villager right-clicks");
        assertFalse(interact.contains("triggerVillagerEasterEgg") || interact.contains("triggerTraderEasterEgg"),
                "chat easter eggs must not be attached to villager right-clicks");
        assertTrue(interact.contains("interactWithCat"), "the cat easter egg interaction must remain");

        // The gate class was the only thing that could cancel the vanilla GUI; it must be gone.
        assertFalse(Files.exists(Path.of("src/main/java/com/israelsimulator/event/VillagerInteractGate.java")),
                "VillagerInteractGate must not exist: nothing may cancel the vanilla villager GUI");
    }

    @Test
    @DisplayName("Money Changer owns currency exchange via data-driven trades")
    void testMoneyChangerTradesAreDataDriven() throws IOException {
        Path tradeSetPath = DATA_PATH.resolve(Path.of("trade_set", "money_changer.json"));
        assertTrue(Files.exists(tradeSetPath), "data/israel_simulator/trade_set/money_changer.json must exist");
        JsonObject tradeSet = GSON.fromJson(Files.readString(tradeSetPath), JsonObject.class);
        assertEquals("#israel_simulator:money_changer", tradeSet.get("trades").getAsString());
        assertTrue(tradeSet.get("amount").getAsDouble() >= 4, "a trader must offer several trades");

        Path tagPath = DATA_PATH.resolve(Path.of("tags", "villager_trade", "money_changer.json"));
        assertTrue(Files.exists(tagPath), "the money_changer trade tag must exist");
        JsonObject tag = GSON.fromJson(Files.readString(tagPath), JsonObject.class);
        List<String> values = new ArrayList<>();
        tag.getAsJsonArray("values").forEach(v -> values.add(v.getAsString()));
        assertTrue(values.size() >= 8, "at least 8 distinct money changer trades");

        Set<String> currencyInputs = Set.of(
                "israel_simulator:shekel", "israel_simulator:agora", "israel_simulator:ancient_coin");
        Set<String> forbiddenOutputs = Set.of(
                "israel_simulator:rabbis_crown", "israel_simulator:first_amendment",
                "israel_simulator:hava_nagila_disc");

        Map<String, JsonObject> trades = new HashMap<>();
        for (String value : values) {
            Path tradePath = DATA_PATH.resolve(Path.of(
                    "villager_trade", value.substring("israel_simulator:".length()) + ".json"));
            assertTrue(Files.exists(tradePath), "missing trade definition " + value);
            JsonObject trade = GSON.fromJson(Files.readString(tradePath), JsonObject.class);
            assertTrue(trade.has("wants"), "trade must define wants: " + value);
            assertTrue(trade.has("gives"), "trade must define gives: " + value);
            assertTrue(trade.get("max_uses").getAsDouble() >= 1, "trade must have uses: " + value);
            trades.put(value, trade);
        }

        for (Map.Entry<String, JsonObject> entry : trades.entrySet()) {
            String givesId = entry.getValue().getAsJsonObject("gives").get("id").getAsString();
            assertFalse(forbiddenOutputs.contains(givesId),
                    "rare endgame items must not be tradable: " + givesId + " in " + entry.getKey());
        }

        // Every trade either takes mod currency, or is the diamond buy-back (sells shekels).
        for (Map.Entry<String, JsonObject> entry : trades.entrySet()) {
            JsonObject wants = entry.getValue().getAsJsonObject("wants");
            String wantsId = wants.get("id").getAsString();
            assertTrue(currencyInputs.contains(wantsId) || "minecraft:diamond".equals(wantsId),
                    "money changers only deal in currency (and buy back diamonds): " + wantsId);
        }

        // Anti-exploit (AGENTS.md §16): buying a diamond must cost at least what selling one pays.
        double buyDiamond = shekelCost(trades, "minecraft:diamond");
        double sellDiamond = shekelPayout(trades, "minecraft:diamond");
        assertTrue(buyDiamond >= sellDiamond,
                "diamond buy price (" + buyDiamond + ") must be >= sell price (" + sellDiamond + ")");
    }

    private static double shekelCost(Map<String, JsonObject> trades, String outputId) {
        for (JsonObject trade : trades.values()) {
            JsonObject wants = trade.getAsJsonObject("wants");
            JsonObject gives = trade.getAsJsonObject("gives");
            if ("israel_simulator:shekel".equals(wants.get("id").getAsString())
                    && outputId.equals(gives.get("id").getAsString())) {
                return wants.has("count") ? wants.get("count").getAsDouble() : 1.0;
            }
        }
        return Double.MAX_VALUE;
    }

    private static double shekelPayout(Map<String, JsonObject> trades, String inputId) {
        for (JsonObject trade : trades.values()) {
            JsonObject wants = trade.getAsJsonObject("wants");
            JsonObject gives = trade.getAsJsonObject("gives");
            if (inputId.equals(wants.get("id").getAsString())
                    && "israel_simulator:shekel".equals(gives.get("id").getAsString())) {
                return gives.has("count") ? gives.get("count").getAsDouble() : 1.0;
            }
        }
        return 0.0;
    }

    private static String methodSource(String path, String signature) throws Exception {
        String source = Files.readString(Path.of(path));
        int start = source.indexOf(signature);
        assertTrue(start >= 0, "missing " + signature + " in " + path);
        return source.substring(start);
    }
}
