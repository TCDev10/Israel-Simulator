package com.israelsimulator.client.renderer;

import com.israelsimulator.transport.BicycleEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Client renderer for the rideable bicycle (GAME_DESIGN.md §32, TODO §47).
 */
public class BicycleRenderer extends EntityRenderer<BicycleEntity, EntityRenderState> {

    public BicycleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
