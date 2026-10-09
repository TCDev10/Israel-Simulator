package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.BibiBossEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Client-side renderer for the satirical Bibi Boss (GAME_DESIGN.md §41, TODO §43).
 */
public class BibiBossRenderer extends HumanoidMobRenderer<BibiBossEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/bibi_boss.png");

    public BibiBossRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.6F);
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
