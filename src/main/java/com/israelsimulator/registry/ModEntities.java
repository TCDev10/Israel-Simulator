package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.BibiBossEntity;
import com.israelsimulator.entity.boss.BibiGuardEntity;
import com.israelsimulator.entity.npc.MoneyChangerEntity;
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
    }
}
