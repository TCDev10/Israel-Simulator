package com.israelsimulator.mixin.client;

import com.israelsimulator.client.westernwall.ClientWesternWallPrayer;
import com.israelsimulator.client.westernwall.WesternWallPrayerClientEvents;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelPrayerMixin<T extends HumanoidRenderState> {
    @Inject(method = "setupAnim" , at = @At("RETURN"))
    private void israelSimulator$prayerPose(T state, CallbackInfo ci) {
        Boolean praying = state.getRenderData(ClientWesternWallPrayer.PRAYING_KEY);
        if (praying == null || !praying) {
            // Fallback: AvatarRenderState.id for tracking without render data
            if (state instanceof AvatarRenderState avatar && ClientWesternWallPrayer.isPraying(avatar.id)) {
                WesternWallPrayerClientEvents.applyPrayerPose((HumanoidModel<?>) (Object) this);
            }
            return;
        }
        WesternWallPrayerClientEvents.applyPrayerPose((HumanoidModel<?>) (Object) this);
    }
}
