package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.BibiGuardEntity;
import com.israelsimulator.item.combat.SecurityPistolItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Client-side renderer for Coalition Guards summoned by Bibi Boss (GAME_DESIGN.md §43, TODO §44).
 * Guards hold the Security Pistol like a player (ITEM pose) and raise both arms to aim
 * (BOW_AND_ARROW pose) while they have a target.
 */
public class BibiGuardRenderer extends HumanoidMobRenderer<BibiGuardEntity, BibiGuardRenderState, BibiGuardModel> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/bibi_guard.png");

    public BibiGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new BibiGuardModel(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(BibiGuardRenderState state) {
        return TEXTURE;
    }

    @Override
    public BibiGuardRenderState createRenderState() {
        return new BibiGuardRenderState();
    }

    @Override
    public void extractRenderState(BibiGuardEntity entity, BibiGuardRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.aimingPistol = isAimingPistol(entity);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(BibiGuardEntity mob, HumanoidArm arm) {
        if (arm == mob.getMainArm() && mob.getMainHandItem().getItem() instanceof SecurityPistolItem) {
            return isAimingPistol(mob) ? HumanoidModel.ArmPose.BOW_AND_ARROW : HumanoidModel.ArmPose.ITEM;
        }
        return super.getArmPose(mob, arm);
    }

    static boolean isAimingPistol(BibiGuardEntity mob) {
        return mob.isAggressive() && mob.getMainHandItem().getItem() instanceof SecurityPistolItem;
    }
}
