package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.BibiGuardEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Client-side renderer for Coalition Guards summoned by Bibi Boss (GAME_DESIGN.md §43, TODO §44).
 */
public class BibiGuardRenderer extends HumanoidMobRenderer<BibiGuardEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/bibi_guard.png");

    public BibiGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
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
