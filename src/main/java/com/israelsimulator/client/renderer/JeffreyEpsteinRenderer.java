package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.JeffreyEpsteinEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Client-side renderer for Jeffrey Epstein satirical miniboss (GAME_DESIGN.md §41–44).
 */
public class JeffreyEpsteinRenderer extends HumanoidMobRenderer<JeffreyEpsteinEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/jeffrey_epstein.png");

    public JeffreyEpsteinRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.55F);
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }
}

