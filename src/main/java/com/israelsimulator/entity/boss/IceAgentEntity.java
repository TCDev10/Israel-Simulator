package com.israelsimulator.entity.boss;

import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Elite federal enforcement agent summoned by Donald Trump miniboss (GAME_DESIGN.md §41–44).
 * Fully equipped in Netherite armor and wielding Netherite swords.
 */
public class IceAgentEntity extends Monster {

    private UUID trumpMinibossUuid;
    private int lifeTicksRemaining = 6000;

    public IceAgentEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public void setTrumpMinibossUuid(UUID uuid) {
        this.trumpMinibossUuid = uuid;
    }

    public UUID getTrumpMinibossUuid() {
        return this.trumpMinibossUuid;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            // Ensure full Netherite equipment is equipped
            if (this.getMainHandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
                this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
                this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));

                // Balanced multiplayer drop rates
                this.setDropChance(EquipmentSlot.HEAD, 0.05F);
                this.setDropChance(EquipmentSlot.CHEST, 0.05F);
                this.setDropChance(EquipmentSlot.LEGS, 0.05F);
                this.setDropChance(EquipmentSlot.FEET, 0.05F);
                this.setDropChance(EquipmentSlot.MAINHAND, 0.08F);
            }

            lifeTicksRemaining--;
            if (lifeTicksRemaining <= 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            this.getX(), this.getY() + 0.5, this.getZ(),
                            10, 0.2, 0.2, 0.2, 0.05);
                }
                this.discard();
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("LifeTicksRemaining", this.lifeTicksRemaining);
        if (this.trumpMinibossUuid != null) {
            output.putString("TrumpUUID", this.trumpMinibossUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifeTicksRemaining = input.getIntOr("LifeTicksRemaining", 6000);
        String uuidStr = input.getStringOr("TrumpUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.trumpMinibossUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}

