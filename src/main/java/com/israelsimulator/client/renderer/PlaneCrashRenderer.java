package com.israelsimulator.client.renderer;

import com.israelsimulator.entity.event.PlaneCrashEntity;
import com.israelsimulator.entity.event.PlaneModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Client-side renderer for the incoming airplane.
 * Renders the REAL blocks from plane1-e1160.schem (1034 schematic blocks) moving through the air!
 */
public class PlaneCrashRenderer extends EntityRenderer<PlaneCrashEntity, PlaneCrashRenderState> {

    public record PreparedBlock(int dx, int dy, int dz, BlockModelRenderState modelState) {}

    private final List<PreparedBlock> preparedBlocks = new ArrayList<>();

    public PlaneCrashRenderer(EntityRendererProvider.Context context) {
        super(context);
        BlockModelResolver resolver = context.getBlockModelResolver();
        BlockDisplayContext displayContext = BlockDisplayContext.create();

        // Bake the schematic blocks into cached BlockModelRenderStates
        List<PlaneModelData.BlockEntry> entries = PlaneModelData.getBlocks();
        for (PlaneModelData.BlockEntry entry : entries) {
            if (entry.state() != null && !entry.state().isAir()) {
                BlockModelRenderState bState = new BlockModelRenderState();
                resolver.update(bState, entry.state(), displayContext);
                preparedBlocks.add(new PreparedBlock(entry.dx(), entry.dy(), entry.dz(), bState));
            }
        }
    }

    @Override
    public boolean shouldRender(PlaneCrashEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return true;
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
        state.lightCoords = 15728880; // Full daylight illumination (packed light 15, 15)
    }

    @Override
    public void submit(PlaneCrashRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        // Orient schematic blocks along flight direction.
        // In the schematic: nose is at -X, tail is at +X, wings along Z.
        // Rotation (90 - yRot) correctly points nose forward along the flight trajectory.
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - state.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        // Center block offset (-0.5 so blocks align properly to grid)
        poseStack.translate(-0.5, -0.5, -0.5);

        // Render each block from the schematic
        for (PreparedBlock block : preparedBlocks) {
            poseStack.pushPose();
            poseStack.translate(block.dx, block.dy, block.dz);
            block.modelState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
