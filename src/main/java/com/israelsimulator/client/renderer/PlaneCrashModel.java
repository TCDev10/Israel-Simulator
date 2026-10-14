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
import net.minecraft.resources.Identifier;

/**
 * 3D Model of the commercial airliner (GAME_DESIGN.md §41–44).
 * Fuselage, nose cone, cabin, jet engines, wings, and vertical/horizontal stabilizers.
 */
public class PlaneCrashModel extends EntityModel<PlaneCrashRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "plane_crash"), "main");

    public PlaneCrashModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition plane = root.addOrReplaceChild("plane", CubeListBuilder.create(), PartPose.ZERO);

        // Main cylindrical fuselage (long tube along X/Z axis)
        // Center at (0, 0, 0), length = 80 units (~5 blocks scaled), width = 16, height = 16
        plane.addOrReplaceChild("fuselage", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -8.0F, -30.0F, 16.0F, 16.0F, 60.0F),
                PartPose.ZERO);

        // Cockpit / Nose cone tapering forward
        plane.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-6.0F, -6.0F, 30.0F, 12.0F, 12.0F, 16.0F),
                PartPose.ZERO);

        // Main Swept Wings (Left & Right)
        plane.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 32).addBox(8.0F, -1.0F, -10.0F, 48.0F, 2.0F, 20.0F),
                PartPose.ZERO);

        plane.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-56.0F, -1.0F, -10.0F, 48.0F, 2.0F, 20.0F),
                PartPose.ZERO);

        // Jet Engines underneath wings
        plane.addOrReplaceChild("left_engine", CubeListBuilder.create()
                .texOffs(16, 0).addBox(22.0F, 2.0F, -6.0F, 6.0F, 6.0F, 16.0F),
                PartPose.ZERO);

        plane.addOrReplaceChild("right_engine", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-28.0F, 2.0F, -6.0F, 6.0F, 6.0F, 16.0F),
                PartPose.ZERO);

        // Tail vertical stabilizer (rudder)
        plane.addOrReplaceChild("rudder", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1.5F, -26.0F, -32.0F, 3.0F, 18.0F, 14.0F),
                PartPose.ZERO);

        // Tail horizontal stabilizers
        plane.addOrReplaceChild("tail_wings", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-20.0F, -6.0F, -34.0F, 40.0F, 2.0F, 10.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }
}

