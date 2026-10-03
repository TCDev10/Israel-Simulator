package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound-event registry. Entries are added when audio content is implemented.
 */
public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, IsraelSimulator.MOD_ID);

    /**
     * Sound event for the Hava Nagila music disc (GAME_DESIGN §44).
     * Placeholder without audio file: a compatible-licence recording must be
     * supplied before the disc can actually play (see ASSET_LICENSES.md).
     */
    public static final net.neoforged.neoforge.registries.DeferredHolder<SoundEvent, SoundEvent> HAVA_NAGILA =
            SOUND_EVENTS.register("music_disc.hava_nagila",
                    () -> SoundEvent.createVariableRangeEvent(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                    com.israelsimulator.IsraelSimulator.MOD_ID, "music_disc.hava_nagila")));

    private ModSoundEvents() {}

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
