package com.israelsimulator.client.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class LocalWildlifeModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation GAZELLE_LAYER = layer("mountain_gazelle");
    public static final ModelLayerLocation IBEX_LAYER = layer("nubian_ibex");
    public static final ModelLayerLocation HYRAX_LAYER = layer("cape_hyrax");

    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;

    public LocalWildlifeModel(ModelPart root) {
        super(root);
        head = root.getChild("head");
        frontLeftLeg = root.getChild("front_left_leg");
        frontRightLeg = root.getChild("front_right_leg");
        backLeftLeg = root.getChild("back_left_leg");
        backRightLeg = root.getChild("back_right_leg");
    }

    public static LayerDefinition createBodyLayer(String species) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        boolean hyrax = species.equals("cape_hyrax");
        boolean horns = !hyrax;
        float bodyWidth = hyrax ? 8.0F : 7.0F;
        float bodyHeight = hyrax ? 7.0F : 8.0F;
        float legHeight = hyrax ? 5.0F : 9.0F;

        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-bodyWidth / 2, -bodyHeight / 2, -6, bodyWidth, bodyHeight, 12),
                PartPose.offset(0, 12, 1));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20)
                        .addBox(-2.5F, -2.5F, -4, 5, 5, 5)
                        .texOffs(20, 20).addBox(-1.5F, 0, -6, 3, 2, 2),
                PartPose.offset(0, hyrax ? 10 : 9, -6));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(36, 20)
                        .addBox(-0.5F, -3, -0.5F, 1, 3, 1),
                PartPose.offset(2, -1, 0));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(36, 20)
                        .addBox(-0.5F, -3, -0.5F, 1, 3, 1),
                PartPose.offset(-2, -1, 0));

        if (horns) {
            int hornWidth = species.equals("nubian_ibex") ? 2 : 1;
            head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(40, 0)
                            .addBox(-0.5F, -5, -0.5F, hornWidth, 5, 1),
                    PartPose.offset(1.5F, -2, 0));
            head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(40, 0)
                            .addBox(-0.5F, -5, -0.5F, hornWidth, 5, 1),
                    PartPose.offset(-1.5F, -2, 0));
        }

        addLeg(root, "front_left_leg", -2.5F, -4, legHeight);
        addLeg(root, "front_right_leg", 2.5F, -4, legHeight);
        addLeg(root, "back_left_leg", -2.5F, 5, legHeight);
        addLeg(root, "back_right_leg", 2.5F, 5, legHeight);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float swing = (float) Math.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        frontLeftLeg.xRot = swing;
        backRightLeg.xRot = swing;
        frontRightLeg.xRot = -swing;
        backLeftLeg.xRot = -swing;
        head.xRot = state.xRot * ((float) Math.PI / 180.0F);
    }

    private static void addLeg(PartDefinition root, String name, float x, float z, float height) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(40, 12)
                        .addBox(-1, 0, -1, 2, height, 2, CubeDeformation.NONE),
                PartPose.offset(x, 15, z));
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath("israel_simulator", name), "main");
    }
}
