package com.israelsimulator.client.westernwall;

import com.israelsimulator.IsraelSimulator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = IsraelSimulator.MOD_ID, value = Dist.CLIENT)
public final class WesternWallPrayerClientEvents {
    private WesternWallPrayerClientEvents() {}

    /** Call from client mod-bus setup. */
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends net.minecraft.world.entity.Avatar & net.minecraft.client.entity.ClientAvatarEntity>
                    void accept(T avatar, AvatarRenderState renderState) {
                if (avatar instanceof Player && ClientWesternWallPrayer.isPraying(avatar.getId())) {
                    renderState.setRenderData(ClientWesternWallPrayer.PRAYING_KEY, Boolean.TRUE);
                    renderState.xRot = Math.max(renderState.xRot, 35.0F);
                }
            }
        });
    }

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientWesternWallPrayer.clear();
    }

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        if (!ClientWesternWallPrayer.isLocalPlayerPraying()) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        pose.translate(0.0F, 0.05F, -0.25F);
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-80.0F));
    }

    @SubscribeEvent
    static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        if (!ClientWesternWallPrayer.isLocalPlayerPraying()) {
            return;
        }
        event.setPitch(event.getPitch() + 18.0F);
    }

    public static void applyPrayerPose(HumanoidModel<?> model) {
        ModelPart arm = model.rightArm;
        arm.xRot = -85.0F * Mth.DEG_TO_RAD;
        arm.yRot = -10.0F * Mth.DEG_TO_RAD;
        arm.zRot = 0.0F;
        model.head.xRot = Math.max(model.head.xRot, 25.0F * Mth.DEG_TO_RAD);
        model.hat.xRot = model.head.xRot;
        model.hat.yRot = model.head.yRot;
        model.hat.zRot = model.head.zRot;
    }
}
