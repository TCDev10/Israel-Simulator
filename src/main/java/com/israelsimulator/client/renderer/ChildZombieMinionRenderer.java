package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.entity.boss.ChildZombieMinionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Client-side renderer for child-skinned baby zombie minions (GAME_DESIGN.md §41–44).
 */
public class ChildZombieMinionRenderer extends HumanoidMobRenderer<ChildZombieMinionEntity, ChildZombieMinionRenderState, HumanoidModel<ChildZombieMinionRenderState>> {

    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/child_zombie.png");
    public static final Identifier TEXTURE_1 =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/child_zombie_1.png");
    public static final Identifier TEXTURE_2 =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/child_zombie_2.png");
    public static final Identifier TEXTURE_3 =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/child_zombie_3.png");
    public static final Identifier TEXTURE_4 =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/child_zombie_4.png");
    public static final Identifier TEXTURE_5 =
            Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "textures/entity/child_zombie_5.png");

    public static final Identifier[] TEXTURES = {
            TEXTURE, TEXTURE_1, TEXTURE_2, TEXTURE_3, TEXTURE_4, TEXTURE_5
    };

    public ChildZombieMinionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.25F);
    }

    @Override
    public Identifier getTextureLocation(ChildZombieMinionRenderState state) {
        int index = Math.floorMod(state.skinVariant, TEXTURES.length);
        return TEXTURES[index];
    }

    @Override
    public ChildZombieMinionRenderState createRenderState() {
        ChildZombieMinionRenderState state = new ChildZombieMinionRenderState();
        state.isBaby = true;
        return state;
    }

    @Override
    public void extractRenderState(ChildZombieMinionEntity entity, ChildZombieMinionRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        long seed = entity.getUUID().getLeastSignificantBits();
        state.skinVariant = (int) Math.abs(seed % TEXTURES.length);
    }

    @Override
    protected void scale(ChildZombieMinionRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }
}
