package com.israelsimulator.client;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.item.combat.FirearmItem;
import com.israelsimulator.network.ReloadWeaponPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Client key bindings for firearms: R reloads the weapon in the main hand.
 */
@EventBusSubscriber(modid = IsraelSimulator.MOD_ID, value = Dist.CLIENT)
public final class WeaponKeys {
    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "weapons"));
    public static final KeyMapping RELOAD = new KeyMapping("key.israel_simulator.reload",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);

    private WeaponKeys() {}

    static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(RELOAD);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (RELOAD.consumeClick()) {
            if (mc.player != null && mc.player.getMainHandItem().getItem() instanceof FirearmItem) {
                ClientPacketDistributor.sendToServer(new ReloadWeaponPayload());
            }
        }
    }
}
