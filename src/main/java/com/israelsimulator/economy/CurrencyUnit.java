package com.israelsimulator.economy;

/**
 * Currency definitions for Israel-Simulator (GAME_DESIGN.md §27, §29, TODO §29).
 * 1 Shekel (ILS) = 100 Agorot.
 */
public enum CurrencyUnit {
    AGORA(1, "agora", "item.israel_simulator.agora"),
    SHEKEL(100, "shekel", "item.israel_simulator.shekel");

    private final int valueInAgorot;
    private final String id;
    private final String translationKey;

    CurrencyUnit(int valueInAgorot, String id, String translationKey) {
        this.valueInAgorot = valueInAgorot;
        this.id = id;
        this.translationKey = translationKey;
    }

    public int getValueInAgorot() {
        return valueInAgorot;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static long toAgorot(long shekels, long agorot) {
        return (shekels * 100L) + agorot;
    }

    public static long toShekels(long agorot) {
        return agorot / 100L;
    }

    public static long remainderAgorot(long totalAgorot) {
        return totalAgorot % 100L;
    }

    public static String format(long totalAgorot) {
        long shekels = totalAgorot / 100L;
        long agorot = Math.abs(totalAgorot % 100L);
        return String.format("%d.%02d ILS", shekels, agorot);
    }
}
