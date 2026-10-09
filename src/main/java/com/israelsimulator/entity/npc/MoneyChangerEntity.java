package com.israelsimulator.entity.npc;

import com.israelsimulator.IsraelSimulator;
import com.israelsimulator.easteregg.EasterEggManager;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Money Changer: a market NPC that exchanges currency (Shekel, Agora, Ancient Coin)
 * for precious valuables, rare minerals and cultural goods.
 *
 * <p>Trades are fully data-driven via the {@code israel_simulator:money_changer} trade set
 * (data/israel_simulator/trade_set/money_changer.json referencing the
 * {@code israel_simulator:money_changer} villager trade tag). Vanilla villagers are
 * never intercepted; currency exchange lives exclusively here.
 *
 * <p>Server-authoritative per AGENTS.md §8: the merchant menu, uses and daily restock
 * are all handled by vanilla server logic, so multiplayer sync and duplication safety
 * come for free. Restock happens once per game day ({@link #restockIfNewDay()}).
 */
public class MoneyChangerEntity extends AbstractVillager {
    /** Trade set shown in the trading GUI: {@value #TRADE_SET_ID}. */
    public static final String TRADE_SET_ID = "money_changer";
    public static final ResourceKey<TradeSet> TRADES = ResourceKey.create(
            Registries.TRADE_SET, Identifier.fromNamespaceAndPath(IsraelSimulator.MOD_ID, TRADE_SET_ID));

    /** Saved day (gameTime / 24000) of the last restock; -1 until the first restock check. */
    private long lastRestockDay = -1L;

    public MoneyChangerEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
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

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(net.minecraft.world.item.Items.VILLAGER_SPAWN_EGG) || !this.isAlive() || this.isTrading() || this.isBaby()) {
            return super.mobInteract(player, hand);
        }

        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
            if (player instanceof ServerPlayer serverPlayer) {
                // Market flavour line, server-side, with the shared easter-egg cooldown.
                EasterEggManager.triggerTraderEasterEgg(serverPlayer, this);
            }
        }

        if (!this.level().isClientSide()) {
            if (this.getOffers().isEmpty()) {
                return InteractionResult.CONSUME;
            }
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        this.addOffersFromTradeSet(level, this.getOffers(), TRADES);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int xp = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), xp));
        }
    }

    /**
     * Restocks every offer once per game day. Runs server-side only; the check is a single
     * long comparison per tick, so there is no expensive scan (AGENTS.md §19).
     */
    private void restockIfNewDay() {
        long day = this.level().getGameTime() / 24000L;
        if (this.lastRestockDay == -1L) {
            this.lastRestockDay = day;
            return;
        }
        if (day > this.lastRestockDay) {
            this.lastRestockDay = day;
            for (MerchantOffer offer : this.getOffers()) {
                offer.resetUses();
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            this.restockIfNewDay();
        }
    }

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
        output.putLong("LastRestockDay", this.lastRestockDay);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lastRestockDay = input.getLongOr("LastRestockDay", -1L);
    }
}
