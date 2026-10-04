package com.israelsimulator.registry;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound-event registry for Israel-Simulator audio, music discs, ambience, transport and exploration (TODO §46–48).
 */
public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, IsraelSimulator.MOD_ID);

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, name)));
    }

    // Music & Cultural Discs (§46)
    public static final DeferredHolder<SoundEvent, SoundEvent> HAVA_NAGILA = registerSound("music_disc.hava_nagila");
    public static final DeferredHolder<SoundEvent, SoundEvent> KLEZMER = registerSound("music.cultural.klezmer");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHABBAT_SHALOM = registerSound("music.cultural.shabbat_shalom");

    // City Ambience (§46)
    public static final DeferredHolder<SoundEvent, SoundEvent> TEL_AVIV_AMBIENT = registerSound("ambient.city.tel_aviv");
    public static final DeferredHolder<SoundEvent, SoundEvent> JERUSALEM_AMBIENT = registerSound("ambient.city.jerusalem");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAFFA_AMBIENT = registerSound("ambient.city.jaffa");

    // Event & Festival Audio (§46)
    public static final DeferredHolder<SoundEvent, SoundEvent> SPEECH_CROWD = registerSound("ambient.event.speech_crowd");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARKET_BUSTLE = registerSound("ambient.event.market_bustle");
    public static final DeferredHolder<SoundEvent, SoundEvent> HANUKKAH_CHIME = registerSound("audio.festival.hanukkah_chime");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHABBAT_CANDLE = registerSound("audio.festival.shabbat_candle");

    // Boss Audio (§44-46)
    public static final DeferredHolder<SoundEvent, SoundEvent> BIBI_AMBIENT = registerSound("entity.bibi_boss.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIBI_HURT = registerSound("entity.bibi_boss.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIBI_DEATH = registerSound("entity.bibi_boss.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIBI_SPEECH = registerSound("entity.bibi_boss.speech");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIBI_ENRAGE = registerSound("entity.bibi_boss.enrage");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIBI_THEME = registerSound("music.boss.bibi_theme");

    // Transportation Audio (§47)
    public static final DeferredHolder<SoundEvent, SoundEvent> BICYCLE_BELL = registerSound("entity.bicycle.bell");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUS_HORN = registerSound("entity.bus.horn");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAIN_WHISTLE = registerSound("entity.train.whistle");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRANSIT_TRAVEL = registerSound("entity.transport.travel");

    // Exploration & Discovery Audio (§48)
    public static final DeferredHolder<SoundEvent, SoundEvent> LANDMARK_DISCOVERED = registerSound("ui.landmark_discovered");

    private ModSoundEvents() {}

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
