package com.israelsimulator.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code israel_simulator:mod_event}: one criterion trigger for every mod action that has no vanilla
 * trigger (prayers, Dead Sea floating, startup work, train rides, festivals, ...). The advancement
 * names the action in {@code conditions.event}; the code fires it through {@link IsraelAdvancements}.
 * The valid names are the constants of {@link AdvancementEvents}.
 */
public class IsraelEventTrigger extends SimpleCriterionTrigger<IsraelEventTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String event) {
        this.trigger(player, instance -> instance.event().equals(event));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, String event)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Codec.STRING.fieldOf("event").forGetter(TriggerInstance::event))
                .apply(i, TriggerInstance::new));
    }
}
