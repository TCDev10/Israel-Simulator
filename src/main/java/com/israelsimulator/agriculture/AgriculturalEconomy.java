package com.israelsimulator.agriculture;

import com.israelsimulator.registry.ModItems;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/**
 * Economic calculation service for agricultural trades and rural markets.
 */
public final class AgriculturalEconomy {
    public record TradeResult(int itemsConsumed, int shekelsEarned, int agorotEarned, String commodityName) {}

    private AgriculturalEconomy() {}

    /**
     * Calculates the best exchange for the offered stack.
     */
    public static Optional<TradeResult> calculateTrade(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();

        return AgriculturalCommodity.fromStack(stack).map(commodity -> {
            int available = stack.getCount();
            int requiredPerShekel = commodity.itemsPerShekel();
            if (available < requiredPerShekel) {
                // Not enough for a full Shekel, pay in Agorot (1 Shekel = 100 Agorot)
                int agorot = (available * 100) / requiredPerShekel;
                if (agorot <= 0) return null;
                return new TradeResult(available, 0, agorot, commodity.displayName());
            }

            int shekels = available / requiredPerShekel;
            int consumed = shekels * requiredPerShekel;
            return new TradeResult(consumed, shekels, 0, commodity.displayName());
        });
    }

    /**
     * Generates item stacks representing the payment in Shekels and Agorot.
     */
    public static ItemStack createShekelPayout(int count) {
        if (count <= 0) return ItemStack.EMPTY;
        return new ItemStack(ModItems.SHEKEL.get(), count);
    }

    public static ItemStack createAgoraPayout(int count) {
        if (count <= 0) return ItemStack.EMPTY;
        return new ItemStack(ModItems.AGORA.get(), count);
    }
}

