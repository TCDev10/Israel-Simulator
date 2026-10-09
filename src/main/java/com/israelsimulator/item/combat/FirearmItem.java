package com.israelsimulator.item.combat;

import com.israelsimulator.config.IsraelSimulatorConfig;
import com.israelsimulator.registry.ModDataComponents;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Magazine-fed hitscan firearm (assault rifle, SMG, sniper rifle).
 *
 * <ul>
 *   <li>Use fires one round; holding use keeps firing at the weapon's fire interval.</li>
 *   <li>Sneak + use, the reload key (R by default), or use with an empty magazine reloads from the
 *       matching ammo in the inventory; the weapon is then on cooldown for the reload time.</li>
 *   <li>The sniper rifle is aimed instead: hold use to look through the scope (spyglass zoom),
 *       release to fire. Aimed for at least {@value FirearmStats#SCOPE_STEADY_TICKS} ticks it has no spread.</li>
 * </ul>
 * Creative players reload without spending ammo.
 */
public class FirearmItem extends Item {
    private final FirearmStats stats;
    private final Supplier<? extends Item> ammo;
    private final boolean scoped;

    public FirearmItem(Properties properties, FirearmStats stats, Supplier<? extends Item> ammo, boolean scoped) {
        super(properties.stacksTo(1).durability(stats.durability()).enchantable(1));
        this.stats = stats;
        this.ammo = ammo;
        this.scoped = scoped;
    }

    public FirearmStats stats() {
        return stats;
    }

    public Item ammoItem() {
        return ammo.get();
    }

    public boolean isScoped() {
        return scoped;
    }

    public static int loadedRounds(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MAGAZINE.get(), 0);
    }

    public static void setLoadedRounds(ItemStack stack, int rounds) {
        stack.set(ModDataComponents.MAGAZINE.get(), Math.max(0, rounds));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && loadedRounds(held) < stats.magazineSize()) {
            if (level instanceof ServerLevel serverLevel) {
                reload(serverLevel, player, held);
            }
            return InteractionResult.CONSUME;
        }
        if (loadedRounds(held) <= 0) {
            if (level instanceof ServerLevel serverLevel && !reload(serverLevel, player, held)) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6F, 1.6F);
                player.getCooldowns().addCooldown(held, 10);
            }
            return InteractionResult.CONSUME;
        }
        if (scoped) {
            player.startUsingItem(hand);
            if (level.isClientSide()) {
                player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
            }
            return InteractionResult.CONSUME;
        }
        if (level instanceof ServerLevel serverLevel) {
            shoot(serverLevel, player, held, hand, stats.spreadDegrees());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (!scoped) {
            return false;
        }
        entity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
        int aimed = this.getUseDuration(stack, entity) - remainingTime;
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player && loadedRounds(stack) > 0) {
            float spread = aimed >= FirearmStats.SCOPE_STEADY_TICKS ? stats.scopedSpreadDegrees() : stats.spreadDegrees();
            InteractionHand hand = player.getUsedItemHand();
            shoot(serverLevel, player, stack, hand, spread);
        }
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return scoped ? 72000 : 0;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return scoped ? ItemUseAnimation.SPYGLASS : ItemUseAnimation.NONE;
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return scoped && ItemAbilities.DEFAULT_SPYGLASS_ACTIONS.contains(itemAbility);
    }

    private void shoot(ServerLevel level, Player player, ItemStack held, InteractionHand hand, float spread) {
        int rounds = loadedRounds(held);
        if (rounds <= 0) {
            return;
        }
        setLoadedRounds(held, rounds - 1);
        player.getCooldowns().addCooldown(held, stats.fireIntervalTicks());
        Vec3 dir = Ballistics.applySpread(player.getViewVector(1.0F), spread, player.getRandom());
        float damage = (float) (stats.damage() * IsraelSimulatorConfig.weaponDamageMultiplier());
        Ballistics.fire(level, player, dir, stats.range(), damage);
        playShot(level, player, stats);
        held.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.sendOverlayMessage(Component.translatable("message.israel_simulator.ammo_count",
                rounds - 1, stats.magazineSize()).withStyle(rounds - 1 == 0 ? ChatFormatting.RED : ChatFormatting.GRAY));
    }

    /** Vanilla sounds layered per weapon class. */
    public static void playShot(ServerLevel level, LivingEntity shooter, FirearmStats stats) {
        double x = shooter.getX(), y = shooter.getY(), z = shooter.getZ();
        SoundSource source = shooter instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        if (stats == FirearmStats.SNIPER_RIFLE) {
            level.playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, source, 2.5F, 0.7F);
            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, source, 0.6F, 1.6F);
        } else if (stats == FirearmStats.SMG) {
            level.playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_BLAST, source, 1.2F, 2.0F);
        } else {
            level.playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_BLAST, source, 1.6F, 1.5F);
            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, source, 0.25F, 2.0F);
        }
    }

    /**
     * Fills the magazine from the inventory. Returns false if nothing could be loaded.
     */
    public boolean reload(ServerLevel level, Player player, ItemStack held) {
        int loaded = loadedRounds(held);
        if (loaded >= stats.magazineSize() || player.getCooldowns().isOnCooldown(held)) {
            return false;
        }
        boolean creative = player.getAbilities().instabuild;
        int available = creative ? Integer.MAX_VALUE : countAmmo(player.getInventory());
        int toLoad = FirearmStats.roundsToLoad(loaded, stats.magazineSize(), available);
        if (toLoad <= 0) {
            player.sendOverlayMessage(Component.translatable("message.israel_simulator.no_ammo",
                    ammoItem().getDefaultInstance().getHoverName()).withStyle(ChatFormatting.RED));
            return false;
        }
        if (!creative) {
            removeAmmo(player.getInventory(), toLoad);
        }
        setLoadedRounds(held, loaded + toLoad);
        player.getCooldowns().addCooldown(held, stats.reloadTicks());
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
        player.sendOverlayMessage(Component.translatable("message.israel_simulator.reloading",
                loaded + toLoad, stats.magazineSize()).withStyle(ChatFormatting.YELLOW));
        return true;
    }

    private int countAmmo(Inventory inv) {
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ammoItem())) {
                total += s.getCount();
            }
        }
        return total;
    }

    private void removeAmmo(Inventory inv, int amount) {
        for (int i = 0; i < inv.getContainerSize() && amount > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ammoItem())) {
                int take = Math.min(amount, s.getCount());
                s.shrink(take);
                amount -= take;
            }
        }
        inv.setChanged();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.israel_simulator." + stats.id() + ".desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.israel_simulator.magazine", loadedRounds(stack), stats.magazineSize())
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.translatable("tooltip.israel_simulator.weapon_stats",
                String.format(java.util.Locale.ROOT, "%.1f", stats.damage()), stats.shotsPerMinute(), (int) stats.range())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable("tooltip.israel_simulator.uses_ammo", ammoItem().getDefaultInstance().getHoverName())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.translatable(scoped ? "tooltip.israel_simulator.weapon_controls_scoped"
                : "tooltip.israel_simulator.weapon_controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}
