package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.event.PlaneCrashEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Client-side renderer for the incoming commercial airplane (GAME_DESIGN.md §41–44).
 */
public class PlaneCrashRenderer extends EntityRenderer<PlaneCrashEntity, PlaneCrashRenderState> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/plane_crash.png");

    private final PlaneCrashModel model;

    public PlaneCrashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PlaneCrashModel(context.bakeLayer(PlaneCrashModel.LAYER_LOCATION));
    }

    @Override
    public PlaneCrashRenderState createRenderState() {
        return new PlaneCrashRenderState();
    }

    @Override
    public void extractRenderState(PlaneCrashEntity entity, PlaneCrashRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
    }

    @Override
    public void submit(PlaneCrashRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        // Orient along flight direction and scale up to realistic airliner size
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));
        poseStack.scale(3.5F, 3.5F, 3.5F);

        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
