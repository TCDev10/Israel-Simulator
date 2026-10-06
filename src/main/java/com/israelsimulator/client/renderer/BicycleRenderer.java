package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.transport.BicycleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client renderer for the rideable bicycle (GAME_DESIGN.md §32, TODO §47).
 * Renders the custom 3D model with spinning wheels and animated pedals.
 */
@OnlyIn(Dist.CLIENT)
public class BicycleRenderer extends MobRenderer<BicycleEntity, LivingEntityRenderState, BicycleModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/bicycle.png");

    public BicycleRenderer(EntityRendererProvider.Context context) {
        super(context, new BicycleModel(context.bakeLayer(BicycleModel.LAYER_LOCATION)), 0.5F);
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

