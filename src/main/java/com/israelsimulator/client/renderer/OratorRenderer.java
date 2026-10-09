package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.npc.OratorEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

/**
 * Player-model renderer for the Orator. The skin is a standard 64x64 player skin at
 * {@code assets/israel_simulator/textures/entity/orator.png}: replace that file to change the look.
 */
public class OratorRenderer extends HumanoidMobRenderer<OratorEntity, AvatarRenderState, PlayerModel> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/orator.png");

    public OratorRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        return TEXTURE;
    }

    @Override
    public AvatarRenderState createRenderState() {
        AvatarRenderState state = new AvatarRenderState();
        state.showCape = false;
        return state;
    }
}
