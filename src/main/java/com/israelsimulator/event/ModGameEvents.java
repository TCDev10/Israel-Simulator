package com.israelsimulator.event;

import com.israelsimulator.IsraelSimulator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Game-bus listeners for server lifecycle and later gameplay events.
 * This class does not reference client-only types.
 */
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID)
public final class ModGameEvents {
    private ModGameEvents() {}

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        IsraelSimulator.LOGGER.info("Israel-Simulator server starting");
    }

    @SubscribeEvent
    public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity entity) {
            if (entity.level().isClientSide()) {
                return;
            }

            // Dead Sea buoyancy
            com.israelsimulator.deadsea.DeadSeaMechanics.applyBuoyancy(entity);

            // Desert sun and heat exhaustion (player-specific)
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                com.israelsimulator.desert.DesertHazards.handleDesertTick(player);
            }

            // Landmark discovery proximity check (§48)
            if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (serverPlayer.tickCount % 20 == 0) {
                    com.israelsimulator.world.map.PlayerLandmarkTracker.checkProximityAndDiscover(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof net.minecraft.world.entity.npc.villager.AbstractVillager villager) {
            net.minecraft.world.entity.player.Player player = event.getEntity();
            net.minecraft.world.InteractionHand hand = event.getHand();

            // Try regional trades in priority order
            if (com.israelsimulator.agriculture.AgriculturalTrades.tryTrade(player, hand, villager)
                    || com.israelsimulator.deadsea.DeadSeaTrades.tryTrade(player, hand, villager)
                    || com.israelsimulator.desert.DesertTrades.tryTrade(player, hand, villager)
                    || com.israelsimulator.city.telaviv.TelAvivTrades.tryTrade(player, hand, villager)
                    || com.israelsimulator.city.jaffa.JaffaTrades.tryTrade(player, hand, villager)
                    || com.israelsimulator.city.jerusalem.JerusalemTrades.tryTrade(player, hand, villager)
                    || com.israelsimulator.religion.RuralSynagogueTrades.tryInteract(player, hand, villager)
                    || com.israelsimulator.npc.IsraelNpcManager.handleNpcInteraction(player, hand, villager)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getPos()).is(com.israelsimulator.registry.ModBlocks.WESTERN_WALL_STONE.get())) {
            if (com.israelsimulator.westernwall.WesternWallManager.tryPray(event.getEntity(), event.getHand(), event.getPos())) {
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                return;
            }
        }

        if (com.israelsimulator.synagogue.SynagogueManager.tryArkPray(event.getEntity(), event.getHand(), event.getPos())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }
}
