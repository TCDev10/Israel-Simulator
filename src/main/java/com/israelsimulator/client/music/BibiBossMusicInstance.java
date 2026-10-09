package com.israelsimulator.client.music;

import com.israelsimulator.entity.boss.BibiBossEntity;
import com.israelsimulator.entity.boss.BibiBossMusicRules;
import com.israelsimulator.registry.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Looping, non-positional Bibi boss song; fades out when the fight ends or the player leaves. */
public final class BibiBossMusicInstance extends AbstractTickableSoundInstance {
    private final BibiBossEntity boss;
    private int fadeTicks;

    public BibiBossMusicInstance(BibiBossEntity boss) {
        super(ModSoundEvents.BIBI_THEME.get(), SoundSource.MUSIC, RandomSource.create());
        this.boss = boss;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    public BibiBossEntity boss() {
        return this.boss;
    }

    public boolean isFading() {
        return this.fadeTicks > 0;
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        boolean play = mc.player != null && this.boss.level() == mc.player.level()
                && BibiBossMusicRules.shouldPlay(this.boss.isAlive(), this.boss.isRemoved(),
                        this.boss.distanceToSqr(mc.player));
        if (play && this.fadeTicks == 0) {
            return;
        }
        this.fadeTicks++;
        this.volume = BibiBossMusicRules.fadedVolume(this.fadeTicks);
        if (this.volume <= 0.0F) {
            this.stop();
        }
    }
}
