package com.israelsimulator.client.renderer;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for Coalition Guards: adds whether the guard is aiming its pistol at a target.
 */
public class BibiGuardRenderState extends HumanoidRenderState {
    /** True while the guard has a target and holds the Security Pistol in its main hand. */
    public boolean aimingPistol;
}
