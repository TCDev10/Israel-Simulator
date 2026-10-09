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
    public static void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        com.israelsimulator.event.world.speech.SpeechCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        com.israelsimulator.event.world.speech.PublicSpeechEvent.reset();
    }

    /** Crowd villagers left over from a previous session (event state is not saved) leave on load. */
    @SubscribeEvent
    public static void onEntityJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && event.getEntity().entityTags().contains(com.israelsimulator.event.world.speech.PublicSpeechEvent.CROWD_TAG)
                && !com.israelsimulator.event.world.speech.PublicSpeechEvent.isCrowdMember(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    /** Restores Epstein arenas whose boss vanished (died unloaded, other dimension, deleted). */
    @SubscribeEvent
    public static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel speechLevel) {
            com.israelsimulator.event.world.speech.PublicSpeechEvent.tick(speechLevel);
        }
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                && level.getGameTime() % com.israelsimulator.entity.boss.ArenaRestoreRules.ORPHAN_CHECK_INTERVAL == 0) {
            com.israelsimulator.entity.boss.EpsteinArenaSnapshots.tickOrphans(level);
        }
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
                com.israelsimulator.westernwall.WesternWallManager.tickPlayer(serverPlayer);
                if (serverPlayer.tickCount % 20 == 0) {
                    com.israelsimulator.world.map.PlayerLandmarkTracker.checkProximityAndDiscover(serverPlayer);
                    com.israelsimulator.city.telaviv.TelAvivDistrictManager.checkPlayerDistrict(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            if (event.getTarget() instanceof net.minecraft.world.entity.animal.feline.Cat cat) {
                if (com.israelsimulator.easteregg.EasterEggManager.interactWithCat(serverPlayer, cat, event.getItemStack())) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        // Villagers are never intercepted: the vanilla trading GUI must always open.
        // Currency exchange lives on the Money Changer entity instead.
    }

    @SubscribeEvent
    public static void onRightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getPos()).is(com.israelsimulator.registry.ModBlocks.WESTERN_WALL_STONE.get())) {
            // tryPray starts a multi-tick prayer session (or immediate failure). SUCCESS cancels vanilla use.
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
