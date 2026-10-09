package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.PathfinderMob;

/** Player-shaped renderer for the friendly Mossad Handler and the escorted informant. */
public class MossadNpcRenderer<T extends PathfinderMob> extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private final Identifier texture;

    public MossadNpcRenderer(EntityRendererProvider.Context context, String skin) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.texture = textureFor(skin);
    }

    public static Identifier textureFor(String skin) {
        return Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/" + skin + ".png");
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return texture;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }
}
