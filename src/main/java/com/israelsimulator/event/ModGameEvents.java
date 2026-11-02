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
        com.israelsimulator.event.world.plane.PlaneCrashCommands.register(event.getDispatcher());
        com.israelsimulator.mossad.MossadCommands.register(event.getDispatcher());
        com.israelsimulator.festival.FestivalCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            com.israelsimulator.festival.FestivalEvents.onJoin(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        com.israelsimulator.event.world.speech.PublicSpeechEvent.reset();
        com.israelsimulator.deadsea.DeadSeaSpa.reset();
        com.israelsimulator.festival.FestivalEvents.reset();
        com.israelsimulator.festival.ShabbatRest.reset();
        com.israelsimulator.festival.FestivalManager.clearForcedFestival();
    }

    /** Crowd villagers left over from a previous session (event state is not saved) leave on load. */
    @SubscribeEvent
    public static void onEntityJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && event.getEntity().entityTags().contains(com.israelsimulator.event.world.speech.PublicSpeechEvent.CROWD_TAG)
                && !com.israelsimulator.event.world.speech.PublicSpeechEvent.isCrowdMember(event.getEntity().getUUID())) {
            event.setCanceled(true);
            return;
        }
        // Replace vanilla villagers with Goyim NPCs to keep trades/professions but with custom name/skin.
        if (!event.getLevel().isClientSide()
                && event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && event.getEntity() instanceof net.minecraft.world.entity.npc.villager.Villager villager
                && !(villager instanceof com.israelsimulator.entity.npc.GoyimEntity)
                && !villager.entityTags().contains(com.israelsimulator.event.world.speech.PublicSpeechEvent.CROWD_TAG)) {
            event.setCanceled(true);
            serverLevel.getServer().execute(() -> {
                com.israelsimulator.entity.npc.GoyimEntity goyim = com.israelsimulator.registry.ModEntities.GOYIM.get()
                        .create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.EVENT);
                if (goyim != null) {
                    goyim.copyPosition(villager);
                    goyim.setYRot(villager.getYRot());
                    goyim.setXRot(villager.getXRot());
                    goyim.setHealth(villager.getHealth());
                    goyim.setInvulnerable(villager.isInvulnerable());
                    goyim.setVillagerData(villager.getVillagerData());
                    goyim.setVillagerXp(villager.getVillagerXp());
                    goyim.setCustomName(net.minecraft.network.chat.Component.literal("Goy"));
                    goyim.setCustomNameVisible(true);
                    serverLevel.addFreshEntity(goyim);
                }
            });
        }
    }

    /** Restores Epstein arenas whose boss vanished (died unloaded, other dimension, deleted). */
    @SubscribeEvent
    public static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel speechLevel) {
            com.israelsimulator.event.world.speech.PublicSpeechEvent.tick(speechLevel);
            com.israelsimulator.event.world.plane.TwinTowersPlaneEvent.tick(speechLevel);
            com.israelsimulator.mossad.MossadAgentSpawner.tick(speechLevel);
            com.israelsimulator.festival.FestivalEvents.tick(speechLevel);
            if (speechLevel.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                com.israelsimulator.festival.ShabbatRest.tick(speechLevel);
            }
            if (speechLevel.getGameTime() % 20 == 0) {
                for (net.minecraft.server.level.ServerPlayer p : speechLevel.players()) {
                    com.israelsimulator.mossad.MossadMissions.tickPlayer(p);
                }
            }
        }
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                && level.getGameTime() % com.israelsimulator.entity.boss.ArenaRestoreRules.ORPHAN_CHECK_INTERVAL == 0) {
            com.israelsimulator.entity.boss.EpsteinArenaSnapshots.tickOrphans(level);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity entity) {
            // Dead Sea buoyancy (also on the client: players move themselves)
            com.israelsimulator.deadsea.DeadSeaMechanics.applyBuoyancy(entity);
            if (entity.level().isClientSide()) {
                return;
            }

            if (entity instanceof net.minecraft.world.entity.npc.villager.AbstractVillager villager) {
                com.israelsimulator.festival.ShabbatRest.tickVillager(villager);
            }

            // Desert sun and heat exhaustion (player-specific)
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                com.israelsimulator.desert.DesertHazards.handleDesertTick(player);
            }

            // Landmark discovery proximity check (§48)
            if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                com.israelsimulator.westernwall.WesternWallManager.tickPlayer(serverPlayer);
                if (serverPlayer.tickCount % 20 == 0) {
                    com.israelsimulator.deadsea.DeadSeaSpa.tickPlayer(serverPlayer);
                    com.israelsimulator.advancement.IsraelAdvancements.tickPlayer(serverPlayer);
                    com.israelsimulator.world.map.PlayerLandmarkTracker.checkProximityAndDiscover(serverPlayer);
                    com.israelsimulator.city.telaviv.TelAvivDistrictManager.checkPlayerDistrict(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        com.israelsimulator.festival.ShabbatRest.onWork(event.getPlayer());
    }

    @SubscribeEvent
    public static void onBlockPlace(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        com.israelsimulator.festival.ShabbatRest.onWork(event.getEntity());
    }

    @SubscribeEvent
    public static void onAttack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        com.israelsimulator.festival.ShabbatRest.onWork(event.getEntity());
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

        if (com.israelsimulator.deadsea.DeadSeaSpa.tryScoop(event.getEntity(), event.getHand(), event.getPos())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            return;
        }

        if (com.israelsimulator.synagogue.SynagogueManager.tryArkPray(event.getEntity(), event.getHand(), event.getPos())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    /** Kills of hostile Mossad agents count for elimination missions. */
    @SubscribeEvent
    public static void onMossadAgentDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof com.israelsimulator.entity.mossad.MossadAgentEntity agent
                && event.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            com.israelsimulator.mossad.MossadMissions.onAgentKilled(player, agent);
        }
    }
}
