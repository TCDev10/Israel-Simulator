package com.israelsimulator.npc;

import com.israelsimulator.economy.CityRegion;
import com.israelsimulator.economy.IsraelEconomy;
import com.israelsimulator.npc.schedule.NpcSchedule;
import com.israelsimulator.registry.ModItems;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * NPC framework manager coordinating professions, dialogue, trading, and routines (GAME_DESIGN.md §26, §27, §29).
 */
public final class IsraelNpcManager {

    private static final Map<UUID, IsraelNpcData> NPC_DATA_MAP = new ConcurrentHashMap<>();

    private IsraelNpcManager() {}

    public static IsraelNpcData getOrAssignNpcData(AbstractVillager villager) {
        return NPC_DATA_MAP.computeIfAbsent(villager.getUUID(), id -> {
            NpcProfession profession = determineProfessionForLocation(villager.level(), villager.blockPosition());
            BlockPos pos = villager.blockPosition();
            return new IsraelNpcData(id, profession, pos, pos.offset(2, 0, 2), pos.offset(-2, 0, -2));
        });
    }

    public static IsraelNpcData getNpcData(UUID uuid) {
        return NPC_DATA_MAP.get(uuid);
    }

    public static void registerNpcData(IsraelNpcData data) {
        NPC_DATA_MAP.put(data.getNpcId(), data);
    }

    public static void clearNpcData(UUID uuid) {
        NPC_DATA_MAP.remove(uuid);
    }

    /**
     * Determines a contextual profession for an NPC based on biome and structure location.
     */
    public static NpcProfession determineProfessionForLocation(Level level, BlockPos pos) {
        var biome = level.getBiome(pos);
        Identifier biomeId = biome.unwrapKey().map(net.minecraft.resources.ResourceKey::identifier).orElse(null);

        if (biomeId != null) {
            String path = biomeId.getPath();
            if (path.contains("jerusalem")) {
                NpcProfession[] options = { NpcProfession.RABBI, NpcProfession.HISTORIAN, NpcProfession.MERCHANT, NpcProfession.SHOPKEEPER, NpcProfession.TOURIST };
                return options[Math.abs(pos.hashCode()) % options.length];
            } else if (path.contains("urban")) {
                NpcProfession[] options = { NpcProfession.DEVELOPER, NpcProfession.FOUNDER, NpcProfession.INVESTOR, NpcProfession.CHEF, NpcProfession.TAXI_DRIVER, NpcProfession.ENGINEER, NpcProfession.MUSICIAN };
                return options[Math.abs(pos.hashCode()) % options.length];
            } else if (path.contains("agriculture")) {
                return NpcProfession.FARMER;
            } else if (path.contains("coast")) {
                NpcProfession[] options = { NpcProfession.FISHERMAN, NpcProfession.ARTISAN, NpcProfession.CHEF };
                return options[Math.abs(pos.hashCode()) % options.length];
            }
        }

        // Default rotation across all 15 professions
        NpcProfession[] all = NpcProfession.values();
        return all[Math.abs(pos.hashCode()) % all.length];
    }

    /**
     * Vanilla villagers are not mod NPCs. Only an entry already stored via
     * {@link #registerNpcData} may replace the villager GUI.
     */
    public static boolean shouldHandleRegisteredNpc(IsraelNpcData existing) {
        return existing != null;
    }

    /**
     * Handles player interaction with an NPC: provides dialogue and executes Shekel trading.
     */
    public static boolean handleNpcInteraction(Player player, InteractionHand hand, AbstractVillager villager) {
        if (player.level().isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return false;
        }

        Level level = player.level();
        IsraelNpcData npcData = getNpcData(villager.getUUID());
        if (!shouldHandleRegisteredNpc(npcData)) {
            return false;
        }
        NpcProfession profession = npcData.getProfession();
        boolean isShabbat = NpcSchedule.isShabbat(level.getGameTime());
        long dayTime = level.getOverworldClockTime() % 24000L;

        // Check if trading is allowed by schedule
        boolean canTrade = NpcSchedule.canNpcTrade(dayTime, isShabbat, profession);
        com.israelsimulator.festival.FestivalType activeFestival = com.israelsimulator.festival.FestivalManager.getCurrentFestival(level.getGameTime());

        // 1. If it's Shabbat or NPC cannot trade, display dialogue only
        if (isShabbat && profession.isObservantShabbat()) {
            player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] " + profession.getShabbatGreeting()));
            level.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                    SoundEvents.VILLAGER_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
            return true;
        }

        if (activeFestival != null && !activeFestival.equals(com.israelsimulator.festival.FestivalType.SHABBAT)) {
            // Holiday greeting
            player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] " + activeFestival.getGreeting()));
        }

        if (!canTrade) {
            player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] Currently resting or busy with daily routines. Check back later!"));
            level.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                    SoundEvents.VILLAGER_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
            return true;
        }

        // 2. Try trade execution if player is holding Shekel or tradeable goods
        ItemStack held = player.getItemInHand(hand);
        CityRegion region = profession.getPreferredRegion();
        boolean isBlessed = com.israelsimulator.trading.IsraelVillagerTrades.isBlessedTraderEligible(player);

        // Map profession to faction for global reputation tracking
        com.israelsimulator.reputation.ReputationFaction faction = switch (profession.getPrimaryCategory()) {
            case JUDAICA_CULTURAL -> com.israelsimulator.reputation.ReputationFaction.RELIGIOUS;
            case TECHNOLOGY -> com.israelsimulator.reputation.ReputationFaction.TECH_DISTRICT;
            case AGRICULTURE -> com.israelsimulator.reputation.ReputationFaction.VILLAGE;
            case FOOD -> com.israelsimulator.reputation.ReputationFaction.CITY;
            case COMMODITIES_MINERALS -> com.israelsimulator.reputation.ReputationFaction.MERCHANT;
        };

        int playerRep = Math.max(npcData.getReputation(),
                com.israelsimulator.reputation.ReputationManager.getReputation(player.getUUID(), faction));

        // Player wants to BUY a primary item from this NPC using Shekels
        if (held.is(ModItems.SHEKEL.get()) && !profession.getTradedItems().isEmpty()) {
            if (!npcData.canTradeToday()) {
                player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] I have concluded my business transactions for today. Return tomorrow!"));
                return true;
            }

            if (isBlessed) {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.blessed_trader_greeting"));
            }

            // Pick an item sold by this profession
            String soldItemId = profession.getTradedItems().getFirst();
            long buyPriceAgorot = IsraelEconomy.calculateBuyPrice(soldItemId, region, playerRep);
            if (isBlessed) {
                buyPriceAgorot = (long) (buyPriceAgorot * 0.85); // 15% holy discount
            }
            long costInShekels = Math.max(1L, (buyPriceAgorot + 99L) / 100L); // Ceiling to Shekels

            if (held.getCount() >= costInShekels) {
                Item itemToGive = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("israel_simulator", soldItemId));
                if (itemToGive != null && itemToGive != net.minecraft.world.item.Items.AIR) {
                    held.shrink((int) costInShekels);
                    player.addItem(new ItemStack(itemToGive, 1));
                    npcData.recordTrade(level.getGameTime());
                    npcData.adjustReputation(2);
                    com.israelsimulator.reputation.ReputationManager.adjustReputation(player.getUUID(), faction, 2);

                    player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] Pleasure doing business! "
                            + costInShekels + " Shekels for " + soldItemId + "."));
                    level.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                            SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    return true;
                }
            } else {
                player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] That item costs "
                        + costInShekels + " Shekels. You are holding " + held.getCount() + "."));
                return true;
            }
        }

        // Player wants to SELL an item to this NPC for Shekels
        if (!held.isEmpty() && !held.is(ModItems.SHEKEL.get()) && !held.is(ModItems.AGORA.get())) {
            Identifier heldId = BuiltInRegistries.ITEM.getKey(held.getItem());
            if (profession.getTradedItems().contains(heldId.getPath())) {
                if (!npcData.canTradeToday()) {
                    player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] I cannot purchase more inventory today. Come back tomorrow!"));
                    return true;
                }

                long sellPriceAgorot = IsraelEconomy.calculateSellPrice(heldId.getPath(), region, npcData.getReputation());
                int payoutShekels = (int) Math.max(1L, sellPriceAgorot / 100L);

                held.shrink(1);
                player.addItem(new ItemStack(ModItems.SHEKEL.get(), payoutShekels));
                npcData.recordTrade(level.getGameTime());
                npcData.adjustReputation(1);

                player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] I'll take that! Here are "
                        + payoutShekels + " Shekels."));
                level.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                        SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                return true;
            }
        }

        // Default greeting dialogue
        String greeting = npcData.getReputation() > 20
                ? "Shalom, my dear trusted friend! Always good to see you."
                : profession.getGreetings().get(Math.abs(player.tickCount) % profession.getGreetings().size());

        player.sendSystemMessage(Component.literal("[" + profession.getDisplayName() + "] " + greeting));
        level.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                SoundEvents.VILLAGER_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
        return true;
    }
}
