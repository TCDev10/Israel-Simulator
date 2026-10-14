package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.BibiBossEntity;
import com.israelsimulator.entity.boss.BibiGuardEntity;
import com.israelsimulator.entity.npc.MoneyChangerEntity;
import com.israelsimulator.entity.wildlife.CapeHyraxEntity;
import com.israelsimulator.entity.wildlife.MountainGazelleEntity;
import com.israelsimulator.entity.wildlife.NubianIbexEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity type registry for Israel-Simulator entities (GAME_DESIGN.md §41–43, TODO §43–44).
 */
public final class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(IsraelSimulator.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BibiBossEntity>> BIBI_BOSS =
            ENTITY_TYPES.registerEntityType("bibi_boss", BibiBossEntity::new, MobCategory.MONSTER,
                    b -> b.sized(0.9F, 2.2F).fireImmune().clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<BibiGuardEntity>> BIBI_GUARD =
            ENTITY_TYPES.registerEntityType("bibi_guard", BibiGuardEntity::new, MobCategory.MONSTER,
                    b -> b.sized(0.6F, 1.95F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.transport.BicycleEntity>> BICYCLE =
            ENTITY_TYPES.registerEntityType("bicycle", com.israelsimulator.transport.BicycleEntity::new, MobCategory.MISC,
                    b -> b.sized(0.8F, 1.0F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.boss.BibiMissileEntity>> BIBI_MISSILE =
            ENTITY_TYPES.registerEntityType("bibi_missile", com.israelsimulator.entity.boss.BibiMissileEntity::new, MobCategory.MISC,
                    b -> b.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(2));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.boss.TrumpMinibossEntity>> TRUMP_MINIBOSS =
            ENTITY_TYPES.registerEntityType("trump_miniboss", com.israelsimulator.entity.boss.TrumpMinibossEntity::new, MobCategory.MONSTER,
                    b -> b.sized(0.9F, 2.1F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.boss.IceAgentEntity>> ICE_AGENT =
            ENTITY_TYPES.registerEntityType("ice_agent", com.israelsimulator.entity.boss.IceAgentEntity::new, MobCategory.MONSTER,
                    b -> b.sized(0.6F, 1.95F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.boss.JeffreyEpsteinEntity>> JEFFREY_EPSTEIN =
            ENTITY_TYPES.registerEntityType("jeffrey_epstein", com.israelsimulator.entity.boss.JeffreyEpsteinEntity::new, MobCategory.MONSTER,
                    b -> b.sized(0.9F, 2.0F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.boss.ChildZombieMinionEntity>> CHILD_ZOMBIE_MINION =
            ENTITY_TYPES.registerEntityType("child_zombie_minion", com.israelsimulator.entity.boss.ChildZombieMinionEntity::new, MobCategory.MONSTER,
                    b -> b.sized(0.35F, 0.95F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<MoneyChangerEntity>> MONEY_CHANGER =
            ENTITY_TYPES.registerEntityType(MoneyChangerEntity.TRADE_SET_ID, MoneyChangerEntity::new, MobCategory.CREATURE,
                    b -> b.sized(0.6F, 1.95F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.npc.GoyimEntity>> GOYIM =
            ENTITY_TYPES.registerEntityType("goyim", com.israelsimulator.entity.npc.GoyimEntity::new, MobCategory.CREATURE,
                    b -> b.sized(0.6F, 1.95F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.npc.OratorEntity>> ORATOR =
            ENTITY_TYPES.registerEntityType("orator", com.israelsimulator.entity.npc.OratorEntity::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.8F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.projectile.FragGrenadeEntity>> FRAG_GRENADE =
            ENTITY_TYPES.registerEntityType("frag_grenade", com.israelsimulator.entity.projectile.FragGrenadeEntity::new, MobCategory.MISC,
                    b -> b.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.projectile.PalantrioDroneEntity>> PALANTRIO_DRONE =
            ENTITY_TYPES.registerEntityType("palantrio_drone", com.israelsimulator.entity.projectile.PalantrioDroneEntity::new, MobCategory.MISC,
                    b -> b.sized(0.7F, 0.35F).clientTrackingRange(8).updateInterval(2));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.projectile.BibiPoisonBreathEntity>> BIBI_POISON_BREATH =
            ENTITY_TYPES.registerEntityType("bibi_poison_breath", com.israelsimulator.entity.projectile.BibiPoisonBreathEntity::new, MobCategory.MISC,
                    b -> b.sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.mossad.MossadAgentEntity>> MOSSAD_AGENT =
            ENTITY_TYPES.registerEntityType("mossad_agent", com.israelsimulator.entity.mossad.MossadAgentEntity::new, MobCategory.MONSTER,
                    b -> b.sized(0.6F, 1.95F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.mossad.MossadHandlerEntity>> MOSSAD_HANDLER =
            ENTITY_TYPES.registerEntityType("mossad_handler", com.israelsimulator.entity.mossad.MossadHandlerEntity::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.mossad.MossadInformantEntity>> MOSSAD_INFORMANT =
            ENTITY_TYPES.registerEntityType("mossad_informant", com.israelsimulator.entity.mossad.MossadInformantEntity::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<MountainGazelleEntity>> MOUNTAIN_GAZELLE =
            ENTITY_TYPES.registerEntityType("mountain_gazelle", MountainGazelleEntity::new, MobCategory.CREATURE,
                    b -> b.sized(0.8F, 1.25F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<NubianIbexEntity>> NUBIAN_IBEX =
            ENTITY_TYPES.registerEntityType("nubian_ibex", NubianIbexEntity::new, MobCategory.CREATURE,
                    b -> b.sized(0.95F, 1.35F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<CapeHyraxEntity>> CAPE_HYRAX =
            ENTITY_TYPES.registerEntityType("cape_hyrax", CapeHyraxEntity::new, MobCategory.CREATURE,
                    b -> b.sized(0.55F, 0.55F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<com.israelsimulator.entity.event.PlaneCrashEntity>> PLANE_CRASH =
            ENTITY_TYPES.registerEntityType("plane_crash", com.israelsimulator.entity.event.PlaneCrashEntity::new, MobCategory.MISC,
                    b -> b.sized(8.0F, 4.0F).clientTrackingRange(16).updateInterval(1));

    private ModEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(EntityAttributeCreationEvent.class, ModEntities::onEntityAttributeCreation);
        modEventBus.addListener(RegisterSpawnPlacementsEvent.class, ModEntities::onRegisterSpawnPlacements);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(BIBI_BOSS.get(), BibiBossEntity.createAttributes().build());
        event.put(BIBI_GUARD.get(), BibiGuardEntity.createAttributes().build());
        event.put(BICYCLE.get(), com.israelsimulator.transport.BicycleEntity.createAttributes().build());
        event.put(TRUMP_MINIBOSS.get(), com.israelsimulator.entity.boss.TrumpMinibossEntity.createAttributes().build());
        event.put(ICE_AGENT.get(), com.israelsimulator.entity.boss.IceAgentEntity.createAttributes().build());
        event.put(JEFFREY_EPSTEIN.get(), com.israelsimulator.entity.boss.JeffreyEpsteinEntity.createAttributes().build());
        event.put(CHILD_ZOMBIE_MINION.get(), com.israelsimulator.entity.boss.ChildZombieMinionEntity.createAttributes().build());
        event.put(MONEY_CHANGER.get(), MoneyChangerEntity.createAttributes().build());
        event.put(GOYIM.get(), com.israelsimulator.entity.npc.GoyimEntity.createAttributes().build());
        event.put(PALANTRIO_DRONE.get(), com.israelsimulator.entity.projectile.PalantrioDroneEntity.createAttributes().build());
        event.put(MOSSAD_AGENT.get(), com.israelsimulator.entity.mossad.MossadAgentEntity.createAttributes().build());
        event.put(MOSSAD_HANDLER.get(), com.israelsimulator.entity.mossad.MossadHandlerEntity.createAttributes().build());
        event.put(MOSSAD_INFORMANT.get(), com.israelsimulator.entity.mossad.MossadInformantEntity.createAttributes().build());
        event.put(ORATOR.get(), com.israelsimulator.entity.npc.OratorEntity.createAttributes().build());
        event.put(MOUNTAIN_GAZELLE.get(), MountainGazelleEntity.createAttributes().build());
        event.put(NUBIAN_IBEX.get(), NubianIbexEntity.createAttributes().build());
        event.put(CAPE_HYRAX.get(), CapeHyraxEntity.createAttributes().build());
    }

    /**
     * Money Changers spawn naturally in the city biomes added by the
     * {@code israel_simulator:add_money_changer_spawns} biome modifier; the egg and
     * commands work without this placement, but natural spawns need it.
     */
    private static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(MONEY_CHANGER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnReason, pos, random) ->
                        level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(MOUNTAIN_GAZELLE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> isRegionalWildlifeSpawnSurface(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NUBIAN_IBEX.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> isRegionalWildlifeSpawnSurface(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CAPE_HYRAX.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> isRegionalWildlifeSpawnSurface(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean isRegionalWildlifeSpawnSurface(net.minecraft.world.level.ServerLevelAccessor level,
                                                           net.minecraft.core.BlockPos pos) {
        var ground = level.getBlockState(pos.below());
        return ground.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || ground.is(BlockTags.SAND)
                || ground.is(BlockTags.BASE_STONE_OVERWORLD)
                || ground.is(ModBlocks.SALT_BLOCK.get())
                || ground.is(ModBlocks.JERUSALEM_STONE.get());
    }
}
