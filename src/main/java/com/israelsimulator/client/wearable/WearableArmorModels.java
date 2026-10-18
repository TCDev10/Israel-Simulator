package com.israelsimulator.client.wearable;

import com.israelsimulator.item.cultural.TalitItem;
import com.israelsimulator.registry.ModItems;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Real 3D worn geometry for Kippah, Talit and Tefillin.
 *
 * <p>The three items keep their vanilla equipment assets (so the armor layer draws them on
 * players, humanoid mobs and armor stands), but instead of painting a flat texture on the
 * inflated helmet/chestplate/leggings box we hand the armor layer a small dedicated model:
 * a cap on the back of the head, a shawl over shoulders and back with tzitzit, and the
 * tefillin boxes on the forehead and the left arm with their straps. The model copies the
 * pose of the original armor model every frame, so it follows head rotation, sneaking,
 * walking and armor-stand poses.</p>
 *
 * <p>Baby humanoids (baby zombies, husks, drowned, piglins...) get their own smaller geometry,
 * fitted to the vanilla baby body (6x6x6 head, 4x5x2 body, 2x5x2 arms) and expressed relative to
 * the pivots of the baby armor model it copies the pose from (head 0/15/0, body 0/18/0, arms
 * +-3.5/15.5/0). The baby layers use the same textures as the adult ones.</p>
 */
public final class WearableArmorModels {

    private WearableArmorModels() {}

    // ---------------------------------------------------------------- geometry

    /** Kippah: knitted cap on the crown/back of the head, above the hat layer. Texture 32x16. */
    static LayerDefinition kippahLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -9.25F, -1.4F, 6.0F, 1.0F, 6.0F)
                        .texOffs(0, 8).addBox(-2.0F, -9.75F, -0.4F, 4.0F, 0.5F, 4.0F),
                PartPose.ZERO);
        empty(root, "body", "left_arm", "right_arm");
        return LayerDefinition.create(mesh, 32, 16);
    }

    /** Talit: shawl over the back, two front panels, sleeves over the upper arms, four tzitzit. Texture 64x32. */
    static LayerDefinition talitLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation panel = new CubeDeformation(0.6F, 0.3F, 0.1F);
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 0.0F, 2.0F, 8.0F, 13.0F, 1.0F, panel)
                        .texOffs(20, 0).addBox(-4.0F, 0.0F, -3.0F, 2.0F, 13.0F, 1.0F, panel)
                        .texOffs(28, 0).addBox(2.0F, 0.0F, -3.0F, 2.0F, 13.0F, 1.0F, panel)
                        .texOffs(40, 0).addBox(-4.4F, 13.3F, 2.4F, 0.5F, 3.0F, 0.5F)
                        .texOffs(40, 0).addBox(3.9F, 13.3F, 2.4F, 0.5F, 3.0F, 0.5F)
                        .texOffs(40, 0).addBox(-4.4F, 13.3F, -3.0F, 0.5F, 3.0F, 0.5F)
                        .texOffs(40, 0).addBox(3.9F, 13.3F, -3.0F, 0.5F, 3.0F, 0.5F),
                PartPose.ZERO);
        CubeDeformation sleeve = new CubeDeformation(0.75F);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, sleeve), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, sleeve), PartPose.ZERO);
        empty(root, "head");
        return LayerDefinition.create(mesh, 64, 32);
    }

    /**
     * Tefillin: shel rosh (box on the forehead, plate, strap around the head, knot at the back,
     * straps down the chest) and shel yad (box on the upper left arm, windings down the forearm).
     * Texture 64x32.
     */
    static LayerDefinition tefillinLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.5F, -8.3F, -6.4F, 3.0F, 3.0F, 2.0F)
                        .texOffs(0, 6).addBox(-2.0F, -5.3F, -6.6F, 4.0F, 0.6F, 2.2F)
                        .texOffs(0, 10).addBox(-4.0F, -5.5F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.53F, -0.2F, 0.53F))
                        .texOffs(34, 0).addBox(-1.0F, -6.0F, 4.5F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);
        // Straps hanging down the chest: one pair flat on the shirt, one pair raised over a worn Talit.
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(40, 0).addBox(-3.2F, 0.0F, -2.6F, 0.8F, 9.0F, 0.3F)
                        .texOffs(44, 0).addBox(2.4F, 0.0F, -2.6F, 0.8F, 9.0F, 0.3F),
                PartPose.ZERO);
        root.addOrReplaceChild("body_over_talit", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-3.2F, 0.0F, -3.5F, 0.8F, 9.0F, 0.3F)
                        .texOffs(52, 0).addBox(2.4F, 0.0F, -3.5F, 0.8F, 9.0F, 0.3F),
                PartPose.ZERO);
        CubeDeformation winding = new CubeDeformation(0.35F, -0.3F, 0.35F);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-0.5F, 0.5F, -3.4F, 2.0F, 2.0F, 1.2F)
                        .texOffs(36, 22).addBox(0.2F, 2.5F, -2.6F, 0.6F, 6.0F, 0.3F)
                        .texOffs(16, 22).addBox(-1.0F, 4.5F, -2.0F, 4.0F, 1.0F, 4.0F, winding)
                        .texOffs(16, 22).addBox(-1.0F, 6.5F, -2.0F, 4.0F, 1.0F, 4.0F, winding)
                        .texOffs(16, 22).addBox(-1.0F, 8.5F, -2.0F, 4.0F, 1.0F, 4.0F, winding),
                PartPose.ZERO);
        empty(root, "right_arm");
        return LayerDefinition.create(mesh, 64, 32);
    }

    // ---------------------------------------------------------------- baby geometry
    // Baby armor pivots: head (0, 15, 0), body (0, 18, 0), arms (+-3.5, 15.5, 0). The baby head
    // spans y -6..0 and x/z -3..3 from the head pivot (hat overlay 0.25 larger), the body
    // y -3..2, x -2..2, z -1..1 from the body pivot, each arm is 2x5x2 centred 0.5 inwards of the
    // armor arm pivot.

    /** Kippah on a baby head: same cap, scaled to the 6-wide head. */
    static LayerDefinition kippahBabyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.25F, -6.9F, -1.0F, 4.5F, 0.75F, 4.5F)
                        .texOffs(0, 8).addBox(-1.5F, -7.3F, -0.25F, 3.0F, 0.4F, 3.0F),
                PartPose.ZERO);
        empty(root, "body", "left_arm", "right_arm");
        return LayerDefinition.create(mesh, 32, 16);
    }

    /** Talit on a baby: back panel, front panels, short sleeves, four tzitzit. */
    static LayerDefinition talitBabyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation panel = new CubeDeformation(0.35F, 0.2F, 0.05F);
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -3.0F, 1.0F, 4.0F, 5.5F, 0.6F, panel)
                        .texOffs(20, 0).addBox(-2.0F, -3.0F, -1.6F, 1.0F, 5.5F, 0.6F, panel)
                        .texOffs(28, 0).addBox(1.0F, -3.0F, -1.6F, 1.0F, 5.5F, 0.6F, panel)
                        .texOffs(40, 0).addBox(-2.45F, 2.7F, 1.25F, 0.35F, 1.6F, 0.35F)
                        .texOffs(40, 0).addBox(2.1F, 2.7F, 1.25F, 0.35F, 1.6F, 0.35F)
                        .texOffs(40, 0).addBox(-2.45F, 2.7F, -1.75F, 0.35F, 1.6F, 0.35F)
                        .texOffs(40, 0).addBox(2.1F, 2.7F, -1.75F, 0.35F, 1.6F, 0.35F),
                PartPose.ZERO);
        CubeDeformation sleeve = new CubeDeformation(0.4F);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-0.5F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F, sleeve), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-1.5F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F, sleeve), PartPose.ZERO);
        empty(root, "head");
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Tefillin on a baby: forehead box with plate, head strap and knot, chest straps, arm box and windings. */
    static LayerDefinition tefillinBabyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -5.6F, -4.6F, 2.0F, 2.0F, 1.4F)
                        .texOffs(0, 6).addBox(-1.4F, -3.6F, -4.75F, 2.8F, 0.4F, 1.6F)
                        .texOffs(0, 10).addBox(-3.0F, -3.7F, -3.0F, 6.0F, 0.7F, 6.0F, new CubeDeformation(0.33F, -0.1F, 0.33F))
                        .texOffs(34, 0).addBox(-0.75F, -4.0F, 3.3F, 1.5F, 1.5F, 0.7F),
                PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(40, 0).addBox(-1.6F, -3.0F, -1.25F, 0.6F, 5.5F, 0.2F)
                        .texOffs(44, 0).addBox(1.0F, -3.0F, -1.25F, 0.6F, 5.5F, 0.2F),
                PartPose.ZERO);
        root.addOrReplaceChild("body_over_talit", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-1.6F, -3.0F, -1.9F, 0.6F, 5.5F, 0.2F)
                        .texOffs(52, 0).addBox(1.0F, -3.0F, -1.9F, 0.6F, 5.5F, 0.2F),
                PartPose.ZERO);
        CubeDeformation winding = new CubeDeformation(0.25F, -0.15F, 0.25F);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-1.2F, 0.0F, -1.9F, 1.4F, 1.4F, 0.9F)
                        .texOffs(36, 22).addBox(-0.7F, 1.4F, -1.25F, 0.4F, 3.0F, 0.2F)
                        .texOffs(16, 22).addBox(-1.5F, 2.0F, -1.0F, 2.0F, 0.6F, 2.0F, winding)
                        .texOffs(16, 22).addBox(-1.5F, 3.0F, -1.0F, 2.0F, 0.6F, 2.0F, winding)
                        .texOffs(16, 22).addBox(-1.5F, 4.0F, -1.0F, 2.0F, 0.6F, 2.0F, winding),
                PartPose.ZERO);
        empty(root, "right_arm");
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void empty(PartDefinition root, String... names) {
        for (String name : names) {
            root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
        }
    }

    // ---------------------------------------------------------------- runtime model

    /** Wraps the vanilla armor model the layer would have used and copies its pose each frame. */
    static final class WornModel extends Model<HumanoidRenderState> {
        private final HumanoidModel<?> original;
        private final ModelPart head;
        private final ModelPart body;
        private final ModelPart bodyOverTalit;
        private final ModelPart rightArm;
        private final ModelPart leftArm;

        WornModel(ModelPart root, HumanoidModel<?> original) {
            super(root, RenderTypes::armorCutoutNoCull);
            this.original = original;
            this.head = root.getChild("head");
            this.body = root.getChild("body");
            this.bodyOverTalit = root.hasChild("body_over_talit") ? root.getChild("body_over_talit") : null;
            this.rightArm = root.getChild("right_arm");
            this.leftArm = root.getChild("left_arm");
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void setupAnim(HumanoidRenderState state) {
            ((Model) this.original).setupAnim(state);
            this.resetPose();
            this.head.loadPose(this.original.head.storePose());
            this.body.loadPose(this.original.body.storePose());
            this.rightArm.loadPose(this.original.rightArm.storePose());
            this.leftArm.loadPose(this.original.leftArm.storePose());
            if (this.bodyOverTalit != null) {
                boolean talit = state.chestEquipment.getItem() instanceof TalitItem;
                this.bodyOverTalit.loadPose(this.original.body.storePose());
                this.bodyOverTalit.visible = talit;
                this.body.visible = !talit;
            }
        }
    }

    /** One wrapper per (item, original armor model); renderers rebuilt on resource reload get fresh ones. */
    private static final class Extensions implements IClientItemExtensions {
        private final java.util.function.Supplier<LayerDefinition> layer;
        private final java.util.function.Supplier<LayerDefinition> babyLayer;
        private final Map<Model<?>, WornModel> cache = new WeakHashMap<>();

        Extensions(java.util.function.Supplier<LayerDefinition> layer, java.util.function.Supplier<LayerDefinition> babyLayer) {
            this.layer = layer;
            this.babyLayer = babyLayer;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Model getGenericArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
            if (!(original instanceof HumanoidModel<?> humanoid)) {
                return original;
            }
            boolean baby = layerType == EquipmentClientInfo.LayerType.HUMANOID_BABY;
            return this.cache.computeIfAbsent(original,
                    o -> new WornModel((baby ? this.babyLayer : this.layer).get().bakeRoot(), humanoid));
        }
    }

    public static void register(RegisterClientExtensionsEvent event) {
        event.registerItem(new Extensions(WearableArmorModels::kippahLayer, WearableArmorModels::kippahBabyLayer), ModItems.KIPPAH.get());
        event.registerItem(new Extensions(WearableArmorModels::talitLayer, WearableArmorModels::talitBabyLayer), ModItems.TALIT.get());
        event.registerItem(new Extensions(WearableArmorModels::tefillinLayer, WearableArmorModels::tefillinBabyLayer), ModItems.TEFILLIN.get());
    }
}
