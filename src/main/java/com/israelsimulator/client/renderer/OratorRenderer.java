package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.npc.OratorEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/**
 * Player-shaped renderer for the fictional Orator. The skin is a standard 64x64 (wide) player skin at
 * {@code assets/israel_simulator/textures/entity/orator.png}: replace that file to change the look.
 *
 * <p>Must NOT use {@code AvatarRenderState}: in 26.2 {@code EntityRenderDispatcher.getRenderer(state)} sends
 * every {@code AvatarRenderState} to the vanilla player renderer, which draws {@code state.skin} (default Steve)
 * and never calls this renderer's {@link #getTextureLocation}.
 */
public class OratorRenderer extends HumanoidMobRenderer<OratorEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/orator.png");

    public OratorRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
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
