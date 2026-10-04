package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.BibiBossEntity;
import com.israelsimulator.entity.boss.BibiGuardEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
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

    private ModEntities() {}

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(EntityAttributeCreationEvent.class, ModEntities::onEntityAttributeCreation);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(BIBI_BOSS.get(), BibiBossEntity.createAttributes().build());
        event.put(BIBI_GUARD.get(), BibiGuardEntity.createAttributes().build());
        event.put(BICYCLE.get(), com.israelsimulator.transport.BicycleEntity.createAttributes().build());
    }
}
