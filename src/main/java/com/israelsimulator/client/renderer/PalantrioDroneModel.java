package com.israelsimulator.client.renderer;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Quadcopter: central body with camera, four arms with motors and four spinning rotors. */
public class PalantrioDroneModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "palantrio_drone"), "main");
    static final String[] ROTORS = {"rotor_fl", "rotor_fr", "rotor_bl", "rotor_br"};

    private final ModelPart[] rotors = new ModelPart[4];

    public PalantrioDroneModel(ModelPart root) {
        super(root);
        ModelPart drone = root.getChild("drone");
        for (int i = 0; i < 4; i++) {
            rotors[i] = drone.getChild(ROTORS[i]);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition drone = root.addOrReplaceChild("drone", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -1.5F, -3.0F, 5.0F, 2.0F, 6.0F)     // body
                        .texOffs(0, 8).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 1.0F, 4.0F)     // battery
                        .texOffs(14, 8).addBox(-1.0F, 0.5F, -3.5F, 2.0F, 2.0F, 2.0F)     // camera gimbal
                        .texOffs(0, 14).addBox(-7.0F, -1.0F, -0.5F, 14.0F, 1.0F, 1.0F, true) // arm X
                        .texOffs(0, 16).addBox(-0.5F, -1.0F, -7.0F, 1.0F, 1.0F, 14.0F)    // arm Z
                        .texOffs(22, 0).addBox(-2.0F, 0.5F, 2.0F, 1.0F, 2.0F, 1.0F)      // landing legs
                        .texOffs(22, 0).addBox(1.0F, 0.5F, 2.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        float[][] pos = {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}};
        for (int i = 0; i < 4; i++) {
            PartDefinition motorHolder = drone;
            motorHolder.addOrReplaceChild("motor_" + i, CubeListBuilder.create()
                            .texOffs(26, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                    PartPose.offset(pos[i][0], 0.0F, pos[i][1]));
            drone.addOrReplaceChild(ROTORS[i], CubeListBuilder.create()
                            .texOffs(0, 31).addBox(-4.0F, -0.25F, -0.5F, 8.0F, 0.5F, 1.0F)
                            .texOffs(18, 31).addBox(-0.5F, -0.3F, -0.5F, 1.0F, 0.6F, 1.0F),
                    PartPose.offset(pos[i][0], -2.3F, pos[i][1]));
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float spin = state.ageInTicks * 1.7F;
        for (int i = 0; i < 4; i++) {
            rotors[i].yRot = (i % 2 == 0 ? spin : -spin) + i;
        }
    }
}
