package com.israelsimulator.client.renderer;

import com.israelsimulator.entity.npc.shuk.ShukVendorEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

/** Shuk vendor: a plain villager (vanilla villager model and base skin). */
public class ShukVendorRenderer extends MobRenderer<ShukVendorEntity, VillagerRenderState, VillagerModel> {
    private static final Identifier SKIN = Identifier.withDefaultNamespace("textures/entity/villager/villager.png");

    public ShukVendorRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.addLayer(new CrossedArmsItemLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return SKIN;
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public void extractRenderState(ShukVendorEntity entity, VillagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HoldingEntityRenderState.extractHoldingEntityRenderState(entity, state, this.itemModelResolver);
    }
}
