package com.israelsimulator.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Drifting green banknote particle emitted during Donald Trump miniboss attacks.
 */
@OnlyIn(Dist.CLIENT)
public class DollarBillParticle extends SingleQuadParticle {

    private final SpriteSet sprites;

    public DollarBillParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed,
                              SpriteSet sprites, TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        this.sprites = sprites;
        this.lifetime = 35 + this.random.nextInt(25);
        this.gravity = 0.20F;
        this.friction = 0.95F;
        this.quadSize = 0.20F + this.random.nextFloat() * 0.08F;
        this.xd = xSpeed + (this.random.nextDouble() - 0.5) * 0.15;
        this.yd = ySpeed + 0.08 + this.random.nextDouble() * 0.08;
        this.zd = zSpeed + (this.random.nextDouble() - 0.5) * 0.15;
        this.roll = this.random.nextFloat() * ((float) Math.PI * 2F);
        this.oRoll = this.roll;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += 0.06F;
        this.setSpriteFromAge(this.sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new DollarBillParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.sprites.get(random));
        }
    }
}

