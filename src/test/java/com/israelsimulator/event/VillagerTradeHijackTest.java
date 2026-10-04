package com.israelsimulator.event;

import com.israelsimulator.agriculture.AgriculturalTrades;
import com.israelsimulator.npc.IsraelNpcData;
import com.israelsimulator.npc.IsraelNpcManager;
import com.israelsimulator.npc.NpcProfession;
import com.israelsimulator.religion.RuralSynagogueTrades;
import com.israelsimulator.world.biome.ModBiomes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VillagerTradeHijackTest {

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
    @DisplayName("An unregistered villager is not claimed, so the cancel-all path does not return")
    void testUnregisteredVillagerDoesNotCancel() throws Exception {
        assertFalse(IsraelNpcManager.shouldHandleRegisteredNpc(null));
        IsraelNpcData registered = new IsraelNpcData(
                UUID.randomUUID(), NpcProfession.FARMER, BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO);
        assertTrue(IsraelNpcManager.shouldHandleRegisteredNpc(registered));

        String method = methodSource(
                "src/main/java/com/israelsimulator/npc/IsraelNpcManager.java",
                "public static boolean handleNpcInteraction");
        assertTrue(method.contains("shouldHandleRegisteredNpc"));
        assertFalse(method.contains("getOrAssignNpcData"),
                "assigning a profession to every villager cancels the vanilla GUI");

        assertFalse(VillagerInteractGate.shouldCancelVanillaGui(false, false));
        assertTrue(VillagerInteractGate.shouldCancelVanillaGui(true, false));
        assertTrue(VillagerInteractGate.shouldCancelVanillaGui(false, true));

        String interact = methodSource(
                "src/main/java/com/israelsimulator/event/ModGameEvents.java",
                "public static void onEntityInteract");
        int next = interact.indexOf("public static void onRightClickBlock");
        assertTrue(next > 0);
        interact = interact.substring(0, next);
        int villager = interact.indexOf("AbstractVillager");
        assertTrue(villager >= 0);
        String villagerBranch = interact.substring(villager);
        assertTrue(villagerBranch.contains("AgriculturalTrades.tryTrade"));
        assertTrue(villagerBranch.contains("DeadSeaTrades.tryTrade"));
        assertTrue(villagerBranch.contains("DesertTrades.tryTrade"));
        assertTrue(villagerBranch.contains("TelAvivTrades.tryTrade"));
        assertTrue(villagerBranch.contains("JaffaTrades.tryTrade"));
        assertTrue(villagerBranch.contains("JerusalemTrades.tryTrade"));
        assertTrue(villagerBranch.contains("RuralSynagogueTrades.tryInteract"));
        assertTrue(villagerBranch.contains("handleNpcInteraction"));
        int gate = villagerBranch.indexOf("shouldCancelVanillaGui");
        int cancel = villagerBranch.indexOf("setCanceled");
        assertTrue(gate >= 0 && cancel > gate);
        assertEquals(cancel, villagerBranch.lastIndexOf("setCanceled"));
    }

    private static String methodSource(String path, String signature) throws Exception {
        String source = Files.readString(Path.of(path));
        int start = source.indexOf(signature);
        assertTrue(start >= 0, "missing " + signature + " in " + path);
        return source.substring(start);
    }
}
