package com.israelsimulator.network;

import com.israelsimulator.IsraelSimulator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Syncs whether a player is mid-prayer (and the wall block) to tracking clients.
 * {@code praying=false} clears the pose.
 */
public record WesternWallPrayingPayload(int entityId, boolean praying, BlockPos wallPos)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WesternWallPrayingPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, "western_wall_praying"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WesternWallPrayingPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, WesternWallPrayingPayload::entityId,
                    ByteBufCodecs.BOOL, WesternWallPrayingPayload::praying,
                    BlockPos.STREAM_CODEC, WesternWallPrayingPayload::wallPos,
                    WesternWallPrayingPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
