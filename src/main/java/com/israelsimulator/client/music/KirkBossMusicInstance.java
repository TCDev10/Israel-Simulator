package com.israelsimulator.client.music;

import com.israelsimulator.entity.npc.KirkBossMusicRules;
import com.israelsimulator.entity.npc.OratorEntity;
import com.israelsimulator.registry.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Non-positional Kirk boss song; plays for the duration of the speech and fades out when Kirk dies.
 */
public final class KirkBossMusicInstance extends AbstractTickableSoundInstance {
    private final OratorEntity orator;
    private int fadeTicks;

    public KirkBossMusicInstance(OratorEntity orator) {
        super(ModSoundEvents.KIRK_THEME.get(), SoundSource.MUSIC, RandomSource.create());
        this.orator = orator;
        this.looping = false;
        this.delay = 0;
        this.volume = 1.0F;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    public OratorEntity orator() {
        return this.orator;
    }

    public boolean isFading() {
        return this.fadeTicks > 0;
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        boolean play = mc.player != null && this.orator.level() == mc.player.level()
                && KirkBossMusicRules.shouldPlay(this.orator.isAlive(), this.orator.isRemoved(),
                        this.orator.distanceToSqr(mc.player));
        if (play && this.fadeTicks == 0) {
            return;
        }
        this.fadeTicks++;
        this.volume = KirkBossMusicRules.fadedVolume(this.fadeTicks);
        if (this.volume <= 0.0F) {
            this.stop();
        }
    }
}

