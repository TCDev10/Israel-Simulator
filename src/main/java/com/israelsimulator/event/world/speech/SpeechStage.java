package com.israelsimulator.event.world.speech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.SlabBlock;

/** Places the {@link SpeechStageLayout} in the world. */
public final class SpeechStage {
    private SpeechStage() {}

    public static void place(ServerLevel level, BlockPos ground) {
        for (SpeechStageLayout.Placement p : SpeechStageLayout.placements()) {
            BlockPos pos = ground.offset(p.dx(), p.dy(), p.dz());
            if (p.kind() == SpeechStageLayout.Kind.SUPPORT) {
                if (level.getBlockState(pos).canBeReplaced()) level.setBlock(pos, Blocks.SANDSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                continue;
            }
            BlockState state = stateFor(p.kind());
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            if (p.kind() == SpeechStageLayout.Kind.SIGN && level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
                SignText text = new SignText()
                        .setMessage(1, Component.translatable("sign.israel_simulator.public_speech.title"))
                        .setMessage(2, Component.translatable("sign.israel_simulator.public_speech.subtitle"));
                sign.setText(text, true);
                sign.setChanged();
            }
        }
    }

    static BlockState stateFor(SpeechStageLayout.Kind kind) {
        return switch (kind) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case FLOOR -> Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            case SUPPORT -> Blocks.SANDSTONE.defaultBlockState();
            case STAGE -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case STAGE_STEP -> Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            case POST -> Blocks.CONCRETE.white().defaultBlockState();
            case ROOF -> Blocks.DYED_TERRACOTTA.blue().defaultBlockState();
            case SPEAKER -> Blocks.JUKEBOX.defaultBlockState();
            case SPEAKER_TOP -> Blocks.NOTE_BLOCK.defaultBlockState();
            case MIC_STAND -> Blocks.IRON_BARS.defaultBlockState();
            case MIC -> Blocks.END_ROD.defaultBlockState();
            case CHAIR -> Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            case SIGN -> Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 0);
        };
    }
}
