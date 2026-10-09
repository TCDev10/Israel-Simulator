package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.IceAgentEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Client-side renderer for ICE Federal Agents with full Netherite armor layer rendering (GAME_DESIGN.md §41–44).
 */
public class IceAgentRenderer extends HumanoidMobRenderer<IceAgentEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/ice_agent.png");

    public IceAgentRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer()));
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

