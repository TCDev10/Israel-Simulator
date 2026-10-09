package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.projectile.PalantrioDroneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class PalantrioDroneRenderer extends MobRenderer<PalantrioDroneEntity, LivingEntityRenderState, PalantrioDroneModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/palantrio_drone.png");

    public PalantrioDroneRenderer(EntityRendererProvider.Context context) {
        super(context, new PalantrioDroneModel(context.bakeLayer(PalantrioDroneModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
