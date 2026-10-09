package com.israelsimulator.mossad;

import com.israelsimulator.entity.mossad.MossadAgentEntity;
import com.israelsimulator.entity.mossad.MossadHandlerEntity;
import com.israelsimulator.entity.mossad.MossadInformantEntity;
import com.israelsimulator.item.combat.FirearmItem;
import com.israelsimulator.registry.ModEntities;
import com.israelsimulator.registry.ModItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Mission flow for the Mossad Handler: chat-click menu, accept/abandon, progress, rewards and reputation.
 */
public final class MossadMissions {
    public static final double HANDLER_RANGE = 8.0;
    public static final double ESCORT_ARRIVAL_DISTANCE = 6.0;

    private MossadMissions() {}

    private static MutableComponent prefix() {
        return Component.translatable("mossad.israel_simulator.prefix").withStyle(ChatFormatting.DARK_AQUA);
    }

    private static void say(ServerPlayer player, Component message) {
        player.sendSystemMessage(prefix().append(" ").append(message));
    }

    public static void interact(ServerPlayer player, MossadHandlerEntity handler) {
        MossadData data = MossadData.get(player.level().getServer());
        MossadData.Entry e = data.entry(player.getUUID());
        MossadMission active = e.activeMission();
        if (active == MossadMission.RETRIEVE && takeOne(player, ModItems.SEALED_DOSSIER.get())) {
            complete(player, data, e);
            return;
        }
        if (active != null) {
            status(player);
            return;
        }
        say(player, Component.translatable("mossad.israel_simulator.greeting", e.reputation).withStyle(ChatFormatting.GRAY));
        for (MossadMission m : MossadMission.values()) {
            String cmd = "/mossad accept " + m.id();
            MutableComponent line = Component.literal("  [").append(Component.translatable(m.translationKey()))
                    .append("]").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent.RunCommand(cmd))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable(m.translationKey() + ".desc"))));
            line.append(Component.literal(" ").append(Component.translatable(m.translationKey() + ".desc")).withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(line);
        }
    }

    public static boolean nearHandler(ServerPlayer player) {
        return !player.level().getEntitiesOfClass(MossadHandlerEntity.class,
                player.getBoundingBox().inflate(HANDLER_RANGE)).isEmpty();
    }

    public static int accept(ServerPlayer player, MossadMission mission) {
        ServerLevel level = (ServerLevel) player.level();
        MossadData data = MossadData.get(level.getServer());
        MossadData.Entry e = data.entry(player.getUUID());
        if (e.activeMission() != null) {
            say(player, Component.translatable("mossad.israel_simulator.already_active").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!nearHandler(player)) {
            say(player, Component.translatable("mossad.israel_simulator.need_handler").withStyle(ChatFormatting.RED));
            return 0;
        }
        e.clearMission();
        e.mission = mission.id();
        RandomSource random = level.getRandom();
        switch (mission) {
            case ELIMINATE -> {
                BlockPos at = surfaceAround(level, player.blockPosition(), 30, 45, random);
                e.target = at.asLong();
                spawnAgents(level, at, mission.goal(), random);
                say(player, Component.translatable("mossad.israel_simulator.eliminate.start", mission.goal(), at.getX(), at.getZ()));
            }
            case RETRIEVE -> say(player, Component.translatable("mossad.israel_simulator.retrieve.start"));
            case ESCORT -> {
                BlockPos dest = surfaceAround(level, player.blockPosition(), 120, 180, random);
                e.target = dest.asLong();
                MossadInformantEntity informant = ModEntities.MOSSAD_INFORMANT.get().create(level, EntitySpawnReason.EVENT);
                if (informant != null) {
                    informant.snapTo(player.getX() + 1, player.getY(), player.getZ() + 1, player.getYRot(), 0);
                    informant.setEscort(player.getUUID());
                    level.addFreshEntity(informant);
                    e.informant = informant.getUUID().toString();
                }
                spawnAgents(level, surfaceAround(level, dest, 6, 12, random), 2, random);
                say(player, Component.translatable("mossad.israel_simulator.escort.start", dest.getX(), dest.getY(), dest.getZ()));
            }
        }
        data.setDirty();
        return 1;
    }

    public static void abandon(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        MossadData data = MossadData.get(level.getServer());
        MossadData.Entry e = data.entry(player.getUUID());
        if (e.activeMission() == null) {
            say(player, Component.translatable("mossad.israel_simulator.no_mission"));
            return;
        }
        removeInformant(level, e);
        e.clearMission();
        data.adjustReputation(player.getUUID(), -5);
        say(player, Component.translatable("mossad.israel_simulator.abandoned").withStyle(ChatFormatting.YELLOW));
    }

    public static void status(ServerPlayer player) {
        MossadData.Entry e = MossadData.get(player.level().getServer()).entry(player.getUUID());
        MossadMission m = e.activeMission();
        if (m == null) {
            say(player, Component.translatable("mossad.israel_simulator.status.none", e.reputation, e.completed));
            return;
        }
        BlockPos t = BlockPos.of(e.target);
        say(player, Component.translatable("mossad.israel_simulator.status.active",
                Component.translatable(m.translationKey()), e.progress, m.goal(), t.getX(), t.getY(), t.getZ(), e.reputation));
    }

    /** Called when a player kills a hostile agent. */
    public static void onAgentKilled(ServerPlayer player, MossadAgentEntity agent) {
        MossadData data = MossadData.get(player.level().getServer());
        MossadData.Entry e = data.entry(player.getUUID());
        if (e.activeMission() == MossadMission.ELIMINATE) {
            e.progress++;
            data.setDirty();
            if (e.progress >= MossadMission.ELIMINATE.goal()) {
                complete(player, data, e);
            } else {
                player.sendOverlayMessage(Component.translatable("mossad.israel_simulator.eliminate.progress",
                        e.progress, MossadMission.ELIMINATE.goal()).withStyle(ChatFormatting.AQUA));
            }
        }
    }

    public static void onInformantLost(ServerLevel level, UUID playerId, UUID informant) {
        MossadData data = MossadData.get(level.getServer());
        MossadData.Entry e = data.entry(playerId);
        if (e.activeMission() == MossadMission.ESCORT && informant.toString().equals(e.informant)) {
            e.clearMission();
            data.adjustReputation(playerId, -10);
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                say(player, Component.translatable("mossad.israel_simulator.escort.failed").withStyle(ChatFormatting.RED));
            }
        }
    }

    /** Escort arrival check, once per second per player. */
    public static void tickPlayer(ServerPlayer player) {
        MossadData data = MossadData.get(player.level().getServer());
        MossadData.Entry e = data.entry(player.getUUID());
        if (e.activeMission() != MossadMission.ESCORT || e.informant.isEmpty()) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        Entity informant = level.getEntity(UUID.fromString(e.informant));
        BlockPos dest = BlockPos.of(e.target);
        if (player.tickCount % 40 == 0) {
            for (int i = 0; i < 6; i++) {
                level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER, true, false,
                        dest.getX() + 0.5, dest.getY() + 1 + i * 0.6, dest.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0);
            }
        }
        if (informant != null && informant.isAlive()
                && informant.distanceToSqr(dest.getX() + 0.5, informant.getY(), dest.getZ() + 0.5)
                <= ESCORT_ARRIVAL_DISTANCE * ESCORT_ARRIVAL_DISTANCE) {
            informant.discard();
            complete(player, data, e);
        }
    }

    private static void complete(ServerPlayer player, MossadData data, MossadData.Entry e) {
        MossadMission m = e.activeMission();
        if (m == null) {
            return;
        }
        boolean first = (e.doneMask & (1 << m.ordinal())) == 0;
        e.doneMask |= 1 << m.ordinal();
        e.completed++;
        e.clearMission();
        int rep = data.adjustReputation(player.getUUID(), m.reputationReward());
        for (ItemStack stack : rewards(m, first)) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        data.setDirty();
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.2F);
        say(player, Component.translatable("mossad.israel_simulator.completed", Component.translatable(m.translationKey()),
                m.reputationReward(), rep).withStyle(ChatFormatting.GREEN));
    }

    /** Rewards: shekels and ammo every time, the mission's weapon the first time. */
    public static List<ItemStack> rewards(MossadMission m, boolean first) {
        ItemStack shekels = new ItemStack(ModItems.SHEKEL.get(), m.shekelReward());
        return switch (m) {
            case ELIMINATE -> first
                    ? List.of(shekels, new ItemStack(ModItems.SMG_AMMO.get(), 32), loaded(ModItems.SMG.get(), 32))
                    : List.of(shekels, new ItemStack(ModItems.SMG_AMMO.get(), 32));
            case RETRIEVE -> List.of(shekels, new ItemStack(ModItems.FRAG_GRENADE.get(), 4), new ItemStack(ModItems.SNIPER_AMMO.get(), 8));
            case ESCORT -> first
                    ? List.of(shekels, new ItemStack(ModItems.RIFLE_AMMO.get(), 30), loaded(ModItems.ASSAULT_RIFLE.get(), 30))
                    : List.of(shekels, new ItemStack(ModItems.RIFLE_AMMO.get(), 30));
        };
    }

    private static ItemStack loaded(Item gun, int rounds) {
        ItemStack s = new ItemStack(gun);
        FirearmItem.setLoadedRounds(s, rounds);
        return s;
    }

    private static boolean takeOne(ServerPlayer player, Item item) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) {
                inv.getItem(i).shrink(1);
                return true;
            }
        }
        return false;
    }

    private static void removeInformant(ServerLevel level, MossadData.Entry e) {
        if (!e.informant.isEmpty()) {
            Entity informant = level.getEntity(UUID.fromString(e.informant));
            if (informant != null) {
                informant.discard();
            }
        }
    }

    public static BlockPos surfaceAround(ServerLevel level, BlockPos center, int min, int max, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        int dist = Mth.nextInt(random, min, max);
        int x = center.getX() + (int) Math.round(Math.cos(angle) * dist);
        int z = center.getZ() + (int) Math.round(Math.sin(angle) * dist);
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
    }

    public static int spawnAgents(ServerLevel level, BlockPos at, int count, RandomSource random) {
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            BlockPos p = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    at.offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3));
            MossadAgentEntity agent = ModEntities.MOSSAD_AGENT.get().create(level, EntitySpawnReason.EVENT);
            if (agent == null) {
                continue;
            }
            agent.snapTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, random.nextFloat() * 360F, 0);
            agent.finalizeSpawn(level, level.getCurrentDifficultyAt(p), EntitySpawnReason.EVENT, null);
            agent.setPersistenceRequired();
            level.addFreshEntity(agent);
            spawned++;
        }
        return spawned;
    }
}
