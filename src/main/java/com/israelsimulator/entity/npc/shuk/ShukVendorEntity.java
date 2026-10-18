package com.israelsimulator.entity.npc.shuk;

import com.israelsimulator.festival.FestivalType;
import com.israelsimulator.registry.ModItems;
import com.israelsimulator.block.MenorahBlock;
import com.israelsimulator.festival.FestivalManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Shuk (market) vendor standing behind a stall of the Grand Market. Sells for Shekels; the goods
 * and portions change with the time of day and the festival ({@link ShukMarket}), and the stall
 * is closed at night and on Shabbat. Stays at the stall (no wandering).
 */
public class ShukVendorEntity extends AbstractVillager {

    private ShukMarket.Stall stall = ShukMarket.Stall.FRUIT;
    private String stockKey = "";
    /** Festival decorations this vendor put on the counter, and for which festival ("" = none). */
    private final List<BlockPos> decor = new ArrayList<>();
    private String decorFor = "";
    private boolean decorDone;

    public ShukVendorEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public ShukMarket.Stall getStall() {
        return stall;
    }

    public void setStall(ShukMarket.Stall stall) {
        this.stall = stall;
        this.stockKey = "";
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    private ShukMarket.Period period() {
        return ShukMarket.periodAt(this.level().getOverworldClockTime(), this.level().getGameTime());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.isAlive() || this.isTrading() || this.isBaby()) {
            return super.mobInteract(player, hand);
        }
        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
        }
        if (!this.level().isClientSide()) {
            ShukMarket.Period period = period();
            if (period == ShukMarket.Period.CLOSED) {
                FestivalType f = com.israelsimulator.festival.FestivalManager.getCurrentFestival(this.level().getGameTime());
                String key = f == FestivalType.SHABBAT ? "message.israel_simulator.shuk.closed_shabbat"
                        : f == FestivalType.YOM_KIPPUR ? "message.israel_simulator.shuk.closed_yom_kippur"
                        : "message.israel_simulator.shuk.closed_night";
                player.sendSystemMessage(Component.translatable(key)
                        .withStyle(ChatFormatting.YELLOW));
                return InteractionResult.CONSUME;
            }
            refreshStock();
            if (this.getOffers().isEmpty()) {
                return InteractionResult.CONSUME;
            }
            String periodKey = switch (period) {
                case MORNING -> "message.israel_simulator.shuk.period.morning";
                case CLOSING -> "message.israel_simulator.shuk.period.closing";
                default -> "message.israel_simulator.shuk.period.afternoon";
            };
            player.sendSystemMessage(Component.translatable(periodKey).withStyle(ChatFormatting.GOLD));
            String stallKey = switch (stall) {
                case SPICE -> "entity.israel_simulator.shuk_vendor.stall.spice";
                case FRUIT -> "entity.israel_simulator.shuk_vendor.stall.fruit";
                case BAKERY -> "entity.israel_simulator.shuk_vendor.stall.bakery";
                case FISH -> "entity.israel_simulator.shuk_vendor.stall.fish";
                case HALVA -> "entity.israel_simulator.shuk_vendor.stall.halva";
            };
            this.setTradingPlayer(player);
            this.openTradingScreen(player, Component.translatable(stallKey), 1);
        }
        return InteractionResult.SUCCESS;
    }

    /** Rebuilds the offers when the market period or the festival changed. */
    private void refreshStock() {
        ShukMarket.Period period = period();
        FestivalType festival = ShukMarket.stockFestival(this.level().getGameTime());
        String key = stall.name() + ":" + ShukMarket.stockKey(period, festival);
        if (key.equals(this.stockKey) && !this.getOffers().isEmpty()) return;
        this.stockKey = key;
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, festiveItem(festival, period));
        MerchantOffers offers = this.getOffers();
        offers.clear();
        for (ShukMarket.Listing l : ShukMarket.listings(stall, period, festival)) {
            java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item>> item =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.Identifier.parse(l.item()));
            if (item.isEmpty()) continue;
            offers.add(new MerchantOffer(new ItemCost(ModItems.SHEKEL.get(), l.shekels()),
                    new ItemStack(item.get(), l.count()), 16, 1, 0.05F));
        }
    }

    /** What the vendor holds up: the festival food, nothing when the stall is closed. */
    private static ItemStack festiveItem(FestivalType festival, ShukMarket.Period period) {
        if (festival == null || period == ShukMarket.Period.CLOSED) return ItemStack.EMPTY;
        return switch (festival) {
            case HANUKKAH -> new ItemStack(ModItems.SUFGANIYAH.get());
            case PURIM -> new ItemStack(ModItems.HAMANTASH.get());
            case PESACH -> new ItemStack(ModItems.MATZO.get());
            case ROSH_HASHANAH -> new ItemStack(net.minecraft.world.item.Items.HONEY_BOTTLE);
            case SUKKOT -> new ItemStack(ModItems.CITRUS.get());
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        refreshStock();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.tickCount % 100 == 0 && !this.isTrading()) {
            refreshStock();
        }
        if (this.level() instanceof ServerLevel sl && this.tickCount % 100 == 50) {
            updateDecorations(sl);
        }
    }

    /**
     * Puts the festival decoration on the counter when a festival starts (once per festival, so
     * breaking one does not make it come back) and takes the decorations away when it ends.
     */
    private void updateDecorations(ServerLevel level) {
        long t = level.getGameTime();
        FestivalType festival = FestivalManager.getCurrentFestival(t);
        ShukDecorations.Decoration d = ShukDecorations.forFestival(festival, FestivalManager.getHanukkahNight(t));
        String key = d == null ? "" : festival.name();
        if (!key.equals(decorFor)) {
            clearDecorations(level);
            decorFor = key;
            decorDone = false;
        }
        if (d == null) return;
        BlockState state = decorState(d);
        Block block = state.getBlock();
        decor.removeIf(p -> level.isLoaded(p) && !level.getBlockState(p).is(block));
        for (BlockPos p : decor) {
            if (level.isLoaded(p) && level.getBlockState(p) != state) level.setBlock(p, state, Block.UPDATE_ALL);
        }
        if (decorDone) return;
        BlockPos home = this.blockPosition();
        for (int[] o : ShukDecorations.candidateOffsets()) {
            if (decor.size() >= ShukDecorations.MAX_PER_VENDOR) break;
            BlockPos p = home.offset(o[0], o[1], o[2]);
            BlockPos below = p.below();
            if (!level.isLoaded(p) || !level.isEmptyBlock(p)) continue;
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            if (!state.canSurvive(level, p)) continue;
            level.setBlock(p, state, Block.UPDATE_ALL);
            decor.add(p.immutable());
        }
        decorDone = true;
    }

    private static BlockState decorState(ShukDecorations.Decoration d) {
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(d.blockId()));
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(MenorahBlock.CANDLES)) state = state.setValue(MenorahBlock.CANDLES, d.candles());
        if (d.candles() > 0 && state.hasProperty(BlockStateProperties.CANDLES)) {
            state = state.setValue(BlockStateProperties.CANDLES, Math.min(4, d.candles()));
        }
        if (state.hasProperty(BlockStateProperties.LIT)) state = state.setValue(BlockStateProperties.LIT, d.lit());
        return state;
    }

    /** Removes this vendor's decorations that are still in place (no drops). */
    private void clearDecorations(ServerLevel level) {
        if (!decorFor.isEmpty()) {
            ShukDecorations.Decoration old = ShukDecorations.forFestival(FestivalType.valueOf(decorFor), 1);
            if (old != null) {
                Block block = decorState(old).getBlock();
                for (BlockPos p : decor) {
                    if (level.isLoaded(p) && level.getBlockState(p).is(block)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        decor.clear();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason.shouldDestroy() && this.level() instanceof ServerLevel sl) {
            clearDecorations(sl);
        }
        super.remove(reason);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {}

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Stall", stall.name().toLowerCase(Locale.ROOT));
        output.store("Decor", BlockPos.CODEC.listOf(), List.copyOf(decor));
        output.putString("DecorFor", decorFor);
        output.putBoolean("DecorDone", decorDone);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String s = input.getStringOr("Stall", "fruit").toUpperCase(Locale.ROOT);
        try {
            this.stall = ShukMarket.Stall.valueOf(s);
        } catch (IllegalArgumentException e) {
            this.stall = ShukMarket.Stall.FRUIT;
        }
        this.stockKey = "";
        decor.clear();
        decor.addAll(input.read("Decor", BlockPos.CODEC.listOf()).orElse(List.of()));
        String f = input.getStringOr("DecorFor", "");
        try {
            if (!f.isEmpty()) FestivalType.valueOf(f);
            decorFor = f;
        } catch (IllegalArgumentException e) {
            decorFor = "";
        }
        decorDone = input.getBooleanOr("DecorDone", false);
    }
}
