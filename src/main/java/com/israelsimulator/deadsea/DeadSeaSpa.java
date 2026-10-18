package com.israelsimulator.deadsea;

import com.israelsimulator.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dead Sea mud and salt in the world (roadmap R4; floating is {@link DeadSeaMechanics}).
 * <ul>
 *   <li>Mud baths: standing in mud (the resort's mud baths and spa fountain) in the Dead Sea biome
 *       for {@link #SOAK_SECONDS} s gives the mud treatment, once every {@link #BATH_COOLDOWN_TICKS}.</li>
 *   <li>Mud harvesting: a bowl used on mud, or on sand/clay next to Dead Sea water, gives
 *       Dead Sea Mud.</li>
 *   <li>Salt water: floating with the head above the water for {@link #FLOAT_SECONDS} s heals
 *       (Regeneration); salt in the eyes still stings ({@link DeadSeaMechanics}).</li>
 * </ul>
 */
public final class DeadSeaSpa {
    public static final int SOAK_SECONDS = 5;
    public static final int FLOAT_SECONDS = 10;
    public static final int BATH_COOLDOWN_TICKS = 2400;

    private static final Map<UUID, Integer> soak = new HashMap<>();
    private static final Map<UUID, Integer> floating = new HashMap<>();
    private static final Map<UUID, Long> lastBath = new HashMap<>();

    private DeadSeaSpa() {}

    public static void reset() {
        soak.clear();
        floating.clear();
        lastBath.clear();
    }

    /** Called once a second per player. */
    public static void tickPlayer(ServerPlayer player) {
        ServerLevel level = player.level();
        UUID id = player.getUUID();
        if (!DeadSeaMechanics.isDeadSeaBiome(level, player.blockPosition())) {
            soak.remove(id);
            floating.remove(id);
            return;
        }
        if (inMud(level, player)) {
            int s = soak.merge(id, 1, Integer::sum);
            long now = level.getGameTime();
            Long last = lastBath.get(id);
            boolean ready = last == null || now - last >= BATH_COOLDOWN_TICKS;
            if (s == 1 && ready) {
                player.sendSystemMessage(Component.translatable("message.israel_simulator.dead_sea.mud_bath_start")
                        .withStyle(ChatFormatting.GRAY));
            }
            if (s >= SOAK_SECONDS && ready) {
                lastBath.put(id, now);
                soak.put(id, 0);
                treat(player);
                player.sendSystemMessage(Component.translatable("message.israel_simulator.dead_sea.mud_bath_done")
                        .withStyle(ChatFormatting.GOLD));
            }
        } else {
            soak.remove(id);
        }
        // fluid checks by position: the server's in-water flags lag behind client-driven movement
        boolean feetWet = level.getFluidState(player.blockPosition()).is(FluidTags.WATER);
        boolean eyesWet = level.getFluidState(BlockPos.containing(player.getEyePosition())).is(FluidTags.WATER);
        if (feetWet && !eyesWet) {
            int f = floating.merge(id, 1, Integer::sum);
            if (f % FLOAT_SECONDS == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, true, true));
                if (f == FLOAT_SECONDS) {
                    player.sendSystemMessage(Component.translatable("message.israel_simulator.dead_sea.salt_float")
                            .withStyle(ChatFormatting.AQUA));
                }
            }
        } else {
            floating.remove(id);
        }
    }

    private static boolean inMud(Level level, Player player) {
        BlockPos feet = player.blockPosition();
        return level.getBlockState(feet).is(Blocks.MUD) || level.getBlockState(feet.below()).is(Blocks.MUD);
    }

    /** Same treatment as applying Dead Sea Mud. */
    public static void treat(Player player) {
        player.removeEffect(MobEffects.SLOWNESS);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.POISON);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, true, true));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.MUD_STEP, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.3, 0.3, 0.3, 0.05);
        }
    }

    /** True when a bowl may scoop Dead Sea Mud from this block. */
    public static boolean canScoop(Level level, BlockPos pos) {
        if (!DeadSeaMechanics.isDeadSeaBiome(level, pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.MUD)) return true;
        if (!(state.is(Blocks.SAND) || state.is(Blocks.CLAY))) return false;
        for (Direction d : Direction.values()) {
            if (d != Direction.DOWN && level.getFluidState(pos.relative(d)).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    /** Bowl on mud / wet shore: one Dead Sea Mud. */
    public static boolean tryScoop(Player player, InteractionHand hand, BlockPos pos) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.BOWL) || !canScoop(player.level(), pos)) return false;
        if (!player.level().isClientSide()) {
            held.consume(1, player);
            ItemStack mud = new ItemStack(ModItems.DEAD_SEA_MUD.get());
            if (!player.getInventory().add(mud)) player.drop(mud, false);
            player.level().playSound(null, pos, SoundEvents.MUD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }
}
