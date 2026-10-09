package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.mossad.MossadAgentEntity;
import com.israelsimulator.item.combat.FirearmItem;
import com.israelsimulator.item.combat.SecurityPistolItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;

/**
 * Mossad Agent: player-shaped model in a dark suit. Holds the Uzi/pistol like a player and aims it with
 * raised arms (same grip correction as {@link BibiGuardModel}; every firearm shares the pistol's grip).
 */
public class MossadAgentRenderer extends HumanoidMobRenderer<MossadAgentEntity, BibiGuardRenderState, BibiGuardModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/mossad_agent.png");

    public MossadAgentRenderer(EntityRendererProvider.Context context) {
        super(context, new BibiGuardModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(BibiGuardRenderState state) {
        return TEXTURE;
    }

    @Override
    public BibiGuardRenderState createRenderState() {
        return new BibiGuardRenderState();
    }

    static boolean isGun(Item item) {
        return item instanceof SecurityPistolItem || item instanceof FirearmItem;
    }

    @Override
    public void extractRenderState(MossadAgentEntity entity, BibiGuardRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.aimingPistol = entity.isAggressive() && isGun(entity.getMainHandItem().getItem());
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(MossadAgentEntity mob, HumanoidArm arm) {
        if (arm == mob.getMainArm() && isGun(mob.getMainHandItem().getItem())) {
            return mob.isAggressive() ? HumanoidModel.ArmPose.BOW_AND_ARROW : HumanoidModel.ArmPose.ITEM;
        }
        return super.getArmPose(mob, arm);
    }
}
