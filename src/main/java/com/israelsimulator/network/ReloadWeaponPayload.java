package com.israelsimulator.network;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.item.combat.FirearmItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: the player pressed the reload key. The server reloads the firearm in the main hand.
 */
public record ReloadWeaponPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReloadWeaponPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "reload_weapon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadWeaponPayload> STREAM_CODEC =
            StreamCodec.unit(new ReloadWeaponPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReloadWeaponPayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof FirearmItem gun && player.level() instanceof ServerLevel level) {
            gun.reload(level, player, held);
        }
    }
}
