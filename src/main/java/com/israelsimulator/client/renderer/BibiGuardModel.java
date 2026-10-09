package com.israelsimulator.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Humanoid model for Coalition Guards that keeps the 3D pistol pointing where the guard looks.
 *
 * <p>The pistol's {@code thirdperson_*hand} display transform is tuned for the vanilla ITEM arm pose
 * (arm lowered, pitched {@value #ITEM_ARM_PITCH_DEG} degrees forward), like a player holding it. While a guard
 * aims, its arms are raised with the BOW_AND_ARROW pose; without a correction the pistol would rotate with the
 * arm and point at the sky. {@link #translateToHand} re-pitches the held item around the grip so the barrel
 * follows the head pitch while the grip stays in the fist.</p>
 */
public class BibiGuardModel extends HumanoidModel<BibiGuardRenderState> {

    static final float ITEM_ARM_PITCH_DEG = -18.0F;
    private static final float ITEM_ARM_PITCH = (float) Math.toRadians(ITEM_ARM_PITCH_DEG);
    /**
     * Grip position in arm space (pixels) produced by models/item/pistol.json's third-person transform:
     * x = -1 (right arm) / +1 (left arm), y = 7.5 pixels below the shoulder pivot, z = 0.
     */
    static final float GRIP_X = 1.0F;
    static final float GRIP_Y = 7.5F;

    public BibiGuardModel(ModelPart root) {
        super(root);
    }

    @Override
    public void translateToHand(HumanoidRenderState state, HumanoidArm arm, PoseStack poseStack) {
        super.translateToHand(state, arm, poseStack);
        if (state instanceof BibiGuardRenderState guard && guard.aimingPistol && arm == guard.mainArm) {
            ModelPart armPart = this.getArm(arm);
            float correction = (ITEM_ARM_PITCH + this.head.xRot) - armPart.xRot;
            float gx = (arm == HumanoidArm.RIGHT ? -GRIP_X : GRIP_X) / 16.0F;
            float gy = GRIP_Y / 16.0F;
            poseStack.translate(gx, gy, 0.0F);
            poseStack.mulPose(Axis.XP.rotation(correction));
            poseStack.translate(-gx, -gy, 0.0F);
        }
    }
}
