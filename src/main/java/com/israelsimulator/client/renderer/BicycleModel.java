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

/**
 * Custom 3D entity model for the rideable bicycle (GAME_DESIGN.md §32).
 * Includes frame, spinning wheels, handlebars with bell, saddle, and pedals.
 */
public class BicycleModel extends EntityModel<LivingEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "bicycle"), "main");

    public final ModelPart frame;
    public final ModelPart frontWheel;
    public final ModelPart backWheel;
    public final ModelPart handlebars;
    public final ModelPart pedals;

    public BicycleModel(ModelPart root) {
        super(root);
        ModelPart bike = root.getChild("bike");
        this.frame = bike.getChild("frame");
        this.frontWheel = bike.getChild("front_wheel");
        this.backWheel = bike.getChild("back_wheel");
        this.handlebars = bike.getChild("handlebars");
        this.pedals = bike.getChild("pedals");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bike = root.addOrReplaceChild("bike", CubeListBuilder.create(),
                PartPose.rotation(0.0F, (float) Math.PI, 0.0F));

        // Bicycle main frame (chassis, seat tube, top tube, down tube, chain stays, and comfortable saddle)
        bike.addOrReplaceChild("frame", CubeListBuilder.create()
                // Bottom bracket (crank housing)
                .texOffs(0, 0).addBox(-1.0F, 17.0F, -2.0F, 2.0F, 2.0F, 4.0F)
                // Seat tube
                .texOffs(12, 0).addBox(-0.5F, 10.0F, -2.0F, 1.0F, 7.0F, 1.0F)
                // Bicycle saddle / seat
                .texOffs(16, 0).addBox(-2.0F, 9.0F, -4.5F, 4.0F, 1.0F, 5.0F)
                // Top crossbar tube
                .texOffs(0, 8).addBox(-0.5F, 10.0F, -1.0F, 1.0F, 1.0F, 9.0F)
                // Down tube
                .texOffs(20, 8).addBox(-0.5F, 13.0F, 0.0F, 1.0F, 1.0F, 8.0F)
                // Rear fork (lower stays)
                .texOffs(0, 18).addBox(-1.5F, 17.0F, -8.0F, 1.0F, 1.0F, 7.0F)
                .texOffs(16, 18).addBox(0.5F, 17.0F, -8.0F, 1.0F, 1.0F, 7.0F)
                // Rear seat stays (upper)
                .texOffs(0, 26).addBox(-1.5F, 11.0F, -8.0F, 1.0F, 1.0F, 7.0F)
                .texOffs(16, 26).addBox(0.5F, 11.0F, -8.0F, 1.0F, 1.0F, 7.0F),
                PartPose.ZERO
        );

        // Front wheel (centered at its axle, rotates when moving)
        bike.addOrReplaceChild("front_wheel", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-0.5F, -5.0F, -5.0F, 1.0F, 10.0F, 10.0F),
                PartPose.offset(0.0F, 19.0F, 8.0F)
        );

        // Back wheel (centered at its axle, rotates when moving)
        bike.addOrReplaceChild("back_wheel", CubeListBuilder.create()
                .texOffs(22, 34).addBox(-0.5F, -5.0F, -5.0F, 1.0F, 10.0F, 10.0F),
                PartPose.offset(0.0F, 19.0F, -8.0F)
        );

        // Handlebars, front fork, and bell
        bike.addOrReplaceChild("handlebars", CubeListBuilder.create()
                // Stem / steering column
                .texOffs(44, 0).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                // Front fork blades
                .texOffs(48, 0).addBox(-1.2F, 0.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(52, 0).addBox(0.2F, 0.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                // Horizontal handlebar
                .texOffs(36, 12).addBox(-6.0F, -3.0F, -0.5F, 12.0F, 1.0F, 1.0F)
                // Left and right rubber grips
                .texOffs(36, 15).addBox(-6.5F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                .texOffs(42, 15).addBox(5.5F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                // Golden brass bell
                .texOffs(48, 15).addBox(2.0F, -4.0F, -0.5F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 10.0F, 8.0F)
        );

        // Pedals and crank axle
        bike.addOrReplaceChild("pedals", CubeListBuilder.create()
                // Crank axle
                .texOffs(44, 20).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F)
                // Left crank arm and pedal pad
                .texOffs(44, 23).addBox(-2.5F, -0.5F, -2.5F, 1.0F, 1.0F, 3.0F)
                .texOffs(52, 23).addBox(-4.0F, -0.5F, -3.5F, 2.0F, 1.0F, 2.0F)
                // Right crank arm and pedal pad
                .texOffs(44, 27).addBox(1.5F, -0.5F, -0.5F, 1.0F, 1.0F, 3.0F)
                .texOffs(52, 27).addBox(2.0F, -0.5F, 1.5F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        // Animate wheel rotation and pedal spinning when moving
        float walkPos = state.walkAnimationPos;
        this.frontWheel.xRot = walkPos * 0.8F;
        this.backWheel.xRot = walkPos * 0.8F;
        this.pedals.xRot = walkPos * 0.8F;
    }
}

