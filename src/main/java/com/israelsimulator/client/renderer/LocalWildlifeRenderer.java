package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.wildlife.RegionalWildlifeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class LocalWildlifeRenderer<T extends RegionalWildlifeEntity>
        extends MobRenderer<T, LivingEntityRenderState, LocalWildlifeModel> {
    private final Identifier texture;
    private final float modelScale;

    public LocalWildlifeRenderer(EntityRendererProvider.Context context,
                                 net.minecraft.client.model.geom.ModelLayerLocation layer,
                                 String textureName, float shadowRadius, float modelScale) {
        super(context, new LocalWildlifeModel(context.bakeLayer(layer)), shadowRadius);
        texture = Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID,
                "textures/entity/" + textureName + ".png");
        this.modelScale = modelScale;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        if (modelScale != 1.0F) {
            poseStack.scale(modelScale, modelScale, modelScale);
        }
    }
}
