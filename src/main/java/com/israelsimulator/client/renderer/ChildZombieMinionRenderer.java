package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.ChildZombieMinionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Client-side renderer for child-skinned baby zombie minions (GAME_DESIGN.md §41–44).
 */
public class ChildZombieMinionRenderer extends HumanoidMobRenderer<ChildZombieMinionEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/child_zombie.png");

    public ChildZombieMinionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.25F);
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        HumanoidRenderState state = new HumanoidRenderState();
        state.isBaby = true;
        return state;
    }

    @Override
    protected void scale(HumanoidRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }
}

