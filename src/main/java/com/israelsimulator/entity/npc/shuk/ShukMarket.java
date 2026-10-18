package com.israelsimulator.entity.npc.shuk;

import com.israelsimulator.festival.FestivalManager;
import com.israelsimulator.festival.FestivalType;
import java.util.ArrayList;
import java.util.List;

/**
 * What a Shuk stall sells and for how much, depending on the time of day and the festival.
 *
 * <p>Pure logic (no level access), so it is unit-tested and the vendor just asks for the
 * current {@link Listing}s. Periods follow the day time: the market opens at dawn with fresh
 * produce, runs normally in the afternoon, sells off at closing time and is shut at night and on
 * Shabbat. Festivals add their foods (and on Pesach the bakery sells matzo, no leavened bread).</p>
 */
public final class ShukMarket {

    public enum Stall { SPICE, FRUIT, BAKERY, FISH, HALVA }

    public enum Period {
        /** 06:00–12:00 (day time 0–5999): fresh produce, bigger portions at fruit and fish stalls. */
        MORNING,
        /** 12:00–17:00 (6000–10999): normal prices. */
        AFTERNOON,
        /** 17:00–18:30 (11000–12499): end-of-day sell-off, larger portions everywhere. */
        CLOSING,
        /** Night (12500–23999) and Shabbat: no trading. */
        CLOSED
    }

    /** One offer: pay {@code shekels} Shekels, get {@code count} of the item (registry id). */
    public record Listing(String item, int count, int shekels) {}

    private ShukMarket() {}

    public static Period periodAt(long dayTime, long gameTime) {
        FestivalType f = FestivalManager.getCurrentFestival(gameTime);
        if (f == FestivalType.SHABBAT || f == FestivalType.YOM_KIPPUR) return Period.CLOSED;
        long t = Math.floorMod(dayTime, 24000L);
        if (t < 6000L) return Period.MORNING;
        if (t < 11000L) return Period.AFTERNOON;
        if (t < 12500L) return Period.CLOSING;
        return Period.CLOSED;
    }

    /** The festival that changes the stock, or null (Shabbat closes the market instead). */
    public static FestivalType stockFestival(long gameTime) {
        FestivalType f = FestivalManager.getCurrentFestival(gameTime);
        return f == FestivalType.SHABBAT || f == FestivalType.YOM_KIPPUR ? null : f;
    }

    public static List<Listing> listings(Stall stall, Period period, FestivalType festival) {
        List<Listing> out = new ArrayList<>();
        if (period == Period.CLOSED) return out;
        boolean pesach = festival == FestivalType.PESACH;
        switch (stall) {
            case SPICE -> {
                out.add(new Listing("israel_simulator:tahini", 1, 2));
                out.add(new Listing("israel_simulator:sesame_seeds", 4, 1));
                out.add(new Listing("israel_simulator:chickpeas", 4, 1));
                out.add(new Listing("israel_simulator:hummus", 1, 3));
            }
            case FRUIT -> {
                out.add(new Listing("israel_simulator:dates", 3, 1));
                out.add(new Listing("israel_simulator:citrus", 2, 1));
                out.add(new Listing("israel_simulator:olives", 3, 1));
                out.add(new Listing("israel_simulator:grapes", 3, 1));
                out.add(new Listing("israel_simulator:tomato", 3, 1));
                out.add(new Listing("israel_simulator:eggplant", 2, 1));
            }
            case BAKERY -> {
                if (pesach) {
                    out.add(new Listing("israel_simulator:matzo", 3, 2));
                } else {
                    out.add(new Listing("israel_simulator:challah", 1, 3));
                    out.add(new Listing("israel_simulator:rugelach", 2, 2));
                    out.add(new Listing("minecraft:bread", 2, 1));
                }
            }
            case FISH -> {
                out.add(new Listing("minecraft:cod", 2, 2));
                out.add(new Listing("minecraft:salmon", 1, 2));
                out.add(new Listing("minecraft:dried_kelp", 6, 1));
            }
            case HALVA -> {
                out.add(new Listing("minecraft:honey_bottle", 1, 3));
                out.add(new Listing("israel_simulator:sesame_seeds", 6, 1));
                if (!pesach) out.add(new Listing("israel_simulator:rugelach", 2, 2));
            }
        }
        // festival stock
        if (festival == FestivalType.HANUKKAH && (stall == Stall.BAKERY || stall == Stall.HALVA)) {
            out.add(0, new Listing("israel_simulator:sufganiyah", 2, 3));
        } else if (festival == FestivalType.PURIM && (stall == Stall.BAKERY || stall == Stall.HALVA)) {
            out.add(0, new Listing("israel_simulator:hamantash", 3, 2));
        } else if (festival == FestivalType.PESACH && stall == Stall.SPICE) {
            out.add(0, new Listing("israel_simulator:matzo", 2, 2));
        } else if (festival == FestivalType.ROSH_HASHANAH && (stall == Stall.FRUIT || stall == Stall.HALVA)) {
            out.add(0, new Listing("minecraft:honey_bottle", 1, 2));
            out.add(1, new Listing("minecraft:apple", 3, 1));
        } else if (festival == FestivalType.SUKKOT && stall == Stall.FRUIT) {
            out.add(0, new Listing("israel_simulator:citrus", 4, 1));
        }
        // time of day
        List<Listing> priced = new ArrayList<>(out.size());
        for (Listing l : out) {
            int count = l.count();
            if (period == Period.CLOSING) {
                count = count + Math.max(1, count / 2);           // sell-off: half again for the same price
            } else if (period == Period.MORNING && (stall == Stall.FRUIT || stall == Stall.FISH)) {
                count = count + Math.max(1, count / 3);           // fresh in the morning
            }
            priced.add(new Listing(l.item(), Math.min(count, 64), l.shekels()));
        }
        return priced;
    }

    /** Key that changes whenever the offers have to be rebuilt. */
    public static String stockKey(Period period, FestivalType festival) {
        return period.name() + "/" + (festival == null ? "-" : festival.name());
    }
}
